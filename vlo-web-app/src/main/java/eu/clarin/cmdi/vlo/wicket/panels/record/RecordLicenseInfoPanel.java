/*
 * Copyright (C) 2016 CLARIN
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldValueDescriptor;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.PreferredExplicitOrdering;
import eu.clarin.cmdi.vlo.wicket.components.ToggleLink;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.CombinedLicenseTypeAvailabilityModel;
import eu.clarin.cmdi.vlo.wicket.model.ConvertedFieldValueModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.MapValueModel;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.model.StringReplaceModel;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.MapModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class RecordLicenseInfoPanel extends GenericPanel<SolrDocument> {

    public static final String AVAILABILITY_UNSPECIFIED = "UNSPECIFIED";

    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private FieldNameService fieldNameService;

    private final IModel<Collection<String>> licenseTypeModel;
    private final IModel<Collection<String>> availabilityModel;
    private final IModel<Collection<String>> accessInfoModel;
    private final IModel<Collection<String>> licensesModel;

    public RecordLicenseInfoPanel(String id, IModel<SolrDocument> model) {
        super(id, model);
        this.licenseTypeModel = new SolrFieldModel<>(getModel(), fieldNameService.getFieldName(FieldKey.LICENSE_TYPE));
        this.availabilityModel = new SolrFieldModel<>(getModel(), fieldNameService.getFieldName(FieldKey.AVAILABILITY));
        this.accessInfoModel = new SolrFieldModel<>(getModel(), fieldNameService.getFieldName(FieldKey.ACCESS_INFO));
        this.licensesModel = new SolrFieldModel<>(getModel(), fieldNameService.getFieldName(FieldKey.LICENSE));

        add(createInfoContainer("availableInfo"));
        add(createNoInfoContainer("noInfo"));
    }

    private WebMarkupContainer createInfoContainer(final String id) {
        final WebMarkupContainer container = new WebMarkupContainer(id) {
            @Override
            protected void onConfigure() {
                setVisible(isInfoAvailable());
            }

        };

        //add labels for all license values
        container.add(createLicenseItems("license"));
        //add labels for all availability values
        container.add(createAvailabilityItems("availability"));
        container.add(createAccessInfo("accessInfo"));

        container.add(createOriginalContextContainer("originalContext"));

        return container;
    }

    private MarkupContainer createLicenseItems(final String id) {
        //pattern to match non-alphanumeric characters (for replacement in CSS class)
        final IModel<Pattern> nonAlphanumericPatternModel = Model.of(Pattern.compile("[^a-zA-Z0-9]"));

        return new ListView<String>(id, new CollectionListModel<>(licensesModel)) {
            @Override
            protected void populateItem(final ListItem<String> item) {
                //Model for the license URL: URLs are taken from the license 
                //URL property file. As a fallback, the URI is used as a link
                //if no property has been defined for the license id at hand
                final IModel<String> linkPageModel = new NullFallbackModel(
                        new StringResourceModel("license.url.${}", this, item.getModel()), //see licenseUrls.properties
                        item.getModel());

                //Model for the user friendly name of the license
                final ConvertedFieldValueModel licenseNameModel = new ConvertedFieldValueModel(item.getModel(), fieldNameService.getFieldName(FieldKey.LICENSE));

                //create link to licence page
                item.add(new ExternalLink("licensePage", linkPageModel) {
                    {
                        add(new Label("licenseName", licenseNameModel));
                    }

                    @Override
                    public boolean isEnabled() {
                        return linkPageModel.getObject() != null;
                    }

                });
                //add tooltip for consistency with other legal information (availability)
                item.add(new AttributeModifier("title", licenseNameModel));
                //since value is URI, replace all non-alphanumeric characters with underscore for the CSS class
                item.add(new AttributeAppender("class",
                        new StringReplaceModel(item.getModel(), nonAlphanumericPatternModel, Model.of("_")), " "));
            }
        };
    }

    private MarkupContainer createAvailabilityItems(final String id) {
        //model will be used to fetch availability descriptions
        final IModel<Map<String, FieldValueDescriptor>> descriptorsModel
                = new MapModel<>(ImmutableMap.copyOf(FieldValueDescriptor.toMap(vloConfig.getAvailabilityValues())));

        //define the order for availability values
        final Ordering<String> availabilityOrder = new PreferredExplicitOrdering(
                //extract the 'primary' availability values from the configuration
                FieldValueDescriptor.valuesList(vloConfig.getAvailabilityValues()));

        return new ListView<String>(id, new CollectionListModel<>(new CombinedLicenseTypeAvailabilityModel(licenseTypeModel, availabilityModel), availabilityOrder)) {
            @Override
            protected void populateItem(ListItem<String> item) {

                //human friendly version of availability value
                final IModel<String> titleModel = new ConvertedFieldValueModel(item.getModel(), fieldNameService.getFieldName(FieldKey.AVAILABILITY));

                //descriptor model for the availability value - with fallback to 'plain' (converted) value
                final IModel<FieldValueDescriptor> descriptorModel = new MapValueModel<>(descriptorsModel, item.getModel());
                final IModel<String> descriptionModel = new PropertyModel<>(descriptorModel, "description");
                final IModel<String> descriptionFallbackModel = new NullFallbackModel(descriptionModel, titleModel);

                item.add(new Label("availabilityDescription", descriptionFallbackModel));
                item.add(new AttributeAppender("title", titleModel));
                item.add(new AttributeAppender("class", item.getModel(), " "));
            }
        };
    }

    private MarkupContainer createAccessInfo(String id) {
        final WebMarkupContainer accessInfoContainer = new WebMarkupContainer(id) {

            @Override
            protected void onConfigure() {
                setVisible(accessInfoModel.getObject() != null);
            }
        };
        accessInfoContainer.setOutputMarkupId(true);

        // add a toggler for raw 'access info'
        final IModel<Boolean> showDetailsModel = Model.of(Boolean.FALSE);
        accessInfoContainer.add(new ToggleLink("accessInfoToggle",
                showDetailsModel,
                Model.of("Show all available licence/availabilty information"),
                Model.of("Hide detailed licence/availabilty information")) {
            @Override
            protected void onClick(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(accessInfoContainer);
                }
            }

            @Override
            protected void onConfigure() {
                //if availability and license are both null, disable toggling
                setVisible(availabilityModel.getObject() != null || licensesModel.getObject() != null);
            }

        });

        //add a container-wrapped (for toggling) list of 'access info' items
        accessInfoContainer.add(new WebMarkupContainer("accessInfoTable") {
            @Override
            protected void onConfigure() {
                setVisible(showDetailsModel.getObject()
                        //if availability and license are both null, always display
                        || (availabilityModel.getObject() == null && licensesModel.getObject() == null)
                );
            }
        }.add(new ListView<String>("accessInfoItem", new CollectionListModel<>(accessInfoModel)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("accessInfoValue", item.getModel()));
            }
        }));
        return accessInfoContainer;
    }

    private WebMarkupContainer createNoInfoContainer(final String id) {
        final WebMarkupContainer container = new WebMarkupContainer(id) {
            @Override
            protected void onConfigure() {
                setVisible(!isInfoAvailable());
            }
        };
        container.add(createOriginalContextContainer("originalContext"));
        return container;
    }

    public MarkupContainer createOriginalContextContainer(final String id) {
        // get landing page from document
        final SolrFieldStringModel valueModel = new SolrFieldStringModel(getModel(), fieldNameService.getFieldName(FieldKey.LANDINGPAGE));
        // wrap in model that transforms handle links
        final IModel<String> landingPageHrefModel = new HandleLinkModel(valueModel);

        //create container
        final MarkupContainer originalContext = new WebMarkupContainer(id) {

            @Override
            protected void onConfigure() {
                setVisible(landingPageHrefModel.getObject() != null);
            }
        };

        // add landing page link
        originalContext.add(new ExternalLink("landingPage", landingPageHrefModel));

        return originalContext;
    }

    private boolean isInfoAvailable() {
        final Collection<String> availability = availabilityModel.getObject();
        return accessInfoModel.getObject() != null || licensesModel.getObject() != null
                || (availability != null && !availability.contains(AVAILABILITY_UNSPECIFIED));
    }

}
