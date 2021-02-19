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
import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.DocumentField;
import eu.clarin.cmdi.vlo.service.FacetConfigurationService;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import eu.clarin.cmdi.vlo.wicket.components.FacetSelectLink;
import eu.clarin.cmdi.vlo.wicket.components.FieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.components.PIDLinkLabel;
import eu.clarin.cmdi.vlo.wicket.components.SmartLinkFieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.model.PIDLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.IsPidModel;
import eu.clarin.cmdi.vlo.wicket.model.OrderedListModel;
import eu.clarin.cmdi.vlo.wicket.model.PIDContext;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoObjectModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldDescriptionModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;

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

    @SpringBean
    private FacetConfigurationService facetsConfig;
    @SpringBean(name = "fieldValueSorters")
    private Map<String, Ordering> fieldValueOrderingMap;
    @SpringBean
    private FieldNameService fieldNameService;

    /**
     * List of fields that should be rendered in a
     * {@link SmartLinkFieldValueLabel}, which detects URLs within the text and turns
     * them into links
     */
    private final Collection<String> SMART_LINK_FIELDS;

    /**
     * List of fields that can be assumed to have a URL value and should be
     * rendered as a link
     */
    private final Collection<String> LINK_FIELDS;

    private IDataProvider<DocumentField> fieldProvider;

    public FieldsTablePanel(String id, IDataProvider<DocumentField> fieldProvider) {
        super(id);

        this.SMART_LINK_FIELDS = Stream.of(
                FieldKey.DESCRIPTION,
                FieldKey.COMPLETE_METADATA
        )
                //existing fields (check) into a set
                .map(fieldNameService::getFieldName).filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

        this.LINK_FIELDS = Stream.of(
                FieldKey.SEARCHPAGE,
                FieldKey.SEARCH_SERVICE,
                FieldKey.SELF_LINK
        )
                //existing fields (check) into a set
                .map(fieldNameService::getFieldName).filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

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
        } else if (fieldNameService.getFieldName(FieldKey.RECORD_PID).equals(facetNameModel.getObject())) {
            return new PIDLinkLabel(id, valueModel, Model.of(PIDContext.RECORD));
        } else if (fieldNameService.getFieldName(FieldKey.LANDINGPAGE).equals(facetNameModel.getObject())) {
            return new LinkLabel(id, new PIDLinkModel(new PropertyModel(new ResourceInfoObjectModel(valueModel), "url")));
        } else if (LINK_FIELDS.contains(fieldName)) {
            return new LinkLabel(id, new PIDLinkModel(valueModel));
        } else if (SMART_LINK_FIELDS.contains(fieldName)) {
            // create label that generates links
            return new SmartLinkFieldValueLabel(id, new PIDLinkModel(valueModel), facetNameModel);
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
                        && facetsConfig.getFacetsInSearch().contains(facetNameModel.getObject()));
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

                item.add(new ListView<>("values", sortedValuesModel) {

                    @Override
                    protected void populateItem(final ListItem<String> fieldValueItem) {
                        // add a label that holds the field value
                        fieldValueItem.add(createValueLabel("value", fieldNameModel, fieldValueItem.getModel()));
                        // add a link for selecting the value in the search
                        fieldValueItem.add(createFacetSelectLink("facetSelect", fieldNameModel, fieldValueItem.getModel()));
                    }
                });

                // if field has multiple values, set 'multiple' class on markup element
                item.add(new AttributeModifier("class", new IModel<>() {

                    @Override
                    public String getObject() {
                        if (valuesModel.getObject().size() > 1) {
                            return "multiplevalues";
                        } else {
                            return null;
                        }
                    }
                }));

                //only show PID line if self link is PID
                if (fieldNameModel.getObject().equals(fieldNameService.getFieldName(FieldKey.RECORD_PID))) {
                    item.add(BooleanVisibilityBehavior.visibleOnTrue(new IsPidModel(new IModel<>() {
                        @Override
                        public String getObject() {
                            if (valuesModel.getObject().size() > 0) {
                                return valuesModel.getObject().get(0);
                            } else {
                                return null;
                            }
                        }
                    })));
                }
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
    private static class LinkLabel extends Label {

        public LinkLabel(String id, IModel<?> model) {
            super(id, model);
            setEscapeModelStrings(false);
        }

        @Override
        public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
            final CharSequence link = Strings.escapeMarkup(getDefaultModelObjectAsString());
            replaceComponentTagBody(markupStream, openTag,
                    "<a href=\"" + link + "\">" + link + "</a>");
        }
    }
}
