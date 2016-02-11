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
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.FieldValueDescriptor;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.components.ToggleLink;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.ConvertedFieldValueModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.MapValueModel;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.panels.TogglePanel;
import java.util.Collection;
import java.util.Map;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
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
import org.apache.wicket.model.util.MapModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class RecordLicenseInfoPanel extends GenericPanel<SolrDocument> {

    public static final String AVAILABILITY_UNSPECIFIED = "UNSPECIFIED";

    @SpringBean
    private VloConfig vloConfig;

    private final IModel<Collection<String>> availabilityModel;
    private final IModel<Collection<String>> accessInfoModel;
    private final IModel<Collection<String>> licensesModel;

    public RecordLicenseInfoPanel(String id, IModel<SolrDocument> model) {
        super(id, model);
        this.availabilityModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_AVAILABILITY);
        this.accessInfoModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_ACCESS_INFO);
        this.licensesModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_LICENSE);

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
        return new ListView<String>(id, new CollectionListModel<>(licensesModel)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                //TODO: add link to licence page
                item.add(new Label("licenseName", new ConvertedFieldValueModel(item.getModel(), FacetConstants.FIELD_LICENSE)));
                item.add(new AttributeAppender("class", item.getModel(), " "));
            }
        };
    }

    private MarkupContainer createAvailabilityItems(final String id) {
        //model will be used to fetch availability descriptions
        final IModel<Map<String, FieldValueDescriptor>> descriptorsModel
                = new MapModel<>(ImmutableMap.copyOf(FieldValueDescriptor.toMap(vloConfig.getAvailabilityValues())));

        return new ListView<String>(id, new CollectionListModel<>(availabilityModel)) {
            @Override
            protected void populateItem(ListItem<String> item) {

                //human friendly version of availability value
                final IModel<String> titleModel = new ConvertedFieldValueModel(item.getModel(), FacetConstants.FIELD_AVAILABILITY);

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

        final IModel<Boolean> showDetailsModel = Model.of(Boolean.FALSE);
        accessInfoContainer.add(new ListView<String>("accessInfoItem", new CollectionListModel<>(accessInfoModel)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("accessInfoValue", item.getModel()));
            }

            @Override
            protected void onConfigure() {
                setVisible(showDetailsModel.getObject());
            }

        });

        accessInfoContainer.add(new ToggleLink("accessInfoToggle", showDetailsModel, Model.of("Show all available information"), Model.of("Hide details")) {
            @Override
            protected void onClick(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(accessInfoContainer);
                }
            }
        });
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
        final SolrFieldStringModel valueModel = new SolrFieldStringModel(getModel(), FacetConstants.FIELD_LANDINGPAGE);
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
