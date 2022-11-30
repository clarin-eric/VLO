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

import com.google.common.collect.ImmutableList;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.components.LanguageResourceSwitchboardLink;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.panels.BootstrapDropdown;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.lang.StringEscapeUtils;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.springframework.web.util.JavaScriptUtils;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
class ResourceLinkOptionsDropdown extends BootstrapDropdown {

    /**
     * Placeholders: URL, title
     */
    private static final String ADD_TO_VCR_QUEUE_JS_TEMPLATE = "if (window.vcrIntegration) { window.vcrIntegration.addToQueue('%s', '%s'); }";

    @SpringBean
    private PiwikConfig piwikConfig;

    @SpringBean
    private VloConfig vloConfig;

    @SpringBean
    private FieldNameService fieldNameService;

    private final IModel<SolrDocument> documentModel;
    private final IModel<ResourceInfo> resourceInfoModel;
    private final IModel<String> linkModel;

    public ResourceLinkOptionsDropdown(String id, IModel<SolrDocument> documentModel, final IModel<String> linkModel, final IModel<ResourceInfo> resourceInfoModel) {
        super(id);

        this.documentModel = documentModel;
        this.linkModel = linkModel;
        this.resourceInfoModel = resourceInfoModel;

        setOutputMarkupId(true);
    }

    @Override
    protected final void onInitialize() {
        final ImmutableList.Builder<DropdownMenuItem> optionsBuilder = ImmutableList.<DropdownMenuItem>builder();
        createDropdownOptions(optionsBuilder);

        setModel(new ListModel<>(optionsBuilder.build()));

        add(new Behavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                response.render(JavaScriptReferenceHeaderItem.forReference(JavaScriptResources.getSwitchboardIntegrationJs()));
                response.render(JavaScriptHeaderItem.forScript(String.format("$(document).on('shown.bs.dropdown', '#%s',", JavaScriptUtils.javaScriptEscape(component.getMarkupId()))
                        + "    function() {"
                        + String.format(
                                " switchboardPreflight('%s', '#%s');",
                                JavaScriptUtils.javaScriptEscape(resourceInfoModel.getObject().getHref()),
                                JavaScriptUtils.javaScriptEscape(component.getMarkupId()))
                        + "    });", "onLinkDropdownShown" + component.getMarkupId()));
            }

        });

        super.onInitialize();
    }

    protected void createDropdownOptions(ImmutableList.Builder<DropdownMenuItem> listBuilder) {
        listBuilder.add(createSwitchboardItem());

        if (!Strings.isEmpty(vloConfig.getVcrSubmitEndpoint())) {
            listBuilder.add(createVcrItem());
        }
    }

    private DropdownMenuItem createSwitchboardItem() {
        final Model<String> switchboardItemLabelModel = Model.of("Process with Language Resource Switchboard");
        final DropdownMenuItem switchboardItem = new BootstrapDropdown.DropdownMenuItem(switchboardItemLabelModel, Model.of("glyphicon glyphicon-open-file")) {
            @Override
            protected Link getLink(String id) {
                return getSwitchboardLink(id);
            }
        };
        return switchboardItem;
    }

    private Link getSwitchboardLink(String id) {
        final IModel<Collection<Object>> languageValuesModel = new SolrFieldModel<>(documentModel, fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE));
        final Link link = new LanguageResourceSwitchboardLink(id, linkModel, languageValuesModel, resourceInfoModel);
        if (piwikConfig.isEnabled()) {
            link.add(createPiwikActionTrackingBehavior(resourceInfoModel, PiwikEventConstants.PIWIK_EVENT_CATEGORY_LRS, PiwikEventConstants.PIWIK_EVENT_ACTION_LRS_PROCESSRESOURCE));
        }
        link.add(new AttributeAppender("class", "resourceDropdownSwitchboardItem"));
        return link;
    }

    private DropdownMenuItem createVcrItem() {
        return new BootstrapDropdown.DropdownMenuItem(Model.of("Queue for submission to a Virtual Collection"), Model.of("glyphicon glyphicon-plus")) {
            @Override
            protected Link getLink(String id) {
                return getVcrQueueLink(id);
            }
        };
    }

    public Link getVcrQueueLink(String id) {
        final IModel<String> urlModel = resourceInfoModel.map(ResourceInfo::getHref);
        final IModel<String> titleModel = resourceInfoModel.map(ResourceInfo::getFileName);
        final AjaxFallbackLink<Void> link = new AjaxFallbackLink<Void>(id) {
            @Override
            public void onClick(Optional<AjaxRequestTarget> ajaxRequestTarget) {
                ajaxRequestTarget.ifPresent(target -> {
                    target.appendJavaScript(createAddToVcrQueueJs(urlModel, titleModel));
                });
            }
        };
        if (piwikConfig.isEnabled()) {
            link.add(createPiwikActionTrackingBehavior(resourceInfoModel, PiwikEventConstants.PIWIK_EVENT_CATEGORY_VCR, PiwikEventConstants.PIWIK_EVENT_ACTION_VCR_ADD_TO_QUEUE));
        }
        return link;
    }

    private static String createAddToVcrQueueJs(IModel<String> linkModel, IModel<String> fileNameModel) {
        return String.format(ADD_TO_VCR_QUEUE_JS_TEMPLATE,
                // url
                StringEscapeUtils.escapeJavaScript(linkModel.getObject()),
                //title
                StringEscapeUtils.escapeJavaScript(fileNameModel.getObject())
        );
    }

    private AjaxPiwikTrackingBehavior.EventTrackingBehavior createPiwikActionTrackingBehavior(final IModel<ResourceInfo> resourceInfoModel, String category, String action) {
        final AjaxPiwikTrackingBehavior.EventTrackingBehavior eventBehavior = new AjaxPiwikTrackingBehavior.EventTrackingBehavior("click", category, action) {
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

    @Override
    public void detachModels() {
        super.detachModels();
        documentModel.detach();
        resourceInfoModel.detach();
        linkModel.detach();
    }

}
