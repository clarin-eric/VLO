/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.LoggerFactory;

/**
 * Generates a signature for a document to detect (near-) duplicates
 * 
 * @author Thomas Eckart
 */
public class DeduplicationSignature {
    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DeduplicationSignature.class);
    
    public static String getSignature(FieldNameServiceImpl fieldNameService, SolrInputDocument doc) {
        StringBuilder sb = new StringBuilder("");

        for(Object obj : doc.getFieldValues(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE))) {
            String value = (String) obj;
            sb.append(value);
        }
        sb.append(doc.getFieldValue(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER_NAME)));
        for(Object obj : doc.getFieldValues(fieldNameService.getFieldName(FieldKey.DESCRIPTION))) {
            String value = (String) obj;
            sb.append(value);
        }
        sb.append(doc.getFieldValue(fieldNameService.getFieldName(FieldKey.COLLECTION)));

        return DigestUtils.md5Hex(sb.toString());
    }
}
