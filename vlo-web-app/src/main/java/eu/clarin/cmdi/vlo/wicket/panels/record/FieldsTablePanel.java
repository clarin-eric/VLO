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
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.DocumentField;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.components.FieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.components.SmartLinkFieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldDescriptionModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.basic.SmartLinkMultiLineLabel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
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
    private final static Collection<String> SMART_LINK_FIELDS
            = ImmutableSet.of(
                    FacetConstants.FIELD_DESCRIPTION,
                    FacetConstants.FIELD_LANDINGPAGE,
                    FacetConstants.FIELD_COMPLETE_METADATA,
                    FacetConstants.FIELD_SELF_LINK
            );

    @SpringBean
    private VloConfig vloConfig;
    @SpringBean(name = "queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;

    public FieldsTablePanel(String id, IDataProvider<DocumentField> fieldProvider) {
        super(id);
        add(new DataView<DocumentField>("documentField", fieldProvider) {

            @Override
            protected void populateItem(final Item<DocumentField> item) {
                final IModel<DocumentField> fieldModel = item.getModel();
                final PropertyModel<String> fieldNameModel = new PropertyModel<String>(fieldModel, "fieldName");
                final SolrFieldNameModel friendlyFieldNameModel = new SolrFieldNameModel(fieldNameModel);
                final Label fieldName = new Label("fieldName", friendlyFieldNameModel);
                item.add(fieldName);
                fieldName.add(new AttributeAppender("title", new SolrFieldDescriptionModel(fieldNameModel)));
                final PropertyModel<List> valuesModel = new PropertyModel<List>(fieldModel, "values");
                item.add(new ListView("values", valuesModel) {

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

    private Component createValueLabel(String id, final IModel<String> facetNameModel, final IModel<String> valueModel) {
        final String fieldName = facetNameModel.getObject();

        if (FacetConstants.FIELD_LANGUAGE_CODE.equals(facetNameModel.getObject())) {
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
        return new Link(id) {

            @Override
            public void onClick() {
                final FacetSelection facetSelection = new FacetSelection(Collections.singleton(valueModel.getObject().toString()));
                final QueryFacetsSelection selection = new QueryFacetsSelection(Collections.singletonMap(facetNameModel.getObject(), facetSelection));
                setResponsePage(FacetedSearchPage.class, paramsConverter.toParameters(selection));
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // only show for facet fields
                setVisible(isShowFacetSelectLinks()
                        && vloConfig.getAllFacetFields().contains(facetNameModel.getObject()));
            }

        };
    }

    protected boolean isShowFacetSelectLinks() {
        return true;
    }
    
//    re-enable for 'fancy' popups for the field descriptions
//    @Override
//    public void renderHead(IHeaderResponse response) {
//        // JQuery UI for tooltips
//        response.render(CssHeaderItem.forReference(JavaScriptResources.getJQueryUICSS()));
//        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getFieldsTableJS()));
//    }

}
