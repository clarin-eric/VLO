/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.config.FieldNameService;

import java.util.Comparator;
import java.util.Map;

import javax.inject.Inject;

import eu.clarin.cmdi.vlo.service.FieldValueOrderingsFactory;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 *
 * @author twagoo
 */
public class FieldValueOrderingsFactoryImpl implements FieldValueOrderingsFactory {
    @Inject
    FieldNameService fieldNameService;

    private static final String LANGUAGE_CODE_PREFIX_ENGLISH = "{code:eng}";

    @Override
    public Map<String, Ordering<String>> createFieldValueOrderingMap() {
        return ImmutableMap.of(
            fieldNameService.getFieldName(FieldKey.DESCRIPTION), createDescriptionFieldOrdering()
        );
    }

    public Ordering<String> createDescriptionFieldOrdering() {
        return Ordering.from(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.startsWith(LANGUAGE_CODE_PREFIX_ENGLISH)) {
                    return -1;
                } else if (o2.startsWith(LANGUAGE_CODE_PREFIX_ENGLISH)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

}
