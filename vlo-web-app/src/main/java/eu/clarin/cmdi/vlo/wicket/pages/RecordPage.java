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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.model.UrlFromStringModel;
import eu.clarin.cmdi.vlo.wicket.model.XsltModel;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import eu.clarin.cmdi.vlo.wicket.panels.PermaLinkPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.ContentSearchFormPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.RecordNavigationPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.ResourceLinksPanel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;

/**
 *
 * @author twagoo
 */
public class RecordPage extends VloBasePage<SolrDocument> {

    @SpringBean
    private PageParametersConverter<QueryFacetsSelection> selectionParametersConverter;
    @SpringBean(name = "basicPropertiesFilter")
    private FieldFilter basicPropertiesFilter;
    @SpringBean(name = "technicalPropertiesFilter")
    private FieldFilter technicalPropertiesFilter;

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
        // Coming from bookmark or external link, so no navigation context
        this.navigationModel = null;

        final QueryFacetsSelection selection = selectionParametersConverter.fromParameters(params);
        selectionModel = Model.of(selection);

        final StringValue docId = params.get("docId");
        if (docId.isEmpty()) {
            Session.get().error("No document ID provided");
            throw new RestartResponseException(new FacetedSearchPage(selectionModel));
        }
        final SolrDocumentModel documentModel = new SolrDocumentModel(docId.toString());
        if (null == documentModel.getObject()) {
            Session.get().error(String.format("Document with ID %s could not be found", docId));
            throw new RestartResponseException(new FacetedSearchPage(selectionModel));
        }
        setModel(documentModel);

        addComponents();
    }

    /**
     * Constructor using existing models (when constructed explicitly in code)
     *
     * @param documentModel
     * @param contextModel
     */
    public RecordPage(IModel<SolrDocument> documentModel, IModel<SearchContext> contextModel) {
        super(documentModel);
        this.navigationModel = contextModel;
        this.selectionModel = new PropertyModel(contextModel, "selection");
        addComponents();
    }

    private void addComponents() {
        // Navigation
        add(createNavigation("navigation"));

        final WebMarkupContainer topNavigation = new WebMarkupContainer("topnavigation");
        topNavigation.setOutputMarkupId(true);
        add(topNavigation);

        topNavigation.add(new BreadCrumbPanel("breadcrumbs", selectionModel));
        topNavigation.add(createPermalink("permalink", topNavigation));

        // General information section
        add(new SolrFieldLabel("name", getModel(), FacetConstants.FIELD_NAME, "Unnamed record"));
        add(createLandingPageLink("landingPageLink"));
        add(new FieldsTablePanel("documentProperties", new DocumentFieldsProvider(getModel(), basicPropertiesFilter)));

        // Resources section
        add(new ResourceLinksPanel("resources", new SolrFieldModel<String>(getModel(), FacetConstants.FIELD_RESOURCE)));

        // Technical section
        add(createCmdiContent("cmdi"));
        add(new FieldsTablePanel("technicalProperties", new DocumentFieldsProvider(getModel(), technicalPropertiesFilter)));

        createSearchLinks("searchlinks");
    }

    private Component createNavigation(final String id) {
        if (navigationModel != null) {
            // Add a panel that shows the index of the current record in the
            // resultset and allows for forward/backward navigation
            return new RecordNavigationPanel(id, navigationModel);
        } else {
            // If no context model is available (i.e. when coming from a bookmark
            // or external link, do not show the navigation panel
            final WebMarkupContainer navigationDummy = new WebMarkupContainer(id);
            navigationDummy.setVisible(false);
            return navigationDummy;
        }
    }

    private PermaLinkPanel createPermalink(String id, final WebMarkupContainer topNavigation) {
        return new PermaLinkPanel(id, selectionModel, getModel()) {

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
        final SolrFieldModel<String> searchPageModel = new SolrFieldModel<String>(getModel(), FacetConstants.FIELD_SEARCHPAGE);
        final SolrFieldModel<String> searchServiceModel = new SolrFieldModel<String>(getModel(), FacetConstants.FIELD_SEARCH_SERVICE);
        add(new WebMarkupContainer(id) {
            {
                //Add search page links (can be multiple)
                add(new ListView<String>("searchPage", new CollectionListModel<String>(searchPageModel)) {

                    @Override
                    protected void populateItem(ListItem item) {
                        item.add(new ExternalLink("searchLink", item.getModel()));
                    }
                });

                // We assume there can be multiple content search endpoints too
                add(new ListView<String>("contentSearch", new CollectionListModel<String>(searchServiceModel)) {

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

    private Label createCmdiContent(String id) {
        final IModel<String> locationModel = new SolrFieldStringModel(getModel(), FacetConstants.FIELD_FILENAME);
        final UrlFromStringModel locationUrlModel = new UrlFromStringModel(locationModel);
        final Label cmdiContentLabel = new Label(id, new XsltModel(locationUrlModel));
        cmdiContentLabel.setEscapeModelStrings(false);
        return cmdiContentLabel;
    }

    @Override
    public void detachModels() {
        super.detachModels();
        if (navigationModel != null) {
            // not passed to parent
            navigationModel.detach();
        }
    }

}
