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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author twagoo
 */
public class SearchResultItemPanel extends Panel {

    private final Panel collapsedDetails;
    private final Panel expandedDetails;
    private final IModel<ExpansionState> expansionStateModel;

    public SearchResultItemPanel(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel) {
        super(id, documentModel);

        final Link recordLink = new RecordPageLink("recordLink", documentModel, selectionModel);
        recordLink.add(new SolrFieldLabel("title", documentModel, FacetConstants.FIELD_NAME));
        add(recordLink);

        expansionStateModel = Model.of(ExpansionState.COLLAPSED);

        // add a link to toggle the expansion state
        add(createExpansionStateToggle("expansionStateToggle"));

        // add a collapsed details panel; only shown when expansion state is collapsed (through onConfigure)
        collapsedDetails = new SearchResultItemCollapsedPanel("collapsedDetials", documentModel, selectionModel);
        add(collapsedDetails);

        // add a collapsed details panel; only shown when expansion state is expanded (through onConfigure)
        expandedDetails = new SearchResultItemExpandedPanel("expandedDetails", documentModel, selectionModel);
        add(expandedDetails);

        setOutputMarkupId(true);
    }

    private Link createExpansionStateToggle(String id) {
        final Link expansionStateToggle = new IndicatingAjaxFallbackLink(id) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                // toggle the expansion state
                if (expansionStateModel.getObject() == ExpansionState.COLLAPSED) {
                    expansionStateModel.setObject(ExpansionState.EXPANDED);
                } else {
                    expansionStateModel.setObject(ExpansionState.COLLAPSED);
                }

                if (target != null) {
                    // parial update (just this search result item)
                    target.add(SearchResultItemPanel.this);
                }
            }
        };
        expansionStateToggle.add(new Label("state", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                if (expansionStateModel.getObject() == ExpansionState.COLLAPSED) {
                    return "Expand";
                } else {
                    return "Collapse";
                }
            }
        }));
        return expansionStateToggle;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        // this is called once per request; set visibility state for detail panels
        // according to expansion state
        collapsedDetails.setVisible(expansionStateModel.getObject() == ExpansionState.COLLAPSED);
        expandedDetails.setVisible(expansionStateModel.getObject() == ExpansionState.EXPANDED);
    }

}
