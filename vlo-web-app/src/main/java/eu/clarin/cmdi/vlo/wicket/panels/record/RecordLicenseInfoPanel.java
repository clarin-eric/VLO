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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.ConvertedFieldValueModel;
import eu.clarin.cmdi.vlo.wicket.model.FormattedStringModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import java.util.Collection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class RecordLicenseInfoPanel extends GenericPanel<SolrDocument> {

    public static final String AVAILABILITY_UNSPECIFIED = "UNSPECIFIED";

    private final IModel<Collection<String>> availabilityModel;
    private final IModel<Collection<String>> accessInfoModel;
    private final IModel<String> licenseModel;

    public RecordLicenseInfoPanel(String id, IModel<SolrDocument> model) {
        super(id, model);
        this.availabilityModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_AVAILABILITY);
        this.accessInfoModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_ACCESS_INFO);
        this.licenseModel = new SolrFieldStringModel(getModel(), FacetConstants.FIELD_LICENSE);

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

        //add 'tags' for all availability values
        container.add(new ListView<String>("availability", new CollectionListModel<>(availabilityModel)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("availabilityDescription",
                        new FormattedStringModel(Model.of("Availability: %s"),
                                new ConvertedFieldValueModel(item.getModel(), FacetConstants.FIELD_AVAILABILITY)))); //TODO: description instead
                item.add(new AttributeAppender("class", item.getModel(), " "));
            }
        });

//        //add 'tag' for all licence values
//        final WebMarkupContainer licenseTag = new WebMarkupContainer("license") {
//            @Override
//            protected void onConfigure() {
//                setVisible(licenseModel.getObject() != null);
//            }
//        };
//
//        //TODO: turn into link to licence section of the record page
//        add(licenseTag
//                .add(new AttributeAppender("class", licenseModel, " ")) //TODO: map to id for license image (via css class)
//                .add(new AttributeModifier("title", licenseModel)) //TODO: map to license name
//        );

        container.add(createOriginalContextContainer("originalContext"));
        
        return container;
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
        return accessInfoModel.getObject() != null || licenseModel.getObject() != null
                || (availability != null && !availability.contains(AVAILABILITY_UNSPECIFIED));
    }

}
