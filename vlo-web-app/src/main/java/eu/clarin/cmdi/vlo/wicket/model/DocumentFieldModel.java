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

import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.pojo.DocumentField;
import java.util.Collection;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Model for {@link DocumentField} that wraps a document model and a field name
 *
 * @author twagoo
 * @param <T> type of the field
 */
public class DocumentFieldModel<T> extends AbstractReadOnlyModel<DocumentField<T>> implements DocumentField {
    
    private final IModel<String> fieldName;
    private final IModel<Collection<T>> valueModel;

    /**
     * Uses the document model to create a {@link SolrFieldModel} to provide the
     * field values
     *
     * @param documentModel model that provides the document
     * @param fieldName name of the field to represent
     */
    public DocumentFieldModel(IModel<SolrDocument> documentModel, String fieldName) {
        this(new SolrFieldModel<T>(documentModel, fieldName), Model.of(fieldName));
    }
    
    public DocumentFieldModel(IModel<Collection<T>> valueModel, IModel<String> fieldName) {
        this.fieldName = fieldName;
        this.valueModel = valueModel;
    }
    
    @Override
    public DocumentField getObject() {
        return this;
    }
    
    @Override
    public String getFieldName() {
        return fieldName.getObject();
    }
    
    @Override
    public List<T> getValues() {
        return Lists.newArrayList(valueModel.getObject());
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", fieldName, valueModel);
    }
    
}
