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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.wicket.components.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.components.ResourceLinksPanel;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class RecordPage extends WebPage {

    @SpringBean(name = "basicPropertiesFilter")
    private FieldFilter basicPropertiesFilter;
    @SpringBean(name = "technicalPropertiesFilter")
    private FieldFilter technicalPropertiesFilter;
    
    private final IModel<QueryFacetsSelection> contextModel;

    public RecordPage(IModel<SolrDocument> documentModel, IModel<QueryFacetsSelection> contextModel) {
        super(documentModel);
        this.contextModel = contextModel;

        add(new SolrFieldLabel("name", documentModel, FacetConstants.FIELD_NAME, "Unnamed record"));
        add(createLandingPageLink("landingPageLink", documentModel));
        add(new FieldsTablePanel("documentProperties", new DocumentFieldsProvider(documentModel, basicPropertiesFilter)));
        add(new ResourceLinksPanel("resources", new SolrFieldModel<String>(documentModel, FacetConstants.FIELD_RESOURCE)));
        add(new FieldsTablePanel("technicalProperties", new DocumentFieldsProvider(documentModel, technicalPropertiesFilter)));
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

}
