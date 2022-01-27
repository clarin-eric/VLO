/*
 * Copyright (C) 2019 CLARIN
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
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RecordMetadataLinksCountModel implements IModel<Integer> {

    private final IModel<SolrDocument> documentModel;

    public RecordMetadataLinksCountModel(IModel<SolrDocument> documentModel) {
        this.documentModel = documentModel;
    }

    @Override
    public Integer getObject() {
        final FieldNameService fieldNameService = VloWicketApplication.get().getFieldNameService();

        final SolrDocument document = documentModel.getObject();
        if (document != null) {
            final Object partCount = document.getFieldValue(fieldNameService.getFieldName(FieldKey.HAS_PART_COUNT));
            if (partCount instanceof Integer) {
                return (Integer) partCount;
            }
        }
        return 0;
    }

    @Override
    public void detach() {
        documentModel.detach();
    }

}
