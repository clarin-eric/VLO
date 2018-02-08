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

/**
 * Panel that shows the "basic" (non-technical) property fields of a document
 * through a {@link FieldsTablePanel} with a {@link FieldFilter}; for resources
 * with a single resource, some information about this single resource is also
 * shown.
 *
 * @author twagoo
 */
public abstract class RecordDetailsPanel extends GenericPanel<SolrDocument> {
    
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
    private ResourceInfoModel resourceInfoModel;
    
    public RecordDetailsPanel(String id, IModel<SolrDocument> model) {
        super(id, model);
        
        resourcesModel = new SolrFieldModel<>(model, fieldNameService.getFieldName(FieldKey.RESOURCE));
        resourceInfoModel = new ResourceInfoModel(resourceStringConverter, new SolrFieldStringModel(model, fieldNameService.getFieldName(FieldKey.RESOURCE)));

        // Fields table
        add(new FieldsTablePanel("fieldsTable", new DocumentFieldsProvider(getModel(), basicPropertiesFilter, fieldOrder))
                .add(new HighlightSearchTermBehavior())
                .add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        // leave space for resource info iff there is exactly one resource
                        // using boostrap columns; see https://getbootstrap.com/css/#grid
                        return (resourcesModel.getObject() != null
                                && resourcesModel.getObject().size() == 1)
                                ? "col-sm-9"
                                : "col-xs-12";
                    }
                }))
        );
        
        add(createSingleResourceInfo("resourceInfo"));
        
        add(new SimilarDocumentsPanel("similar", getModel()));
    }
    
    private Component createSingleResourceInfo(String id) {
        final ResolvingLinkModel linkModel = ResolvingLinkModel.modelFor(resourceInfoModel, getModel());
        final WebMarkupContainer resourceInfo = new WebMarkupContainer(id) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                // show resource info iff there is exactly one resource and it has a proper link 
                setVisible(
                        resourcesModel.getObject() != null
                        && resourcesModel.getObject().size() == 1
                        && linkModel.getObject() != null);
            }
            
        };

        // Resource info for single resource (should not appear if there are more or fewer resources)
        resourceInfo.add(new ExternalLink("resourceLink", linkModel)
                //resource type icon
                .add(new ResourceTypeIcon("resourceTypeIcon", new PropertyModel<String>(resourceInfoModel, "resourceType"))
                        //with type name tooltip
                        .add(new AttributeModifier("title", new StringResourceModel("resourcetype.${resourceType}.singular", this, resourceInfoModel).setDefaultValue(new PropertyModel(resourceInfoModel, "resourceType")))))
                //resource name below icon
                .add(new Label("resourceName", new PropertyModel<>(resourceInfoModel, "fileName")))
        );

        // Resource info gets async update to resolve any handle to a file name
        resourceInfo.add(new LazyResourceInfoUpdateBehavior(resolvingResourceStringConverter, resourceInfoModel) {
            
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(resourceInfo);
            }
        });
        
        resourceInfo.add(new AjaxFallbackLink("showResources") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                switchToTab(RESOURCES_SECTION, target);
            }
        });
        
        resourceInfo.setOutputMarkupId(true);
        return resourceInfo;
    }
    
    protected abstract void switchToTab(String tab, AjaxRequestTarget target);
    
    @Override
    public void detachModels() {
        super.detachModels();
        resourcesModel.detach();
        resourceInfoModel.detach();
    }
    
}
