/*
 * Copyright (C) 2019 CLARIN
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

import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import eu.clarin.cmdi.vlo.wicket.LazyResourceInfoUpdateBehavior;
import eu.clarin.cmdi.vlo.wicket.components.LanguageResourceSwitchboardLink;
import eu.clarin.cmdi.vlo.wicket.components.PIDLinkLabel;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeIcon;
import eu.clarin.cmdi.vlo.wicket.model.IsPidModel;
import eu.clarin.cmdi.vlo.wicket.model.PIDContext;
import eu.clarin.cmdi.vlo.wicket.model.ResolvingLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.panels.BootstrapDropdown;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * An item in the resources links and details table
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @see ResourceLinksPanel
 */
public class ResourceLinksPanelItem extends GenericPanel<ResourceInfo> {

    private final DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.UK);

    @SpringBean
    private PiwikConfig piwikConfig;
    @SpringBean(name = "resolvingResourceStringConverter")
    private ResourceStringConverter resolvingResourceStringConverter;
    @SpringBean
    private FieldNameService fieldNameService;

    private final IModel<SolrDocument> documentModel;
    private final ResourceInfoModel resourceInfoModel;
    private final IModel<Boolean> itemDetailsShownModel;

    public ResourceLinksPanelItem(String id, ResourceInfoModel resourceInfoModel, IModel<SolrDocument> documentModel, IModel<Boolean> detailsVisibleModel) {
        super(id, resourceInfoModel);
        this.resourceInfoModel = resourceInfoModel;
        this.documentModel = documentModel;
        this.itemDetailsShownModel = detailsVisibleModel;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        setDefaultModel(new CompoundPropertyModel<>(resourceInfoModel));

        //basic info for items - always shown
        add(createInfoColumns("itemColumns"));

        //details for item - only shown if toggled on
        add(createDetailsColumns("detailsColumns")
                .add(BooleanVisibilityBehavior.visibleOnTrue(itemDetailsShownModel)));
    }

    private MarkupContainer createInfoColumns(String id) {
        final MarkupContainer columns = new WebMarkupContainer(id);
        // Resource type icon
        columns.add(new ResourceTypeIcon("resourceTypeIcon", new PropertyModel(resourceInfoModel, "resourceType")));

        // Resource link (and/or link label)
        // Create link that will show the resource when clicked
        final IModel<String> linkModel = ResolvingLinkModel.modelFor(resourceInfoModel, documentModel);
        final ExternalLink link = new ResourceExternalLink("showResource", linkModel, linkModel);

        // set the file name as the link's text content
        link.add(new Label("fileName", new PropertyModel(resourceInfoModel, "fileName")));

        // make the link update via AJAX with resolved location (in case of handle)
        if (resolvingResourceStringConverter.getResolver() != null && resolvingResourceStringConverter.getResolver().canResolve(linkModel.getObject())) {
            resolvingResourceStringConverter.doPreflight(linkModel.getObject());
            link.add(new LazyResourceInfoUpdateBehavior(resolvingResourceStringConverter, resourceInfoModel) {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(link);
                }
            });
        }

        link.setOutputMarkupId(true);
        columns.add(link);

        // pid label
        columns.add(new PIDLinkLabel("pidLabel", linkModel, Model.of(PIDContext.RESOURCE))
                //make compact
                .setHideLabel(true)
                //show only if pid
                .add(BooleanVisibilityBehavior.visibleOnTrue(new IsPidModel(linkModel))));

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

        // toggle details option
        columns.add(new ResourceDetailsToggleLink("details", new PropertyModel<>(resourceInfoModel, "href"))
                .add(new WebMarkupContainer("show").add(BooleanVisibilityBehavior.visibleOnFalse(itemDetailsShownModel)))
                .add(new WebMarkupContainer("hide").add(BooleanVisibilityBehavior.visibleOnTrue(itemDetailsShownModel)))
        );

        columns.add(createOptionsDropdown(linkModel, resourceInfoModel));

        final IModel<Boolean> availabilityWarningModel = new PropertyModel<>(resourceInfoModel, "availabilityWarning");
        final IModel<Boolean> restrictedAccessWarningModel = new PropertyModel<>(resourceInfoModel, "restrictedAccessWarning");
        final Component availabilityWarningDetailsLink = new ResourceDetailsToggleLink("availabilityWarningDetailsLink", new PropertyModel<>(resourceInfoModel, "href"))
                .add(new WebMarkupContainer("restrictedIcon")
                        .add(BooleanVisibilityBehavior.visibleOnTrue(restrictedAccessWarningModel)))
                .add(new WebMarkupContainer("unavailableIcon")
                        .add(BooleanVisibilityBehavior.visibleOnFalse(restrictedAccessWarningModel)))
                .add(BooleanVisibilityBehavior.visibleOnTrue(availabilityWarningModel))
                .add(new AttributeModifier("title", new AbstractReadOnlyModel() {
                    @Override
                    public Object getObject() {
                        if (restrictedAccessWarningModel.getObject()) {
                            return "Authentication and/or special permissions may be required in order to access the resource. Click to see details.";
                        } else {
                            return "The resource may not be available at this location. Click to see details.";
                        }
                    }
                }));

        columns.add(availabilityWarningDetailsLink);

        return columns;
    }

    protected Component createOptionsDropdown(final IModel<String> linkModel, final ResourceInfoModel resourceInfoModel) {
        final List<BootstrapDropdown.DropdownMenuItem> options = createDropDownOptions(linkModel, resourceInfoModel);

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

    private List<BootstrapDropdown.DropdownMenuItem> createDropDownOptions(final IModel<String> linkModel, final ResourceInfoModel resourceInfoModel) {
        return Lists.newArrayList(new BootstrapDropdown.DropdownMenuItem("Process with Language Resource Switchboard", "glyphicon glyphicon-open-file") {
            @Override
            protected Link getLink(String id) {
                final IModel<Collection<Object>> languageValuesModel
                        = new SolrFieldModel<>(documentModel, fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE));

                final Link link = new LanguageResourceSwitchboardLink(id, linkModel, languageValuesModel, resourceInfoModel);

                if (piwikConfig.isEnabled()) {
                    link.add(createLrsActionTrackingBehavior(resourceInfoModel));
                }
                return link;
            }
        }
        );
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

    private Component createDetailsColumns(String id) {
        final WebMarkupContainer detailsContainer = new WebMarkupContainer(id);
        detailsContainer.add(new Label("mimeType"));
        detailsContainer.add(new Label("href"));
        detailsContainer.add(createLinkCheckingResult("linkCheckingResult", resourceInfoModel));
        return detailsContainer;
    }

    private Component createLinkCheckingResult(String id, ResourceInfoModel resourceInfoModel) {
        final IModel<Boolean> knownAvailabilityModel = new PropertyModel<>(resourceInfoModel, "availabilityKnown");
        final IModel<Boolean> availabilityWarningModel = new PropertyModel<>(resourceInfoModel, "availabilityWarning");
        return new WebMarkupContainer(id)
                .add(new WebMarkupContainer("unknownStatusDetails")
                        .add(BooleanVisibilityBehavior.visibleOnFalse(knownAvailabilityModel)))
                .add(new WebMarkupContainer("availableStatusDetails")
                        .add(BooleanVisibilityBehavior.visibleOnTrue(new AbstractReadOnlyModel<Boolean>() {
                            @Override
                            public Boolean getObject() {
                                final ResourceInfo info = resourceInfoModel.getObject();
                                return info.getAvailabilityKnown() && !info.getAvailabilityWarning();
                            }
                        })))
                .add(new Label("unavailableStatusDetail", new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        return Optional.ofNullable(resourceInfoModel.getObject().getStatus())
                                .map(statusCode -> {
                                    if (statusCode <= 0) {
                                        return "unknown";
                                    } else {
                                        final String message;
                                        if (resourceInfoModel.getObject().getRestrictedAccessWarning()) {
                                            message = "access to the resource is restricted";
                                        } else {
                                            message = "the resource is unavailable";
                                        }
                                        return String.format("%s (%d %s)",
                                                message,
                                                statusCode,
                                                //look up reason phrase (e.g. Not Found) for code
                                                Optional.ofNullable(Response.Status.fromStatusCode(statusCode))
                                                        .map(Response.Status::getReasonPhrase)
                                                        .orElse("-"));
                                    }
                                })
                                .orElse("undetermined");
                    }
                })
                        .add(BooleanVisibilityBehavior.visibleOnTrue(availabilityWarningModel)))
                .add(new Label("unavailableStatusMessage", new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        if (resourceInfoModel.getObject().getRestrictedAccessWarning()) {
                            return "Authentication and/or special permissions may be required in order to access the resource at this location.";
                        } else {
                            return "The resource may not be available at this location.";
                        }
                    }

                })
                        .add(BooleanVisibilityBehavior.visibleOnTrue(availabilityWarningModel)))
                .add(new Label("lastCheckTime", new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        Optional<Long> lastCheckTimestamp = Optional.ofNullable(resourceInfoModel.getObject().getLastCheckTimestamp());
                        return lastCheckTimestamp.map(timestamp -> {
                            final Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(timestamp);
                            return dateFormatter.format(calendar.getTime());
                        }).orElse("unknown");
                    }

                })
                        .add(BooleanVisibilityBehavior.visibleOnTrue(knownAvailabilityModel)))
                .add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        final ResourceInfo info = resourceInfoModel.getObject();
                        if (info != null) {
                            if (info.getAvailabilityKnown() && info.getAvailabilityWarning()) {
                                if (info.getRestrictedAccessWarning()) {
                                    return "panel-warning";
                                } else {
                                    return "panel-danger";
                                }
                            }
                        }
                        return "panel-default";
                    }
                }, " "));
    }

    private class ResourceDetailsToggleLink extends AjaxFallbackLink<String> {

        public ResourceDetailsToggleLink(String id, IModel<String> idModel) {
            super(id, idModel);
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            final String id = getModel().getObject();
            onDetailsToggleClick(id, target);
        }
    }

    protected void onDetailsToggleClick(String id, AjaxRequestTarget target) {

    }

    /**
     * External link for resources. Ajax indicator aware so that an indicator is
     * shown while resolving PID link (see
     * {@link LazyResourceInfoUpdateBehavior})
     */
    private static class ResourceExternalLink extends ExternalLink implements IAjaxIndicatorAware {

        private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();
        private final IModel<String> linkModel;

        public ResourceExternalLink(String id, IModel<String> href, IModel<String> linkModel) {
            super(id, href);
            this.linkModel = linkModel;
            add(indicatorAppender);
        }

        @Override
        protected void onConfigure() {
            super.onConfigure();
            //hide if no absolute link could be resolved for the resource (see label below for fallback)
            setVisible(linkModel.getObject() != null);
        }

        @Override
        public String getAjaxIndicatorMarkupId() {
            return indicatorAppender.getMarkupId();
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        documentModel.detach();
        itemDetailsShownModel.detach();
    }

}
