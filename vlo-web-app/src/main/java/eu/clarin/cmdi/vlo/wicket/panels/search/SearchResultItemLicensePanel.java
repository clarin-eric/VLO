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

import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.ConvertedFieldValueModel;
import eu.clarin.cmdi.vlo.wicket.model.FormattedStringModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.StringReplaceModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import java.util.regex.Pattern;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class SearchResultItemLicensePanel extends GenericPanel<SolrDocument> {

    private final IModel<SearchContext> searchContextModel;

    public SearchResultItemLicensePanel(String id, final IModel<SolrDocument> model, final IModel<SearchContext> searchContextModel, final Ordering<String> availabilityOrder) {
        super(id, model);
        this.searchContextModel = searchContextModel;

        //add 'tags' for all availability values
        final SolrFieldModel<String> availabilityModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_AVAILABILITY);
        add(new ListView<String>("availabilityTag", new CollectionListModel<>(availabilityModel, availabilityOrder)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                // add link to record
                item.add(createLink("recordLink")
                        .add(new AttributeAppender("class", item.getModel(), " "))
                        .add(new AttributeModifier("title",
                                new FormattedStringModel(Model.of("Availability: %s"),
                                        new ConvertedFieldValueModel(item.getModel(), FacetConstants.FIELD_AVAILABILITY))))
                );
            }
        });

        //add 'tags' for all licence values
        final SolrFieldModel<String> licensesModel = new SolrFieldModel<>(getModel(), FacetConstants.FIELD_LICENSE);
        //pattern to match non-alphanumeric characters (for replacement in CSS class)
        final IModel<Pattern> nonAlphanumericPatternModel = Model.of(Pattern.compile("[^a-zA-Z0-9]"));

        add(new ListView<String>("licenseTag", new CollectionListModel<>(licensesModel)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                // add link to record
                item.add(createLink("recordLink")
                        //add CSS class. Since value is URI, replace all non-alphanumeric characters with underscore
                        .add(new AttributeAppender("class",
                                new StringReplaceModel(item.getModel(), nonAlphanumericPatternModel, Model.of("_")), " "))
                        .add(new AttributeModifier("title",
                                new FormattedStringModel(Model.of("Licence: %s"),
                                        new ConvertedFieldValueModel(item.getModel(), FacetConstants.FIELD_LICENSE))))
                );
            }
        });
    }

    protected WebMarkupContainer createLink(String id) {
        return new RecordPageLink(id, getModel(), searchContextModel, RecordPage.AVAILABILITY_SECTION);
    }

    @Override
    public void detachModels() {
        super.detachModels();
        if (searchContextModel != null) {
            searchContextModel.detach();
        }
    }

}
