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
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.LoggerFactory;

/**
 * Generates a signature for a document to detect (near-) duplicates
 *
 * @author Thomas Eckart
 */
public class DeduplicationSignature {

    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DeduplicationSignature.class);

    public static String getSignature(FieldNameServiceImpl fieldNameService, CMDIData doc) {
        StringBuilder sb = new StringBuilder("");

        List<FieldKey> signatureFields = Arrays.asList(FieldKey.LANGUAGE_CODE, FieldKey.DATA_PROVIDER_NAME, FieldKey.DESCRIPTION, FieldKey.COLLECTION);
        for (FieldKey field : signatureFields) {
            if (doc.hasField(fieldNameService.getFieldName(field))) {
                for (Object value : doc.getFieldValues(fieldNameService.getFieldName(field))) {
                    sb.append((String) value);
                }
            }
        }

        return DigestUtils.md5Hex(sb.toString());
    }
}
