/*
 * Copyright (C) 2017 CLARIN
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

import eu.clarin.cmdi.vlo.config.FieldNameService;
//import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.HighlightSearchTermBehavior;
import eu.clarin.cmdi.vlo.wicket.LazyResourceInfoUpdateBehavior;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeIcon;
import eu.clarin.cmdi.vlo.wicket.model.ResolvingLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import static eu.clarin.cmdi.vlo.wicket.pages.RecordPage.RESOURCES_SECTION;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import eu.clarin.cmdi.vlo.wicket.components.PIDLinkLabel;
import eu.clarin.cmdi.vlo.wicket.components.ResourceAvailabilityWarningBadge;
import eu.clarin.cmdi.vlo.wicket.model.PIDLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.IsPidModel;
import eu.clarin.cmdi.vlo.wicket.model.PIDContext;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.Model;

/**
 * Panel that shows the "basic" (non-technical) property fields of a document
 * through a {@link FieldsTablePanel} with a {@link FieldFilter}; for resources
 * with a single resource, some information about this single resource is also
 * shown.
 *
 * @author twagoo
 */
public abstract class RecordDetailsPanel extends GenericPanel<SolrDocument> {

    private static final int PID_LABEL_TEXT_LENGTH = 25;

    @SpringBean(name = "basicPropertiesFilter")
    private FieldFilter basicPropertiesFilter;
    @SpringBean(name = "documentFieldOrder")
    private List<String> fieldOrder;
    @SpringBean(name = "resourceStringConverter")
    private ResourceStringConverter resourceStringConverter;
    @SpringBean(name = "resolvingResourceStringConverter")
    private ResourceStringConverter resolvingResourceStringConverter;
    @SpringBean
    private FieldNameService fieldNameService;

    private final SolrFieldModel<String> resourcesModel;
    private final ResourceInfoModel resourceInfoModel;

    public RecordDetailsPanel(String id, IModel<SolrDocument> model) {
        super(id, model);

        resourcesModel = new SolrFieldModel<>(model, fieldNameService.getFieldName(FieldKey.RESOURCE));
        resourceInfoModel = new ResourceInfoModel(resourceStringConverter, new SolrFieldStringModel(model, fieldNameService.getFieldName(FieldKey.RESOURCE)));

        add(createCoreLinksPanel("coreLinks"));

        // Fields table
        add(new FieldsTablePanel("fieldsTable", new DocumentFieldsProvider(getModel(), basicPropertiesFilter, fieldOrder))
                .add(new HighlightSearchTermBehavior())
        );

        add(new SimilarDocumentsPanel("similar", getModel()));
    }

    /**
     * Creates a panel for 'core links' (landing page and/or single resource)
     *
     * @param id
     * @return
     */
    private Component createCoreLinksPanel(String id) {
        final WebMarkupContainer coreLinksContainer = new WebMarkupContainer(id);

        final ResourceInfoModel landingPageResourceInfoModel = new ResourceInfoModel(resourceStringConverter, new SolrFieldStringModel(getModel(), fieldNameService.getFieldName(FieldKey.LANDINGPAGE)));

        final IModel<Boolean> landingPageVisibilityModel = new AbstractReadOnlyModel<Boolean>() {
            @Override
            public Boolean getObject() {
                return landingPageResourceInfoModel.getObject() != null
                        && landingPageResourceInfoModel.getObject().getHref() != null;
            }
        };

        coreLinksContainer
                .add(createLandingPageLink("landingPage", landingPageResourceInfoModel).
                        add(BooleanVisibilityBehavior.visibleOnTrue(landingPageVisibilityModel)));

        // resource info for single resource
        final ResolvingLinkModel resourceInfoLinkModel = ResolvingLinkModel.modelFor(resourceInfoModel, getModel());
        final IModel<Boolean> resourceInfoVisibilityModel = new AbstractReadOnlyModel<Boolean>() {
            @Override
            public Boolean getObject() {
                return (resourcesModel.getObject() != null
                        && resourcesModel.getObject().size() == 1
                        && resourceInfoLinkModel.getObject() != null);
            }
        };
        coreLinksContainer
                .add(createSingleResourceInfo("resourceInfo", resourceInfoLinkModel)
                        .add(BooleanVisibilityBehavior.visibleOnTrue(resourceInfoVisibilityModel)));

        final AbstractReadOnlyModel<Boolean> resourcesLinkVisibilityModel = new AbstractReadOnlyModel<Boolean>() {
            @Override
            public Boolean getObject() {
                return resourcesModel.getObject() != null
                        && resourcesModel.getObject().size() > 1;
            }
        };
        coreLinksContainer
                .add(createMultipleResourceLink("resourcesInfo")
                        .add(BooleanVisibilityBehavior.visibleOnTrue(resourcesLinkVisibilityModel)));

        coreLinksContainer.add(
                new Label("resourcesTitle", new AbstractReadOnlyModel() {
                    @Override
                    public Object getObject() {
                        return (resourcesModel.getObject() != null && resourcesModel.getObject().size() == 1)
                                ? "Linked resource"
                                : "Linked resources";
                    }
                }).add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(
                                landingPageVisibilityModel.getObject() // landing page link is visible
                                && (resourceInfoVisibilityModel.getObject() || resourcesLinkVisibilityModel.getObject()) // and resource(s) info as well
                        );
                    }

                }));

        return coreLinksContainer;
    }

    private Component createLandingPageLink(String id, ResourceInfoModel resourceModel) {
        final IModel<String> linkModel = new PropertyModel(resourceModel, "href");
        final IsPidModel isPidModel = new IsPidModel(linkModel);
        final PIDLinkModel pidLinkModel = PIDLinkModel.wrapLinkModel(linkModel);

        final AjaxFallbackLink showResourcesLink = new AjaxFallbackLink("showResources") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                switchToTab(RESOURCES_SECTION, target);
            }
        };

        final WebMarkupContainer availabilityWarning = new ResourceAvailabilityWarningBadge("availabilityWarning", resourceModel);
        showResourcesLink.add(availabilityWarning);

        return new WebMarkupContainer(id)
                .add(showResourcesLink)
                .add(new ExternalLink("landingPageLink", pidLinkModel)
                        .add(new Label("landingPageLinkLabel", linkModel)
                                .add(BooleanVisibilityBehavior.visibleOnFalse(isPidModel))))
                .add(new PIDLinkLabel("landingPagePidLabel", pidLinkModel, Model.of(PIDContext.LANDING_PAGE), PID_LABEL_TEXT_LENGTH)
                        .add(BooleanVisibilityBehavior.visibleOnTrue(isPidModel)));
    }

    /**
     * Information component for a single linked resource
     *
     * @param id
     * @param linkModel
     * @return
     */
    private Component createSingleResourceInfo(String id, IModel<String> linkModel) {
        final IsPidModel isPidModel = new IsPidModel(linkModel);

        final WebMarkupContainer resourceInfo = new WebMarkupContainer(id);

        // Resource info for single resource (should not appear if there are more or fewer resources)
        resourceInfo.add(new ExternalLink("resourceLink", new PIDLinkModel(ResolvingLinkModel.modelFor(resourceInfoModel, getModel())))
                //resource type icon
                .add(new ResourceTypeIcon("resourceTypeIcon", new PropertyModel<String>(resourceInfoModel, "resourceType"))
                        //with type name tooltip
                        .add(new AttributeModifier("title", new StringResourceModel("resourcetype.${resourceType}.singular", this, resourceInfoModel).setDefaultValue(new PropertyModel(resourceInfoModel, "resourceType")))))
                //resource name below icon
                .add(new Label("resourceName", new PropertyModel<>(resourceInfoModel, "fileName")))
        );

        resourceInfo
                .add(new PIDLinkLabel("pidLabel", linkModel, Model.of(PIDContext.RESOURCE), PID_LABEL_TEXT_LENGTH)
                        .add(BooleanVisibilityBehavior.visibleOnTrue(isPidModel)));

        // Resource info gets async update to resolve any handle to a file name
        resourceInfo.add(new LazyResourceInfoUpdateBehavior(resolvingResourceStringConverter, resourceInfoModel) {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(resourceInfo);
            }
        });
        final AjaxFallbackLink showResourcesLink = new AjaxFallbackLink("showResources") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                switchToTab(RESOURCES_SECTION, target);
            }
        };

        final WebMarkupContainer availabilityWarning = new ResourceAvailabilityWarningBadge("availabilityWarning", resourceInfoModel);

        final PropertyModel<Boolean> availabilityWarningModel = new PropertyModel<>(resourceInfoModel, "availabilityWarning");
        showResourcesLink
                .add(new WebMarkupContainer("infoSign")
                        .add(BooleanVisibilityBehavior.visibleOnFalse(availabilityWarningModel)))
                .add(availabilityWarning
                        .add(BooleanVisibilityBehavior.visibleOnTrue(availabilityWarningModel)));
        resourceInfo.add(showResourcesLink);

        resourceInfo.setOutputMarkupId(true);
        return resourceInfo;
    }

    private Component createMultipleResourceLink(String id) {
        return new WebMarkupContainer(id)
                .add(new AjaxFallbackLink("showResources") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        switchToTab(RESOURCES_SECTION, target);
                    }
                }.add(new Label("resourcesCount", new PropertyModel<String>(resourcesModel, "size"))));
    }

    protected abstract void switchToTab(String tab, AjaxRequestTarget target);

    @Override
    public void detachModels() {
        super.detachModels();
        resourcesModel.detach();
        resourceInfoModel.detach();
    }

}
