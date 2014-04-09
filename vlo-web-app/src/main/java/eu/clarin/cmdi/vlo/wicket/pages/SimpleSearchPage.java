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

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.components.SearchForm;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import eu.clarin.cmdi.vlo.wicket.panels.SingleFacetPanel;
import eu.clarin.cmdi.vlo.wicket.panels.TopLinksPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class SimpleSearchPage extends VloBasePage<QueryFacetsSelection> {

    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private FacetFieldsService facetFieldsService;

    private final SingleFacetPanel collectionsPanel;
    private final TopLinksPanel topLinksPanel;
    
    public SimpleSearchPage(PageParameters parameters) {
        super(parameters);

        final Model<QueryFacetsSelection> model = Model.of(new QueryFacetsSelection());
        setModel(model);
                
        topLinksPanel = new TopLinksPanel("topLinks", model);
        topLinksPanel.setOutputMarkupId(true);
        add(topLinksPanel);
        
        collectionsPanel = new SingleFacetPanel("collectionsFacet", model, vloConfig.getCollectionFacet(), facetFieldsService) {
            
            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                if(target != null){
                    target.add(topLinksPanel);
                    target.add(collectionsPanel);
                }
            }
        };
        collectionsPanel.setOutputMarkupId(true);
        add(collectionsPanel);
        
        add(new SearchForm("search", model) {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                setResponsePage(new FacetedSearchPage(model));
            }
        });
    }

}
