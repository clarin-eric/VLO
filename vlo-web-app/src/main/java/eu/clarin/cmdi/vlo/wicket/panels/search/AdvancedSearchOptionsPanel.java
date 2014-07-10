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
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import eu.clarin.cmdi.vlo.wicket.model.ToggleModel;
import eu.clarin.cmdi.vlo.wicket.pages.VirtualCollectionSubmissionPage;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel showing advanced search options:
 * <ul>
 * <li>selection of records that support FCS</li>
 * <li>selection of records that are based on either CMDI or OLAC</li>
 * </ul>
 *
 * @author twagoo
 */
public abstract class AdvancedSearchOptionsPanel extends ExpandablePanel<QueryFacetsSelection> {

    @SpringBean
    private VloConfig vloConfig;

    private final IDataProvider<SolrDocument> documentProvider;

    /**
     *
     * @param id component id
     * @param model model of current search query
     * @param documentProvider provider for looking up number of search results
     */
    public AdvancedSearchOptionsPanel(String id, final IModel<QueryFacetsSelection> model, final IDataProvider<SolrDocument> documentProvider) {
        super(id, model);
        this.documentProvider = documentProvider;

        // create a model for the selection state for the FCS facet
        final IModel<FacetSelection> fcsFacetModel = new FacetSelectionModel(model, FacetConstants.FIELD_SEARCH_SERVICE);
        // wrap in a toggle model that allows switching between a null selection and a 'not empty' selection
        final ToggleModel<FacetSelection> toggleModel = new ToggleModel<FacetSelection>(fcsFacetModel, null, new FacetSelection(FacetSelectionType.NOT_EMPTY));

        final Form options = new Form("options");
        final CheckBox fcsCheck = new CheckBox("fcs", toggleModel);
        fcsCheck.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                selectionChanged(target);
            }
        });
        options.add(fcsCheck);
        add(options);

        add(new Link("vcrSubmitTrigger") {

            @Override
            protected void onConfigure() {
                // hide if there are no documents to create collection from
                // or number of items is too high (according to configuration)
                final long documentCount = documentProvider.size();
                setVisible(documentCount > 0 && documentCount <= vloConfig.getVcrMaximumItemsCount());
            }

            @Override
            public void onClick() {
                setResponsePage(new VirtualCollectionSubmissionPage(model, documentProvider));
            }
        });

        // should initially be epxanded if one of the options was selected
        if (toggleModel.getObject()) {
            getExpansionModel().setObject(ExpansionState.EXPANDED);
        }
    }

    @Override
    protected Label createTitleLabel(String id) {
        return new Label(id, "Search options");
    }

    @Override
    public void detachModels() {
        super.detachModels();
        documentProvider.detach();
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

}
