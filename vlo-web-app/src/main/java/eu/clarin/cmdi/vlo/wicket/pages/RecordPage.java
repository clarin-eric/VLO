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
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.pojo.StaticSearchContext;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.panels.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.panels.ResourceLinksPanel;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.model.UrlFromStringModel;
import eu.clarin.cmdi.vlo.wicket.model.XsltModel;
import eu.clarin.cmdi.vlo.wicket.panels.RecordNavigationPanel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class RecordPage extends VloBasePage<SolrDocument> {

    @SpringBean
    private PageParametersConverter<QueryFacetsSelection> selectionParametersConverter;
    @SpringBean(name = "basicPropertiesFilter")
    private FieldFilter basicPropertiesFilter;
    @SpringBean(name = "technicalPropertiesFilter")
    private FieldFilter technicalPropertiesFilter;

    private final IModel<SearchContext> navigationModel;

    public RecordPage(PageParameters params) {
        super(params);
        // Coming from bookmark or external link, so no navigation context
        this.navigationModel = null;

        final SolrDocumentModel documentModel = new SolrDocumentModel(params.get("docId").toString());
        setModel(documentModel);

        final QueryFacetsSelection selection = selectionParametersConverter.fromParameters(params);

        addComponents();
    }

    public RecordPage(IModel<SolrDocument> documentModel, IModel<SearchContext> contextModel) {
        super(documentModel);
        this.navigationModel = contextModel;
        addComponents();
    }

    private void addComponents() {
        // Navigation
        add(createNavigation("navigation"));

        // General information section
        add(new SolrFieldLabel("name", getModel(), FacetConstants.FIELD_NAME, "Unnamed record"));
        add(createLandingPageLink("landingPageLink"));
        add(new FieldsTablePanel("documentProperties", new DocumentFieldsProvider(getModel(), basicPropertiesFilter)));

        // Resources section
        add(new ResourceLinksPanel("resources", new SolrFieldModel<String>(getModel(), FacetConstants.FIELD_RESOURCE)));

        // Technical section
        add(createCmdiContent("cmdi"));
        add(new FieldsTablePanel("technicalProperties", new DocumentFieldsProvider(getModel(), technicalPropertiesFilter)));
    }

    private Component createNavigation(final String id) {
        if (navigationModel != null) {
            // Add a panel that shows the index of the current record in the
            // resultset and allows for forward/backward navigation
            return new RecordNavigationPanel(id, navigationModel);
        } else {
            // If no context model is available (i.e. when coming from a bookmark
            // or external link, do not show the navigation panel
            final WebMarkupContainer navigationDummy = new WebMarkupContainer(id);
            navigationDummy.setVisible(false);
            return navigationDummy;
        }
    }

    private ExternalLink createLandingPageLink(String id) {
        final SolrFieldStringModel landingPageHrefModel = new SolrFieldStringModel(getModel(), FacetConstants.FIELD_LANDINGPAGE);
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

    private Label createCmdiContent(String id) {
        final IModel<String> locationModel = new SolrFieldStringModel(getModel(), FacetConstants.FIELD_FILENAME);
        final UrlFromStringModel locationUrlModel = new UrlFromStringModel(locationModel);
        final Label cmdiContentLabel = new Label(id, new XsltModel(locationUrlModel));
        cmdiContentLabel.setEscapeModelStrings(false);
        return cmdiContentLabel;
    }

    @Override
    public void detachModels() {
        super.detachModels();
        // not passed to parent
        navigationModel.detach();
    }

}
