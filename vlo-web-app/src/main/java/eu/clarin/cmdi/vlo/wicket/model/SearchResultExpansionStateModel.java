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
package eu.clarin.cmdi.vlo.wicket.model;


import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import java.util.Set;
import org.apache.wicket.model.IModel;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;

/**
 * Model for the expansion state of an individual document in a search result
 * set, based on a shared set of id's of expanded documents and tied to a
 * specific document model for dynamic lookup
 *
 * @author twagoo
 */
public class SearchResultExpansionStateModel implements IModel<ExpansionState> {

    private final IModel<Set<Object>> expandedItemsModel;
    private final IModel<SolrDocumentExpansionPair> documentModel;

    /**
     *
     * @param expandedItemsModel model of a set that holds the id's of the
     * expanded documents (=result items)
     * @param documenModel model of the document of which this model represents
     * the expanded state
     */
    public SearchResultExpansionStateModel(IModel<Set<Object>> expandedItemsModel, IModel<SolrDocumentExpansionPair> documenModel) {
        this.expandedItemsModel = expandedItemsModel;
        this.documentModel = documenModel;
    }

    @Override
    public ExpansionState getObject() {
        // state is represented by presence in the set
        if (expandedItemsModel.getObject().contains(getDocumentId())) {
            return ExpansionState.EXPANDED;
        } else {
            return ExpansionState.COLLAPSED;
        }
    }

    @Override
    public void setObject(ExpansionState object) {
        // add or remove from expansion set
        if (object == ExpansionState.COLLAPSED) {
            expandedItemsModel.getObject().remove(getDocumentId());
        } else {
            expandedItemsModel.getObject().add(getDocumentId());
        }
    }

    @Override
    public void detach() {
        expandedItemsModel.detach();
        documentModel.detach();
    }

    private Object getDocumentId() {
        return documentModel.getObject().getDocument().getFieldValue(VloWicketApplication.get().getFieldNameService().getFieldName(FieldKey.ID));
    }

}
