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
package eu.clarin.cmdi.vlo.wicket.provider;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.pojo.DocumentField;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.wicket.model.DocumentFieldModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

/**
 * Provider of {@link DocumentField} instances for a specific document filtered
 * by a {@link FieldFilter}
 *
 * @author twagoo
 */
public class DocumentFieldsProvider implements IDataProvider<DocumentField> {

    private final IModel<SolrDocument> documentModel;
    private final FieldFilter fieldFilter;
    private List<DocumentFieldModel> fields;
    private final List<String> fieldOrder;

    /**
     *
     * @param documentModel model that has the document containing the fields
     * @param fieldFilter filter that decides which fields are included
     * @param fieldOrder list of field names that determine the order.
     * <strong>Notice:</strong> this has to be a <em>superset</em> of the fields
     * that may be present in the {@link SolrDocument}. If it is not, runtime
     * exceptions may occur when requesting the size or iterator; see {@link Ordering#explicit(java.util.List)
     * }. It may also be null, in which case no ordering is applied.
     */
    public DocumentFieldsProvider(IModel<SolrDocument> documentModel, FieldFilter fieldFilter, List<String> fieldOrder) {
        this.documentModel = documentModel;
        this.fieldFilter = fieldFilter;
        this.fieldOrder = fieldOrder;
    }

    private List<DocumentFieldModel> getFields() {
        if (fields == null) {
            // lazy loading/caching of included field models
            final Collection<String> allFields = documentModel.getObject().getFieldNames();
            fields = new ArrayList<DocumentFieldModel>(allFields.size());
            for (String field : allFields) {
                if (fieldFilter.allowField(field)) {
                    fields.add(new DocumentFieldModel(documentModel, field));
                }
            }
        }

        final Ordering<DocumentField> fieldOrdering = getFieldOrdering();
        if (fieldOrdering == null) {
            return fields;
        } else {
            return fieldOrdering.sortedCopy(fields);
        }
    }

    /**
     * Makes an explicit ordering based on the field name of the document field.
     * Note: this is not stored in the instance because the resulting Ordering
     * is not serializable
     *
     * @return ordering for document fields, or null if no ordering has been
     * specified
     */
    private Ordering<DocumentField> getFieldOrdering() {
        if (fieldOrder == null) {
            return null;
        } else {
            final Ordering<DocumentField> fieldOrdering = Ordering.explicit(fieldOrder).onResultOf(new Function<DocumentField, String>() {

                @Override
                public String apply(DocumentField input) {
                    return input.getFieldName();
                }
            });
            return fieldOrdering;
        }
    }

    /**
     *
     * @param first index of the first item in the list returned by the iterator
     * @param count is ignored
     * @return
     */
    @Override
    public Iterator<? extends DocumentField> iterator(long first, long count) {
        return getFields().listIterator((int) first);
    }

    @Override
    public long size() {
        return getFields().size();
    }

    @Override
    public IModel<DocumentField> model(DocumentField object) {
        return (DocumentFieldModel) object;
    }

    @Override
    public void detach() {
        fields = null;
    }

}
