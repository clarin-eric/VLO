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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.service.solr.MorgueDocumentService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import java.util.Objects;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

/**
 * Detachable model for a Solr document.
 *
 * <p>
 * By default it loads from the main index via the {@link SolrDocumentService};
 * use {@link #forTombstone} to load a "tombstone" from the morgue index instead.</p>
 *
 * @author twagoo
 */
public class SolrDocumentModel extends LoadableDetachableModel<SolrDocument> {

    /**
     * The index a {@link SolrDocumentModel} loads its document from.
     */
    public enum Source {
        MAIN {
            @Override
            SolrDocument load(SolrDocumentModel model, String id) {
                return model.getDocumentService().getDocument(id);
            }
        },
        MORGUE {
            @Override
            SolrDocument load(SolrDocumentModel model, String id) {
                return model.getMorgueDocumentService().getById(id).orElse(null);
            }
        };

        abstract SolrDocument load(SolrDocumentModel model, String id);
    }

    private final IModel<String> docId;
    private final Source source;

    public SolrDocumentModel(SolrDocument document, FieldNameService fieldNameService) {
        this(document, idModel(document, fieldNameService), Source.MAIN);
    }

    public SolrDocumentModel(String docId) {
        this(Model.of(docId));
    }

    public SolrDocumentModel(IModel<String> docId) {
        this(docId, Source.MAIN);
    }

    /**
     * @return a model that loads a tombstone document from the
     * morgue index
     */
    public static SolrDocumentModel forTombstone(SolrDocument document, String docId) {
        return new SolrDocumentModel(document, Model.of(docId), Source.MORGUE);
    }

    private SolrDocumentModel(SolrDocument document, IModel<String> docId, Source source) {
        super(document);
        this.docId = docId;
        this.source = source;
    }

    private SolrDocumentModel(IModel<String> docId, Source source) {
        this.docId = docId;
        this.source = source;
    }

    @Override
    protected SolrDocument load() {
        if (docId == null) {
            return null;
        }
        final String id = docId.getObject();
        if (id == null) {
            return null;
        }
        return source.load(this, id);
    }

    private static IModel<String> idModel(SolrDocument document, FieldNameService fieldNameService) {
        return document == null ? null
                : Model.of((String) document.getFieldValue(fieldNameService.getFieldName(FieldKey.ID)));
    }

    protected SolrDocumentService getDocumentService() {
        return VloWicketApplication.get().getDocumentService();
    }

    protected MorgueDocumentService getMorgueDocumentService() {
        return VloWicketApplication.get().getMorgueDocumentService();
    }

    @Override
    public String toString() {
        return String.format("%s docId=%s source=%s attached=%b", super.toString(),
                docId == null ? null : docId.getObject(), source, isAttached());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.docId, this.source);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SolrDocumentModel other = (SolrDocumentModel) obj;
        return Objects.equals(this.docId, other.docId) && this.source == other.source;
    }

}
