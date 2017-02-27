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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.HighlightSearchTermBehavior;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeGlyphicon;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel that shows the "basic" (non-technical) property fields of a document
 * through a {@link FieldsTablePanel} with a {@link FieldFilter}; for resources
 * with a single resource, some information about this single resource is also
 * shown.
 *
 * @author twagoo
 */
public class RecordDetailsPanel extends GenericPanel<SolrDocument> {

    @SpringBean(name = "basicPropertiesFilter")
    private FieldFilter basicPropertiesFilter;
    @SpringBean(name = "documentFieldOrder")
    private List<String> fieldOrder;
    @SpringBean(name = "resourceStringConverter")
    private ResourceStringConverter resourceStringConverter;

    private final SolrFieldModel<String> resourcesModel;
    private ResourceInfoModel resourceInfoModel;

    public RecordDetailsPanel(String id, IModel<SolrDocument> model) {
        super(id, model);

        resourcesModel = new SolrFieldModel<>(model, FacetConstants.FIELD_RESOURCE);
        resourceInfoModel = new ResourceInfoModel(resourceStringConverter, new SolrFieldStringModel(model, FacetConstants.FIELD_RESOURCE));

        // Fields table
        add(new FieldsTablePanel("fieldsTable", new DocumentFieldsProvider(getModel(), basicPropertiesFilter, fieldOrder))
                .add(new HighlightSearchTermBehavior())
                .add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        // leave space for resource info iff there is exactly one resource
                        // using boostrap columns; see https://getbootstrap.com/css/#grid
                        return resourcesModel.getObject().size() == 1 ? "col-sm-9" : "col-xs-12";
                    }
                }))
        );

        // Resource info for single resource (should not appear if there are more or fewer resources)
        // TODO: turn into link
        add(new WebMarkupContainer("resourceInfo") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                // show resource info iff there is exactly one resource
                setVisible(resourcesModel.getObject().size() == 1);
            }

        }.add(new ResourceTypeGlyphicon("resourceTypeIcon", new PropertyModel(resourceInfoModel, "resourceType")))
                .add(new Label("resourceName", new PropertyModel<String>(resourceInfoModel, "fileName"))));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
    }

    @Override
    public void detachModels() {
        super.detachModels();
        resourcesModel.detach();
        resourceInfoModel.detach();
    }

}
