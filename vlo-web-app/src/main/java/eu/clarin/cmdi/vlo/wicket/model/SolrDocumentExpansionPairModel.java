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

import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import java.util.Objects;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;

/**
 * Detachable model for Solr documents that uses the {@link SolrDocumentService}
 * registered on the application to load a single document.
 *
 * @author twagoo
 * @see VloWicketApplication#getDocumentService()
 */
public class SolrDocumentExpansionPairModel extends LoadableDetachableModel<SolrDocumentExpansionPair> {

    /**
     * Maximum number of records to retrieve in the expansion
     */
    private static final int DEFAULT_EXPANSION_LIMIT = 25;

    private final IModel<String> docId;
    private String collapseField;

    public SolrDocumentExpansionPairModel(SolrDocumentExpansionPair pair, FieldNameService fieldNameService, String collapseField) {
        super(pair);
        if (pair == null) {
            this.docId = null;
        } else {
            this.docId = Model.of((String) pair.getDocument().getFieldValue(fieldNameService.getFieldName(FieldKey.ID)));
        }
        this.collapseField = collapseField;
    }

    public SolrDocumentExpansionPairModel(String docId) {
        this(Model.of(docId));
    }

    public SolrDocumentExpansionPairModel(IModel<String> docId) {
        this.docId = docId;
    }

    @Override
    protected SolrDocumentExpansionPair load() {
        if (docId == null) {
            return null;
        } else {
            final String id = docId.getObject();
            if (id == null) {
                return null;
            } else {
                return getDocumentService().getDocumentWithExpansion(id, collapseField, DEFAULT_EXPANSION_LIMIT);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s docId=%s attached=%b", super.toString(), docId.getObject(), isAttached());
    }

    protected SolrDocumentService getDocumentService() {
        return VloWicketApplication.get().getDocumentService();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.docId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SolrDocumentExpansionPairModel other = (SolrDocumentExpansionPairModel) obj;
        if (!Objects.equals(this.docId, other.docId)) {
            return false;
        }
        return true;
    }

}
