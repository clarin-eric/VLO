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
package eu.clarin.cmdi.vlo.wicket.panels.record;

import eu.clarin.cmdi.vlo.wicket.components.LanguageResourceSwitchboardLink;
import eu.clarin.cmdi.vlo.wicket.model.ResolvingLinkModel;
import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.LazyResourceInfoUpdateBehavior;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeIcon;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import static eu.clarin.cmdi.vlo.wicket.pages.RecordPage.HIERARCHY_SECTION;
import eu.clarin.cmdi.vlo.wicket.panels.BootstrapDropdown;
import eu.clarin.cmdi.vlo.wicket.panels.BootstrapDropdown.DropdownMenuItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 * Panel that shows all resources represented by a collection of resource
 * strings as links that trigger a details panel for the selected resource
 *
 * @author twagoo
 */
public abstract class ResourceLinksPanel extends GenericPanel<SolrDocument> {

    private final static Logger logger = LoggerFactory.getLogger(ResourceLinksPanel.class);

    private static final int ITEMS_PER_PAGE = 12;

    @SpringBean
    private PiwikConfig piwikConfig;
    @SpringBean(name = "resourceStringConverter")
    private ResourceStringConverter resourceStringConverter;
    @SpringBean(name = "resolvingResourceStringConverter")
    private ResourceStringConverter resolvingResourceStringConverter;
    @SpringBean
    private FieldNameService fieldNameService;

    private final IModel<List<String>> detailsVisibleModel = new ListModel<>(new ArrayList<String>());

    /**
     *
     * @param id panel id
     * @param documentModel model of document that holds the resources
     */
    public ResourceLinksPanel(String id, IModel<SolrDocument> documentModel) {
        super(id, documentModel);

        final SolrFieldModel<String> resourcesModel
                = new SolrFieldModel<>(documentModel, fieldNameService.getFieldName(FieldKey.RESOURCE));
        final IModel<String> landingPageModel
                // wrap in model that transforms handle links
                = new HandleLinkModel(
                        // get landing page from document
                        new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(FieldKey.LANDINGPAGE)));
        final SolrFieldModel<String> partCountModel
                = new SolrFieldModel<>(documentModel, fieldNameService.getFieldName(FieldKey.HAS_PART_COUNT));

        // create table of resources with optional details
        final ResourcesListView resourceListing = new ResourcesListView("resource", new CollectionListModel<>(resourcesModel));
        add(resourcesTable = createResourcesTable("resources", resourceListing));

        // pagination
        add(new BootstrapAjaxPagingNavigator("paging", resourceListing) {

            @Override
            protected void onConfigure() {
                setVisible(resourceListing.getPageCount() > 1);
            }

        });

        add(new MarkupContainer("noResources") {

            @Override
            protected void onConfigure() {
                setVisible(resourceListing.getPageCount() == 0);
            }

        }
                .add(new WebMarkupContainer("landingPageContainer") {
                    @Override
                    protected void onConfigure() {
                        setVisible(landingPageModel.getObject() != null);
                    }

                }.add(new ExternalLink("landingPageLink", landingPageModel)))
                .add(new WebMarkupContainer("hierarchyLinkContainer") {
                    @Override
                    protected void onConfigure() {
                        setVisible(partCountModel.getObject() != null);
                    }
                }.add(new AjaxFallbackLink("hierarchyLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        switchToTab(HIERARCHY_SECTION, target);
                    }
                })));

        //For Ajax updating of resource listing when paging
        setOutputMarkupId(true);
    }
    private final WebMarkupContainer resourcesTable;

    private WebMarkupContainer createResourcesTable(String id, final ResourcesListView resourceListing) {
        final WebMarkupContainer resourceListContainer = new WebMarkupContainer(id) {
            @Override
            protected void onConfigure() {
                setVisible(resourceListing.getPageCount() > 0);
            }

        };
        //add the actual listing
        resourceListContainer.add(resourceListing);

        resourceListContainer.setOutputMarkupId(true);
        return resourceListContainer;
    }

    private class ResourcesListView extends PageableListView<String> {

        public ResourcesListView(String id, IModel<? extends List<String>> model) {
            super(id, model, ITEMS_PER_PAGE);
            setReuseItems(true);
        }

        @Override
        protected void populateItem(ListItem<String> item) {
            final Model<Integer> itemIndexModel = Model.of(item.getIndex());

            final MarkupContainer columns = new WebMarkupContainer("itemColumns");
            columns.add(new EvenOddClassAppender(itemIndexModel));
            item.add(columns);

            final ResourceInfoModel resourceInfoModel = new ResourceInfoModel(resourceStringConverter, item.getModel());
            item.setDefaultModel(new CompoundPropertyModel<>(resourceInfoModel));

            // Resource type icon
            columns.add(new ResourceTypeIcon("resourceTypeIcon", new PropertyModel(resourceInfoModel, "resourceType")));

            // Resource link (and/or link label)
            // Create link that will show the resource when clicked
            final IModel<String> linkModel = ResolvingLinkModel.modelFor(resourceInfoModel, ResourceLinksPanel.this.getModel());
            final ExternalLink link = new ExternalLink("showResource", linkModel) {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    //hide if no absolute link could be resolved for the resource (see label below for fallback)
                    setVisible(linkModel.getObject() != null);
                }

            };

            // set the file name as the link's text content
            link.add(new Label("fileName", new PropertyModel(resourceInfoModel, "fileName")));
            // make the link update via AJAX with resolved location (in case of handle)
            link.add(new LazyResourceInfoUpdateBehavior(resolvingResourceStringConverter, resourceInfoModel) {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(link);
                }
            });

            link.setOutputMarkupId(true);
            columns.add(link);

            // Fallback label if no absolute link could be determined
            columns.add(new WebMarkupContainer("fileNameNotResolvable") {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(linkModel.getObject() == null);
                }

            }.add(new Label("fileName", new PropertyModel(resourceInfoModel, "fileName"))));

            // get the friendly name of the resource type dynamically from the resource bundle
            columns.add(new Label("resourceType", StringResourceModelMigration.of("resourcetype.${resourceType}.singular", resourceInfoModel, resourceInfoModel.getObject().getResourceType())));

            //detailed properties
            final IModel<Boolean> itemDetailsShownModel = new AbstractReadOnlyModel<Boolean>() {
                @Override
                public Boolean getObject() {
                    return detailsVisibleModel.getObject().contains(resourceInfoModel.getObject().getHref());
                }
            };

            // toggle details option
            columns.add(new ResourceDetailsToggleLink("details", new PropertyModel<String>(resourceInfoModel, "href"))
                    .add(new WebMarkupContainer("show").add(BooleanVisibilityBehavior.visibleOnFalse(itemDetailsShownModel)))
                    .add(new WebMarkupContainer("hide").add(BooleanVisibilityBehavior.visibleOnTrue(itemDetailsShownModel)))
            );

            item.add(new WebMarkupContainer("detailsColumns")
                    .add(new Label("mimeType"))
                    .add(new Label("href"))
                    .add(BooleanVisibilityBehavior.visibleOnTrue(itemDetailsShownModel))
                    .add(new EvenOddClassAppender(itemIndexModel))
            );

            columns.add(createOptionsDropdown(linkModel, resourceInfoModel));
        }

        protected Component createOptionsDropdown(final IModel<String> linkModel, final ResourceInfoModel resourceInfoModel) {
            final List<DropdownMenuItem> options = createDropDownOptions(linkModel, resourceInfoModel);

            return new BootstrapDropdown("dropdown", new ListModel(options)) {
                @Override
                protected Serializable getButtonClass() {
                    return null; //render as link, not button
                }

                @Override
                protected Serializable getButtonIconClass() {
                    return "glyphicon glyphicon-option-horizontal";
                }

                @Override
                protected boolean showCaret() {
                    return false;
                }

            };
        }

        private List<DropdownMenuItem> createDropDownOptions(final IModel<String> linkModel, final ResourceInfoModel resourceInfoModel) {
            return Lists.newArrayList(new DropdownMenuItem("Process with Language Resource Switchboard", "glyphicon glyphicon-open-file") {
                @Override
                protected Link getLink(String id) {
                    final IModel<Collection<Object>> languageValuesModel
                            = new SolrFieldModel<>(ResourceLinksPanel.this.getModel(), fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE));

                    final Link link = new LanguageResourceSwitchboardLink(id, linkModel, languageValuesModel, resourceInfoModel);

                    if (piwikConfig.isEnabled()) {
                        link.add(createLrsActionTrackingBehavior(resourceInfoModel));
                    }
                    return link;
                }
            }
            );
        }

    }

    private AjaxPiwikTrackingBehavior.EventTrackingBehavior createLrsActionTrackingBehavior(final ResourceInfoModel resourceInfoModel) {
        final AjaxPiwikTrackingBehavior.EventTrackingBehavior eventBehavior = new AjaxPiwikTrackingBehavior.EventTrackingBehavior("click", PiwikEventConstants.PIWIK_EVENT_CATEGORY_LRS, PiwikEventConstants.PIWIK_EVENT_ACTION_LRS_PROCESSRESOURCE) {
            @Override
            protected String getName(AjaxRequestTarget target) {
                return "ResourceDropdown";
            }

            @Override
            protected String getValue(AjaxRequestTarget target) {
                return resourceInfoModel.getObject().getHref();
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                attributes.setAsynchronous(false);
                super.updateAjaxAttributes(attributes);
            }

        };
        eventBehavior.setAsync(false);
        return eventBehavior;
    }

    public static class EvenOddClassAppender extends AttributeAppender {

        public EvenOddClassAppender(final IModel<Integer> indexModel) {
            super("class", new AbstractReadOnlyModel<String>() {
                @Override
                public String getObject() {
                    return indexModel.getObject() % 2 == 0 ? "even" : "odd";
                }
            }, " ");
        }

    }

    private class ResourceDetailsToggleLink extends AjaxFallbackLink<String> {

        public ResourceDetailsToggleLink(String id, IModel<String> idModel) {
            super(id, idModel);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            final String id = getModel().getObject();

            final List<String> visible = detailsVisibleModel.getObject();
            if (visible.contains(id)) {
                visible.remove(id);
            } else {
                visible.add(id);
            }

            if (target != null) {
                target.add(resourcesTable);
            }
        }

    }

    protected abstract void switchToTab(String tab, AjaxRequestTarget target);

}
