/*
 * Copyright (C) 2020 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel.JsonLdObject;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RecordStructuredMeatadataHeaderBehavior extends JsonLdHeaderBehavior {

    public RecordStructuredMeatadataHeaderBehavior(IModel<SolrDocument> documentModel) {
        super(createJsonModel(documentModel));
    }

    @Override
    public boolean isEnabled(Component component) {
        // TODO disable in certain cases??
        return super.isEnabled(component);
    }

    private static IModel<String> createJsonModel(IModel<SolrDocument> documentModel) {
        final LoadableDetachableModel<JsonLdObject> model = new LoadableDetachableModel<>() {
            @Override
            protected JsonLdObject load() {
                if (documentModel.getObject() == null) {
                    return null;
                } else {
                    return createDataSetForDocument(documentModel.getObject());
                }
            }
        };

        return new JsonLdModel(model);
    }

    private static DataSet createDataSetForDocument(SolrDocument document) {
        final DataSet dataSet = new DataSet();
        //TODO: set properties
        return dataSet;
    }

    private static class DataSet extends JsonLdObject {

        public DataSet() {
            super("https://schema.org", "DataSet");
        }

    }

}
