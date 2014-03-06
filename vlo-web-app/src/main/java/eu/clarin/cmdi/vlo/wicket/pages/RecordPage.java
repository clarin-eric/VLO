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
package eu.clarin.cmdi.vlo.wicket.pages;

import com.google.common.collect.Sets;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.wicket.components.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import java.io.Serializable;
import java.util.Collection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public class RecordPage extends WebPage {

    /**
     * Fields to be ignored. TODO: read this from config
     */
    private static final Collection<String> IGNORE_FIELDS
            = Sets.newHashSet(
                    FacetConstants.FIELD_FORMAT);
    /**
     * Fields to be included in technical details. TODO: read this from config
     */
    private static final Collection<String> TECHNICAL_FIELDS
            = Sets.newHashSet(
                    FacetConstants.FIELD_ID,
                    FacetConstants.FIELD_DATA_PROVIDER,
                    FacetConstants.FIELD_FORMAT,
                    FacetConstants.FIELD_LANDINGPAGE,
                    FacetConstants.FIELD_SEARCHPAGE,
                    FacetConstants.FIELD_SEARCH_SERVICE,
                    FacetConstants.FIELD_LAST_SEEN);
    private final IModel<QueryFacetsSelection> contextModel;

    public RecordPage(IModel<SolrDocument> documentModel, IModel<QueryFacetsSelection> contextModel) {
        super(documentModel);
        this.contextModel = contextModel;

        add(new SolrFieldLabel("name", documentModel, FacetConstants.FIELD_NAME, "Unnamed record"));
        add(createLandingPageLink("landingPageLink", documentModel));
        add(new FieldsTablePanel("documentProperties", new DocumentFieldsProvider(documentModel, new BasicPropertiesFieldFilter())));
        add(new FieldsTablePanel("technicalProperties", new DocumentFieldsProvider(documentModel, new TechnicalPropertiesFieldFilter())));
    }

    private ExternalLink createLandingPageLink(String id, IModel<SolrDocument> documentModel) {
        final SolrFieldStringModel landingPageHrefModel = new SolrFieldStringModel(documentModel, FacetConstants.FIELD_LANDINGPAGE);
        // add landing page link
        final ExternalLink landingPageLink = new ExternalLink(id, landingPageHrefModel) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(landingPageHrefModel.getObject() != null);
            }

        };
        return landingPageLink;
    }

    @Override
    public void detachModels() {
        super.detachModels();
        // not passed to parent
        contextModel.detach();
    }

    private class BasicPropertiesFieldFilter implements FieldFilter, Serializable {

        @Override
        public boolean allowField(String fieldName) {
            return !(fieldName.startsWith("_")
                    || IGNORE_FIELDS.contains(fieldName)
                    || TECHNICAL_FIELDS.contains(fieldName));
        }
    }

    private class TechnicalPropertiesFieldFilter implements FieldFilter, Serializable {

        @Override
        public boolean allowField(String fieldName) {
            return TECHNICAL_FIELDS.contains(fieldName);
        }
    }

}
