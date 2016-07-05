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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.RecordLicenseInfoPanel;
import eu.clarin.cmdi.vlo.wicket.model.PermaLinkModel;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.config.FieldValueDescriptor;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.HighlightSearchTermBehavior;
import eu.clarin.cmdi.vlo.wicket.PreferredExplicitOrdering;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.SearchContextModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.ErrorPage.ErrorType;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import eu.clarin.cmdi.vlo.wicket.panels.CmdiContentPanel;
import eu.clarin.cmdi.vlo.wicket.panels.ContentSearchFormPanel;
import eu.clarin.cmdi.vlo.wicket.panels.TopLinksPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.HierarchyPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.RecordNavigationPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.ResourceLinksPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchResultItemLicensePanel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;

/**
 *
 * @author twagoo
 */
public class RecordPage extends VloBasePage<SolrDocument> {

    public final static String DETAILS_SECTION = "details";
    public final static String AVAILABILITY_SECTION = "availability";
    public final static String RESOURCES_SECTION = "resources";
    public final static String ALL_METADATA_SECTION = "cmdi";
    public final static String TECHNICAL_DETAILS_SECTION = "technical";
    public final static String HIERARCHY_SECTION = "hierarchy";

    private final static List<String> TABS_ORDER
            = ImmutableList.of(DETAILS_SECTION, RESOURCES_SECTION, AVAILABILITY_SECTION, ALL_METADATA_SECTION, TECHNICAL_DETAILS_SECTION, HIERARCHY_SECTION);

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
            final PageParameters errorParams = new PageParameters(params)
                    .remove(VloWebAppParameters.DOCUMENT_ID);
            ErrorPage.triggerErrorPage(ErrorType.DOCUMENT_NOT_FOUND, errorParams);
        } else {
            setModel(new SolrDocumentModel(document));
        }

        addComponents(params);
    }

    private void addComponents(PageParameters params) {
        // Navigation
        add(createNavigation("recordNavigation"));

        final WebMarkupContainer topNavigation = new WebMarkupContainer("navigation");
        add(topNavigation
                .add(new BreadCrumbPanel("breadcrumbs", selectionModel, getModel()))
                .add(createPermalink("permalink", topNavigation))
                .add(new Link("backToSearch") {
                    @Override
                    public void onClick() {
                        setResponsePage(new FacetedSearchPage(selectionModel));
                    }
                })
                .setOutputMarkupId(true)
        );

        // General information section
        add(new SolrFieldLabel("name", getModel(), FacetConstants.FIELD_NAME, getString("recordpage.unnamedrecord")));
        add(createLandingPageLink("landingPageLink"));

        tabs = createTabs("tabs");
        final StringValue initialTab = params.get(VloWebAppParameters.RECORD_PAGE_TAB);
        if (!initialTab.isEmpty()) {
            final int tabIndex = TABS_ORDER.indexOf(initialTab.toString());
            if (tabIndex >= 0) {
                tabs.setSelectedTab(tabIndex);
            }
        }
        add(tabs);

        add(createSearchLinks("searchlinks"));
        //define the order for availability values
        final Ordering<String> availabilityOrdering = new PreferredExplicitOrdering(
                //extract the 'primary' availability values from the configuration
                FieldValueDescriptor.valuesList(config.getAvailabilityValues()));
        add(new SearchResultItemLicensePanel("licenseInfo", getModel(), navigationModel, availabilityOrdering) {
            @Override
            protected WebMarkupContainer createLink(String id) {
                return new AjaxFallbackLink(id) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        tabs.setSelectedTab(TABS_ORDER.indexOf(AVAILABILITY_SECTION));
                        if (target != null) {
                            target.add(tabs);
                        }
                    }
                };
            }

        });
    }
    private TabbedPanel tabs;

    private TabbedPanel createTabs(String id) {
        final List<ITab> tabs = new ArrayList(Collections.nCopies(TABS_ORDER.size(), null));
        tabs.set(TABS_ORDER.indexOf(DETAILS_SECTION), new AbstractTab(Model.of("Record details")) {
            @Override
            public Panel getPanel(String panelId) {
                final FieldsTablePanel fieldsTable = new FieldsTablePanel(panelId, new DocumentFieldsProvider(getModel(), basicPropertiesFilter, fieldOrder));
                fieldsTable.add(new HighlightSearchTermBehavior());
                return fieldsTable;
            }
        });
        tabs.set(TABS_ORDER.indexOf(AVAILABILITY_SECTION), new AbstractTab(Model.of("Availability")) {
            @Override
            public Panel getPanel(String panelId) {
                final RecordLicenseInfoPanel availabilityPanel = new RecordLicenseInfoPanel(panelId, getModel());
                availabilityPanel.setMarkupId(AVAILABILITY_SECTION); //TODO: make it possible to use this target to select license info
                return availabilityPanel;
            }
        });
        tabs.set(TABS_ORDER.indexOf(RESOURCES_SECTION), new AbstractTab(new StringResourceModel("recordpage.tabs.resources", // model to include resource count in tab title
                new SolrFieldStringModel(getModel(), FacetConstants.FIELD_RESOURCE_COUNT))) {
            @Override
            public Panel getPanel(String panelId) {
                return (new ResourceLinksPanel(panelId, getModel()));
            }
        });
        tabs.set(TABS_ORDER.indexOf(ALL_METADATA_SECTION), new AbstractTab(Model.of("All metadata")) {
            @Override
            public Panel getPanel(String panelId) {
                final CmdiContentPanel cmdiPanel = new CmdiContentPanel(panelId, getModel());
                cmdiPanel.add(new HighlightSearchTermBehavior()); // highlight search terms when panel becomes visible
                return cmdiPanel;
            }
        });
        tabs.set(TABS_ORDER.indexOf(TECHNICAL_DETAILS_SECTION), new AbstractTab(Model.of("Technical details")) {
            @Override
            public Panel getPanel(String panelId) {
                return new FieldsTablePanel(panelId, new DocumentFieldsProvider(getModel(), technicalPropertiesFilter, fieldOrder));
            }
        });

        if (config.isProcessHierarchies()) {
            //TODO: make hierarchy an optional side pane instead
            tabs.set(TABS_ORDER.indexOf(HIERARCHY_SECTION), new AbstractTab(Model.of("Hierarchy")) {
                @Override
                public Panel getPanel(String panelId) {
                    return new HierarchyPanel(panelId, getModel());
                }

                @Override
                public boolean isVisible() {
                    // only show hierarchy panel if there's anything to show
                    final SolrDocument document = getModel().getObject();
                    final Object partCount = document.getFieldValue(FacetConstants.FIELD_HAS_PART_COUNT);
                    final boolean hasHierarchy // has known parent or children
                            = null != document.getFieldValue(FacetConstants.FIELD_IS_PART_OF) // has parent
                            || (null != partCount && !Integer.valueOf(0).equals(partCount)); // children count != 0
                    return hasHierarchy;
                }

            });
        } else {
            tabs.remove(TABS_ORDER.indexOf(HIERARCHY_SECTION));
        }

        return new AjaxBootstrapTabbedPanel(id, tabs);
    }

    private Component createNavigation(final String id) {
        if (navigationModel != null) {
            final IModel<String> tabModel = new AbstractReadOnlyModel<String>() {
                @Override
                public String getObject() {
                    return TABS_ORDER.get(tabs.getSelectedTab());
                }
            };

            // Add a panel that shows the index of the current record in the
            // resultset and allows for forward/backward navigation
            return new RecordNavigationPanel(id, navigationModel, tabModel) {

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
        return new TopLinksPanel(id, new PermaLinkModel(getPageClass(), selectionModel, getModel()), getTitleModel()) {

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

    private Component createSearchLinks(String id) {
        final SolrFieldModel<String> searchPageModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_SEARCHPAGE);
        final SolrFieldModel<String> searchServiceModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_SEARCH_SERVICE);
        return new WebMarkupContainer(id) {
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

}
