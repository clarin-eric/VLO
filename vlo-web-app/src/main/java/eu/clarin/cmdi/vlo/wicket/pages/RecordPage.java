/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.model.PermaLinkModel;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.HighlightSearchTermBehavior;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.SearchContextModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.model.UrlFromStringModel;
import eu.clarin.cmdi.vlo.wicket.model.XsltModel;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import eu.clarin.cmdi.vlo.wicket.panels.ContentSearchFormPanel;
import eu.clarin.cmdi.vlo.wicket.panels.TogglePanel;
import eu.clarin.cmdi.vlo.wicket.panels.TopLinksPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.HierarchyPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.RecordNavigationPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.ResourceLinksPanel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class RecordPage extends VloBasePage<SolrDocument> {

    private final static ResourceReference CMDI_HTML_CSS = new CssResourceReference(RecordPage.class, "cmdi.css");

    @SpringBean(name = "documentParamsConverter")
    private PageParametersConverter<SolrDocument> documentParamConverter;
    @SpringBean(name = "queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> selectionParametersConverter;
    @SpringBean(name = "searchContextParamsConverter")
    private PageParametersConverter<SearchContext> contextParamConverter;
    @SpringBean(name = "basicPropertiesFilter")
    private FieldFilter basicPropertiesFilter;
    @SpringBean(name = "technicalPropertiesFilter")
    private FieldFilter technicalPropertiesFilter;
    @SpringBean(name = "documentFieldOrder")
    private List<String> fieldOrder;
    @SpringBean
    private VloConfig config;

    private final IModel<SearchContext> navigationModel;
    private final IModel<QueryFacetsSelection> selectionModel;

    /**
     * Constructor that derives document and selection models from page
     * parameters (external request or through the framework)
     *
     * @param params
     */
    public RecordPage(PageParameters params) {
        super(params);

        // get search context from params if available
        final SearchContext searchContext = contextParamConverter.fromParameters(params);
        if (searchContext instanceof SearchContextModel) {
            this.navigationModel = (SearchContextModel) (searchContext);
        } else if (searchContext != null) {
            this.navigationModel = Model.of(searchContext);
        } else {
            this.navigationModel = null;
        }

        // get selection from context or parameters
        if (navigationModel == null) {
            final QueryFacetsSelection selection = selectionParametersConverter.fromParameters(params);
            selectionModel = Model.of(selection);
        } else {
            selectionModel = new PropertyModel(navigationModel, "selection");
        }

        // get document from parameters
        final SolrDocument document = documentParamConverter.fromParameters(params);
        if (null == document) {
            Session.get().error(String.format("Document with ID %s could not be found", params.get(VloWebAppParameters.DOCUMENT_ID)));
            throw new RestartResponseException(new FacetedSearchPage(selectionModel));
        } else {
            setModel(new SolrDocumentModel(document));
        }

        addComponents();
    }

    private void addComponents() {
        // Navigation
        add(createNavigation("navigation"));

        final WebMarkupContainer topNavigation = new WebMarkupContainer("topnavigation");
        add(topNavigation
                .add(new BreadCrumbPanel("breadcrumbs", selectionModel))
                .add(createPermalink("permalink", topNavigation))
                .setOutputMarkupId(true)
        );

        // General information section
        add(new SolrFieldLabel("name", getModel(), FacetConstants.FIELD_NAME, getString("recordpage.unnamedrecord")));
        add(createLandingPageLink("landingPageLink"));
        
        final FieldsTablePanel fieldsTable = new FieldsTablePanel("documentProperties", new DocumentFieldsProvider(getModel(), basicPropertiesFilter, fieldOrder));
        fieldsTable.add(new HighlightSearchTermBehavior());
        add(fieldsTable);

        // Resources section
        add(new ResourceLinksPanel("resources", new SolrFieldModel<String>(getModel(), FacetConstants.FIELD_RESOURCE)));

        // Technical section
        add(createCmdiContent("cmdi"));
        add(createTechnicalDetailsPanel("technicalProperties"));

        if (config.isProcessHierarchies()) {
            // show hierarchy if applicable
            add(createHierarchyPanel("recordtree"));
        } else {
            // invisible stub
            add(new WebMarkupContainer("recordtree") {

                @Override
                public boolean isVisible() {
                    return false;
                }

            });
        }

        createSearchLinks("searchlinks");
    }

    private Component createNavigation(final String id) {
        if (navigationModel != null) {
            // Add a panel that shows the index of the current record in the
            // resultset and allows for forward/backward navigation
            return new RecordNavigationPanel(id, navigationModel) {

                @Override
                protected void onConfigure() {
                    final SearchContext context = navigationModel.getObject();
                    setVisible(context != null && context.getResultCount() > 1);
                }

            };
        } else {
            // If no context model is available (i.e. when coming from a bookmark
            // or external link, do not show the navigation panel
            final WebMarkupContainer navigationDummy = new WebMarkupContainer(id);
            navigationDummy.setVisible(false);
            return navigationDummy;
        }
    }

    private TopLinksPanel createPermalink(String id, final WebMarkupContainer topNavigation) {
        return new TopLinksPanel(id, new PermaLinkModel(getPageClass(), selectionModel, getModel())) {

            @Override
            protected void onChange(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(topNavigation);
                }
            }

        };
    }

    private ExternalLink createLandingPageLink(String id) {
        final IModel<String> landingPageHrefModel
                // wrap in model that transforms handle links
                = new HandleLinkModel(
                        // get landing page from document
                        new SolrFieldStringModel(getModel(), FacetConstants.FIELD_LANDINGPAGE));
        // add landing page link
        final ExternalLink landingPageLink = new ExternalLink(id, landingPageHrefModel) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(landingPageHrefModel.getObject() != null);
            }

        };
        return landingPageLink;
    }

    private void createSearchLinks(String id) {
        final SolrFieldModel<String> searchPageModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_SEARCHPAGE);
        final SolrFieldModel<String> searchServiceModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_SEARCH_SERVICE);
        add(new WebMarkupContainer(id) {
            {
                //Add search page links (can be multiple)
                add(new ListView<String>("searchPage", new CollectionListModel<>(searchPageModel)) {

                    @Override
                    protected void populateItem(ListItem item) {
                        item.add(new ExternalLink("searchLink", item.getModel()));
                    }
                });

                // We assume there can be multiple content search endpoints too
                add(new ListView<String>("contentSearch", new CollectionListModel<>(searchServiceModel)) {

                    @Override
                    protected void populateItem(ListItem<String> item) {
                        item.add(new ContentSearchFormPanel("fcsForm", RecordPage.this.getModel(), item.getModel()));
                    }
                });
            }

            @Override

            protected void onConfigure() {
                super.onConfigure();
                setVisible(searchPageModel.getObject() != null || searchServiceModel.getObject() != null);
            }

        });
    }

    private Component createCmdiContent(String id) {

        final IModel<String> locationModel = new SolrFieldStringModel(getModel(), FacetConstants.FIELD_FILENAME);
        final UrlFromStringModel locationUrlModel = new UrlFromStringModel(locationModel);
        final TogglePanel togglePanel = new TogglePanel(id, Model.of("Show all metadata fields"), Model.of("Hide all metadata fields")) {

            @Override
            protected Component createContent(String id) {
                final Label cmdiContentLabel = new Label(id, new XsltModel(locationUrlModel));
                cmdiContentLabel.setEscapeModelStrings(false);
                return cmdiContentLabel;
            }
        };
        // highlight search terms when panel becomes visible
        togglePanel.add(new HighlightSearchTermBehavior());
        return togglePanel;
    }

    private TogglePanel createTechnicalDetailsPanel(String id) {
        return new TogglePanel(id, Model.of("Show technical details"), Model.of("Hide technical details")) {

            @Override
            protected Component createContent(String id) {
                return new FieldsTablePanel(id, new DocumentFieldsProvider(getModel(), technicalPropertiesFilter, fieldOrder));
            }
        };
    }

    private HierarchyPanel createHierarchyPanel(String id) {
        return new HierarchyPanel(id, getModel()) {

            @Override
            protected void onConfigure() {
                final SolrDocument document = getModel().getObject();
                final Object partCount = document.getFieldValue(FacetConstants.FIELD_HAS_PART_COUNT);
                final boolean hasHierarchy // has known parent or children
                        = null != document.getFieldValue(FacetConstants.FIELD_IS_PART_OF) // has parent
                        || (null != partCount && !Integer.valueOf(0).equals(partCount)); // children count != 0

                // only show hierarchy panel if there's anything to show
                setVisible(hasHierarchy);
            }

        };
    }

    @Override
    public void detachModels() {
        super.detachModels();
        if (navigationModel != null) {
            // not passed to parent
            navigationModel.detach();
        }
    }

    @Override
    public IModel<String> getTitleModel() {
        // Put the name of the record in the page title
        return new StringResourceModel("recordpage.title",
                new NullFallbackModel(new SolrFieldStringModel(getModel(), FacetConstants.FIELD_NAME), getString("recordpage.unnamedrecord")))
                .setDefaultValue(DEFAULT_PAGE_TITLE);
    }

    @Override
    public IModel<String> getPageDescriptionModel() {
        return new SolrFieldStringModel(getModel(), FacetConstants.FIELD_DESCRIPTION);
    }

    @Override
    public IModel<String> getCanonicalUrlModel() {
        // omit query in link for canonical URL (record page gets same canonical
        // URL regardless of search term)
        return new PermaLinkModel(getPageClass(), null, getModel());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // add styling for CMDI to HTML transformation output
        response.render(CssHeaderItem.forReference(CMDI_HTML_CSS));
    }

}
