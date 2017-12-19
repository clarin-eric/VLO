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

import javax.inject.Inject;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.FieldKey;

/**
 * Detachable model for Solr documents that uses the {@link SolrDocumentService}
 * registered on the application to load a single document.
 *
 * @author twagoo
 * @see VloWicketApplication#getDocumentService()
 */
public class SolrDocumentModel extends LoadableDetachableModel<SolrDocument> {

    
    private final IModel<String> docId;
    


    public SolrDocumentModel(SolrDocument document, FieldNameService fieldNameService) {
        super(document);
        if (document == null) {
            this.docId = null;
        } else {
            this.docId = Model.of((String) document.getFieldValue(fieldNameService.getFieldName(FieldKey.ID)));
        }
    }

    public SolrDocumentModel(String docId) {
        this(Model.of(docId));
    }

    public SolrDocumentModel(IModel<String> docId) {
        this.docId = docId;
    }

    @Override
    protected SolrDocument load() {
        if (docId == null) {
            return null;
        } else {
            final String id = docId.getObject();
            if (id == null) {
                return null;
            } else {
                return getDocumentService().getDocument(id);
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
        final SolrDocumentModel other = (SolrDocumentModel) obj;
        if (!Objects.equals(this.docId, other.docId)) {
            return false;
        }
        return true;
    }

}
