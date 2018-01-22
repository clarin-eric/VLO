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

import eu.clarin.cmdi.vlo.wicket.components.LanguageInfoLink;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.DocumentField;
import eu.clarin.cmdi.vlo.wicket.components.FacetSelectLink;
import eu.clarin.cmdi.vlo.wicket.components.FieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.components.SmartLinkFieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.OrderedListModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldDescriptionModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.basic.SmartLinkMultiLineLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class FieldsTablePanel extends Panel {

    /**
     * List of fields that should be rendered unescaped, {@literal i.e.} HTML
     * contained in the field should be preserved
     */
    private final static Collection<String> UNESCAPED_VALUE_FIELDS
            = Collections.emptySet(); // ImmutableSet.of(FacetConstants.FIELD_LANGUAGE_CODE);

    /**
     * List of fields that should be rendered in a
     * {@link SmartLinkMultiLineLabel}, which detects URLs and turns them into
     * links
     */


    @SpringBean
    private VloConfig vloConfig;
    @SpringBean(name = "fieldValueSorters")
    private Map<String, Ordering> fieldValueOrderingMap;
    @SpringBean 
    private FieldNameService fieldNameService;
    
    private final Collection<String> SMART_LINK_FIELDS;

    private IDataProvider<DocumentField> fieldProvider;

    public FieldsTablePanel(String id, IDataProvider<DocumentField> fieldProvider) {
        super(id);
        
        ImmutableSet.Builder<String> imb = ImmutableSet.builder();
        
        if(fieldNameService.getFieldName(FieldKey.DESCRIPTION) != null)
        	imb.add(fieldNameService.getFieldName(FieldKey.DESCRIPTION));
        if(fieldNameService.getFieldName(FieldKey.LANDINGPAGE) != null)
        	imb.add(fieldNameService.getFieldName(FieldKey.LANDINGPAGE));
        if(fieldNameService.getFieldName(FieldKey.SEARCHPAGE) != null)
        	imb.add(fieldNameService.getFieldName(FieldKey.SEARCHPAGE));
        if(fieldNameService.getFieldName(FieldKey.COMPLETE_METADATA) != null)
        	imb.add(fieldNameService.getFieldName(FieldKey.COMPLETE_METADATA));
        if(fieldNameService.getFieldName(FieldKey.SELF_LINK) != null)
        	imb.add(fieldNameService.getFieldName(FieldKey.SELF_LINK));
        

        this.SMART_LINK_FIELDS = imb.build();
        
        this.fieldProvider = fieldProvider;
        
    }

    private IModel<List<String>> createOrderedFieldValuesModel(final IModel<List<String>> valuesModel, final IModel<String> fieldNameModel) {

        final IModel<Ordering<String>> orderingModel = new LoadableDetachableModel<Ordering<String>>() {
            @Override
            protected Ordering<String> load() {
                return fieldValueOrderingMap.get(fieldNameModel.getObject());
            }

        };
        return new OrderedListModel<>(valuesModel, orderingModel);
    }

    private Component createValueLabel(String id, final IModel<String> facetNameModel, final IModel<String> valueModel) {
        final String fieldName = facetNameModel.getObject();

        if (fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE).equals(facetNameModel.getObject())) {
            return new LanguageInfoLink(id, valueModel, facetNameModel);
        } else if (SMART_LINK_FIELDS.contains(fieldName)) {
            // create label that generates links
            return new SmartLinkFieldValueLabel(id, new HandleLinkModel(valueModel), facetNameModel);
        } else {
            // add a label for the facet value
            final Label fieldLabel = new FieldValueLabel(id, valueModel, facetNameModel);

            // some selected fields may have HTML that needs to be preserved...
            fieldLabel.setEscapeModelStrings(!UNESCAPED_VALUE_FIELDS.contains(fieldName));
            return fieldLabel;
        }
    }

    private Link createFacetSelectLink(String id, final IModel<String> facetNameModel, final IModel valueModel) {
        return new FacetSelectLink(id, valueModel, facetNameModel) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // only show for facet fields
                setVisible(isShowFacetSelectLinks()
                        && vloConfig.getFacetsInSearch().contains(facetNameModel.getObject()));
            }
        };
    }

    protected boolean isShowFacetSelectLinks() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.MarkupContainer#onInitialize()
     */
    @Override
    protected void onInitialize() {
        // TODO Auto-generated method stub
        super.onInitialize();
        
        add(new DataView<DocumentField>("documentField", this.fieldProvider) {

            @Override
            protected void populateItem(final Item<DocumentField> item) {
                final IModel<DocumentField> fieldModel = item.getModel();
                final PropertyModel<String> fieldNameModel = new PropertyModel<>(fieldModel, "fieldName");
                final SolrFieldNameModel friendlyFieldNameModel = new SolrFieldNameModel(fieldNameModel);
                final Label fieldName = new Label("fieldName", friendlyFieldNameModel);
                item.add(fieldName);
                fieldName.add(new AttributeAppender("title", new SolrFieldDescriptionModel(fieldNameModel)));

                //model of field values
                final PropertyModel<List<String>> valuesModel = new PropertyModel<>(fieldModel, "values");

                //wrapper for sorted model (if ordering is available)
                final IModel<List<String>> sortedValuesModel = createOrderedFieldValuesModel(valuesModel, fieldNameModel);

                item.add(new ListView("values", sortedValuesModel) {

                    @Override
                    protected void populateItem(final ListItem fieldValueItem) {
                        // add a label that holds the field value
                        fieldValueItem.add(createValueLabel("value", fieldNameModel, fieldValueItem.getModel()));
                        // add a link for selecting the value in the search
                        fieldValueItem.add(createFacetSelectLink("facetSelect", fieldNameModel, fieldValueItem.getModel()));
                    }
                });

                // if field has multiple values, set 'multiple' class on markup element
                item.add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        if (valuesModel.getObject().size() > 1) {
                            return "multiplevalues";
                        } else {
                            return null;
                        }
                    }
                }));
            }
        });

    }

//    re-enable for 'fancy' popups for the field descriptions
//    @Override
//    public void renderHead(IHeaderResponse response) {
//        // JQuery UI for tooltips
//        response.render(CssHeaderItem.forReference(JavaScriptResources.getJQueryUICSS()));
//        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getFieldsTableJS()));
//    }
}
