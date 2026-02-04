/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.vlo.importer.normalizer;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.List;

import eu.clarin.cmdi.vlo.importer.DocFieldContainer;
import java.util.Collection;
import java.util.Collections;

/**
 * Normalizer that serves to remove duplicates between names and alternative
 * names
 *
 * @author Twan Goosen
 */
public class AlternativeNamePostNormalizer extends AbstractPostNormalizer {

    private final String nameField;

    public AlternativeNamePostNormalizer(VloConfig config) {
        super(config);
        final FieldNameServiceImpl fieldNameService = new FieldNameServiceImpl(config);
        nameField = fieldNameService.getFieldName(FieldKey.NAME);
    }

    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {
        if (alternativeNameEqualsName(cmdiData, value)) {
            // alternative name already occurs in names, skip
            return Collections.emptyList();
        } else {
            // alternative name not found among names, keep
            return ImmutableList.of(value);
        }
    }

    /**
     * Checks if any of the values for 'name' equals the alternative name
     *
     * @param cmdiData
     * @param altName
     * @return true IFF a value equal to altName is found in the 'name' field OR
     * altName is null
     */
    private boolean alternativeNameEqualsName(DocFieldContainer cmdiData, String altName) {
        if (altName == null) {
            return true;
        } else {
            final Collection<Object> names = cmdiData.getDocField(nameField);
            if (names != null) {
                if (names.contains(altName)) {
                    return true;
                }
            }
        }
        // no true case encountered
        return false;
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }

}
