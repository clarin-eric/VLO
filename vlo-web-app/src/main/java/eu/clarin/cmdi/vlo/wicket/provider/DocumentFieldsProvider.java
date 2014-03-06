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

import eu.clarin.cmdi.vlo.pojo.DocumentField;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.wicket.model.DocumentFieldModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
    private Collection<DocumentFieldModel> fields;

    /**
     *
     * @param documentModel model that has the document containing the fields
     * @param fieldFilter filter that decides which fields are included
     */
    public DocumentFieldsProvider(IModel<SolrDocument> documentModel, FieldFilter fieldFilter) {
        this.documentModel = documentModel;
        this.fieldFilter = fieldFilter;
    }

    private Collection<DocumentFieldModel> getFields() {
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
        return fields;
    }

    @Override
    public Iterator<? extends DocumentField> iterator(long first, long count) {
        return getFields().iterator();
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
