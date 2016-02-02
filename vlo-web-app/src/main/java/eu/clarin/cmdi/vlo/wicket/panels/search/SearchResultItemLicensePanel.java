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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class SearchResultItemLicensePanel extends GenericPanel<SolrDocument> {

    public SearchResultItemLicensePanel(String id, IModel<SolrDocument> model) {
        super(id, model);

        //add 'tags' for all availability values
        final SolrFieldModel<String> availabilityModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_AVAILABILITY);
        add(new ListView<String>("availabilityTag", new CollectionListModel<>(availabilityModel)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item
                        .add(new AttributeAppender("class", item.getModel(), " "))
                        .add(new AttributeModifier("title", item.getModel())); //TODO: use converter to get a friendly name for the tooltip
            }
        });

        //add 'tag' for all licence values
        //TODO: turn into link to licence section of the record page
        final IModel<String> licenseModel = new SolrFieldStringModel(getModel(), FacetConstants.FIELD_LICENSE);
        final WebMarkupContainer licenseTag = new WebMarkupContainer("licenseTag") {
            @Override
            protected void onConfigure() {
                setVisible(licenseModel.getObject() != null);
            }
        };
        
        //TODO: turn into link to licence section of the record page
        add(licenseTag
                .add(new AttributeAppender("class", licenseModel, " ")) //TODO: map to id for license image (via css class)
                .add(new AttributeModifier("title", licenseModel)) //TODO: map to license name
        );
    }

}
