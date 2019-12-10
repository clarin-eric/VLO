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
import com.google.common.collect.Streams;

import de.agilecoders.wicket.core.markup.html.bootstrap.tabs.AjaxBootstrapTabbedPanel;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.VloWebSession;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldValueDescriptor;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.PageView;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.HighlightSearchTermBehavior;
import eu.clarin.cmdi.vlo.wicket.PreferredExplicitOrdering;
import eu.clarin.cmdi.vlo.wicket.components.SingleValueSolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.historyapi.HistoryApiAware;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.PermaLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.RecordHasHierarchyModel;
import eu.clarin.cmdi.vlo.wicket.model.SearchContextModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.ErrorPage.ErrorType;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import eu.clarin.cmdi.vlo.wicket.panels.CmdiContentPanel;
import eu.clarin.cmdi.vlo.wicket.panels.CopyPageLinkPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.HierarchyPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.RecordDetailsPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.RecordLicenseInfoPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.RecordNavigationPanel;
import eu.clarin.cmdi.vlo.wicket.panels.record.ResourceLinksPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchResultItemLicensePanel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class RecordPage extends VloBasePage<SolrDocument> implements HistoryApiAware {

    private final static Logger logger = LoggerFactory.getLogger(RecordPage.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public final static String DETAILS_SECTION = "details";
    public final static String AVAILABILITY_SECTION = "availability";
    public final static String RESOURCES_SECTION = "resources";
    public final static String ALL_METADATA_SECTION = "cmdi";
    public final static String TECHNICAL_DETAILS_SECTION = "technical";
    public final static String HIERARCHY_SECTION = "hierarchy";

    private final static List<String> TABS_ORDER = ImmutableList.of(DETAILS_SECTION, RESOURCES_SECTION,
            AVAILABILITY_SECTION, ALL_METADATA_SECTION, TECHNICAL_DETAILS_SECTION, HIERARCHY_SECTION);

    @SpringBean(name = "documentParamsConverter")
    private PageParametersConverter<SolrDocument> documentParamConverter;
    @SpringBean(name = "queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> selectionParametersConverter;
    @SpringBean(name = "searchContextParamsConverter")
    private PageParametersConverter<SearchContext> contextParamConverter;
    @SpringBean(name = "technicalPropertiesFilter")
    private FieldFilter technicalPropertiesFilter;
    @SpringBean(name = "documentFieldOrder")
    private List<String> fieldOrder;
    @SpringBean
    private VloConfig config;
    @SpringBean
    private PiwikConfig piwikConfig;
    @SpringBean
    private FieldNameService fieldNameService;

    private final IModel<SearchContext> navigationModel;
    private final IModel<QueryFacetsSelection> selectionModel;
    private final IModel<String> linksCountLabelModel;

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
            selectionModel = new PropertyModel<>(navigationModel, "selection");
        }

        // get document from parameters
        final SolrDocument document = documentParamConverter.fromParameters(params);
        if (null == document) {
            Session.get().error(String.format("Document with ID %s could not be found", params.get(VloWebAppParameters.DOCUMENT_ID)));
            final PageParameters errorParams = new PageParameters(params)
                    .remove(VloWebAppParameters.DOCUMENT_ID);
            ErrorPage.triggerErrorPage(ErrorType.DOCUMENT_NOT_FOUND, errorParams);
        } else {
            try {
                // save these information (id, ip, url, referer) in postgresql DB for record
                // exposures
                String id = document.get("id").toString();
                HttpServletRequest httpRequest = ((ServletWebRequest) RequestCycle.get().getRequest())
                        .getContainerRequest();
                String pageUrl = httpRequest.getRequestURL().toString() + "?" + httpRequest.getQueryString();
                // get user ip address
                String ip = ((WebClientInfo) VloWebSession.get().getClientInfo()).getProperties().getRemoteAddress();
                String referer = httpRequest.getHeader("referer");
                // create PageView object and save it in the DB
                PageView pageView = new PageView(id, ip, pageUrl, referer);
                pageView.save(config);

            } catch (Exception e) {
                logger.error(e.getMessage());
            }

            setModel(new SolrDocumentModel(document, fieldNameService));
        }

        linksCountLabelModel = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                final SolrDocument document = RecordPage.this.getModelObject();
                if (document != null) {
                    final Object countValue = document.getFieldValue(fieldNameService.getFieldName(FieldKey.RESOURCE_COUNT));
                    if (countValue instanceof Integer) {
                        Long fieldValuesCount
                                = Streams.concat(
                                        Optional.ofNullable(document.getFieldValues(fieldNameService.getFieldName(FieldKey.LANDINGPAGE))).map(f -> f.stream()).orElse(Stream.empty()),
                                        Optional.ofNullable(document.getFieldValues(fieldNameService.getFieldName(FieldKey.SEARCHPAGE))).map(f -> f.stream()).orElse(Stream.empty()),
                                        Optional.ofNullable(document.getFieldValues(fieldNameService.getFieldName(FieldKey.SEARCH_SERVICE))).map(f -> f.stream()).orElse(Stream.empty()))
                                        .collect(Collectors.counting());
                        if (fieldValuesCount == 0) {
                            return ((Integer) countValue).toString();
                        } else {
                            return Long.toString(fieldValuesCount + (Integer) countValue);
                        }
                    }
                }
                return null;
            }
        };

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
                }).setOutputMarkupId(true));

        // General information section
        add(new SingleValueSolrFieldLabel("name", getModel(), fieldNameService.getFieldName(FieldKey.NAME), getString("recordpage.unnamedrecord")));

        tabs = createTabs("tabs");
        final StringValue initialTab = params.get(VloWebAppParameters.RECORD_PAGE_TAB);
        if (!initialTab.isEmpty()) {
            switchToTab(initialTab.toString(), Optional.empty());
        }
        add(tabs);

        //define the order for availability values
        final Ordering<String> availabilityOrdering = new PreferredExplicitOrdering<>(
                //extract the 'primary' availability values from the configuration
                FieldValueDescriptor.valuesList(config.getAvailabilityValues()));
        add(new SearchResultItemLicensePanel("licenseInfo", getModel(), navigationModel, availabilityOrdering) {
            @Override
            protected WebMarkupContainer createLink(String id) {
                return new AjaxFallbackLink<Void>(id) {
                    @Override
                    public void onClick(Optional<AjaxRequestTarget> target) {
                        switchToTab(AVAILABILITY_SECTION, target);
                    }
                };
            }

        });
    }

    private TabbedPanel tabs;

    private TabbedPanel createTabs(String id) {
        final List<ITab> tabs = new ArrayList<>(Collections.nCopies(TABS_ORDER.size(), null));
        tabs.set(TABS_ORDER.indexOf(DETAILS_SECTION), new AbstractTab(Model.of("Record details")) {
            @Override
            public Panel getPanel(String panelId) {
                return new RecordDetailsPanel(panelId, getModel()) {
                    @Override
                    protected void switchToTab(String tab, Optional<AjaxRequestTarget> target) {
                        RecordPage.this.switchToTab(tab, target);
                    }

                };
            }
        });
        tabs.set(TABS_ORDER.indexOf(AVAILABILITY_SECTION), new AbstractTab(Model.of("Availability")) {
            @Override
            public Panel getPanel(String panelId) {
                final RecordLicenseInfoPanel availabilityPanel = new RecordLicenseInfoPanel(panelId, getModel());
                availabilityPanel.setMarkupId(AVAILABILITY_SECTION); // TODO: make it possible to use this target to
                // select license info
                return availabilityPanel;
            }
        });
        tabs.set(TABS_ORDER.indexOf(RESOURCES_SECTION),
                new AbstractTab(
                        new StringResourceModel("recordpage.tabs.links", new NullFallbackModel<>(linksCountLabelModel, "?"))) {
            @Override
            public Panel getPanel(String panelId) {
                return (new ResourceLinksPanel(panelId, getModel()) {
                    @Override
                    protected void switchToTab(String tab, Optional<AjaxRequestTarget> target) {
                        RecordPage.this.switchToTab(tab, target);
                    }

                });
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

            final IModel<Boolean> hasHierarchyModel = new RecordHasHierarchyModel(getModel());

            tabs.set(TABS_ORDER.indexOf(HIERARCHY_SECTION), new AbstractTab(Model.of("Hierarchy")) {
                @Override
                public Panel getPanel(String panelId) {
                    return new HierarchyPanel(panelId, getModel());
                }

                @Override
                public boolean isVisible() {
                    return hasHierarchyModel.getObject();
                }

            });
        } else {
            tabs.remove(TABS_ORDER.indexOf(HIERARCHY_SECTION));
        }

        return new AjaxBootstrapTabbedPanel<>(id, tabs) {
            @Override
            protected WebMarkupContainer newLink(String linkId, final int index) {
                final WebMarkupContainer link = super.newLink(linkId, index);
                if (piwikConfig.isEnabled()) {
                    link.add(new AjaxPiwikTrackingBehavior.EventTrackingBehavior("click", PiwikEventConstants.PIWIK_EVENT_CATEGORY_RECORDPAGE, PiwikEventConstants.PIWIK_EVENT_ACTION_RECORDPAGE_TABSWITCH) {
                        @Override
                        protected String getName(AjaxRequestTarget target) {
                            return TABS_ORDER.get(index);
                        }
                    });
                }
                return link;
            }

        };
    }

    private Component createNavigation(final String id) {
        if (navigationModel != null) {
            final IModel<String> tabModel = new IModel<String>() {
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
                    super.onConfigure();
                    final SearchContext context = navigationModel.getObject();
                    setVisible(context != null && (context.hasNext() || context.hasPrevious()));
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

    private CopyPageLinkPanel createPermalink(String id, final WebMarkupContainer topNavigation) {
        return new CopyPageLinkPanel(id, new PermaLinkModel(getPageClass(), selectionModel, getModel()), getTitleModel()) {

            @Override
            protected void onChange(Optional<AjaxRequestTarget> target) {
                target.ifPresent(t -> {
                    t.add(topNavigation);
                });
            }

        };
    }

    private void switchToTab(String tab, Optional<AjaxRequestTarget> target) {
        final int tabIndex = TABS_ORDER.indexOf(tab);
        if (tabIndex >= 0) {
            RecordPage.this.tabs.setSelectedTab(tabIndex);
            target.ifPresent(t -> {
                t.add(RecordPage.this.tabs);
            });
        }
    }

    @Override
    public PageParameters getHistoryApiPageParameters() {
        // Merge document params, selection params
        final PageParameters params = documentParamConverter.toParameters(getModelObject())
                .mergeWith(selectionParametersConverter.toParameters(selectionModel.getObject()));

        // Add navigation params if present
        if (navigationModel != null && navigationModel.getObject() != null) {
            params.mergeWith(contextParamConverter.toParameters(navigationModel.getObject()));
        }

        // Add tab id if selected tab is not the default tab
        if (tabs.getSelectedTab() > 0) {
            params.add(VloWebAppParameters.RECORD_PAGE_TAB, TABS_ORDER.get(tabs.getSelectedTab()));
        }

        return params;
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
                new NullFallbackModel<>(new SolrFieldStringModel(getModel(), fieldNameService.getFieldName(FieldKey.NAME), true), getString("recordpage.unnamedrecord")))
                .setDefaultValue(DEFAULT_PAGE_TITLE);
    }

    @Override
    public IModel<String> getPageDescriptionModel() {
        return new SolrFieldStringModel(getModel(), fieldNameService.getFieldName(FieldKey.DESCRIPTION));
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
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getBootstrapTour()));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FacetedSearchPage.class, "vlo-tour.js")));
        response.render(JavaScriptHeaderItem.forScript("initTourRecordPage();", "initTourRecordPage"));
    }
}
