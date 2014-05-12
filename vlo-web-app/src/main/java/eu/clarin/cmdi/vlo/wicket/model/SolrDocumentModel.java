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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Detachable model for Solr documents that uses the {@link SolrDocumentService}
 * registered on the application to load a single document.
 *
 * @author twagoo
 * @see VloWicketApplication#getDocumentService()
 */
public class SolrDocumentModel extends LoadableDetachableModel<SolrDocument> {

    private final String docId;

    public SolrDocumentModel(SolrDocument document) {
        super(document);
        this.docId = (String) document.getFieldValue(FacetConstants.FIELD_ID);
    }

    public SolrDocumentModel(String docId) {
        this.docId = docId;
    }

    @Override
    protected SolrDocument load() {
        if (docId == null) {
            return null;
        } else {
            return getDocumentService().getDocument(docId);
        }
    }

    @Override
    public String toString() {
        return String.format("%s docId=%s attached=%b", super.toString(), docId, isAttached());
    }

    protected SolrDocumentService getDocumentService() {
        return VloWicketApplication.get().getDocumentService();
    }

}
