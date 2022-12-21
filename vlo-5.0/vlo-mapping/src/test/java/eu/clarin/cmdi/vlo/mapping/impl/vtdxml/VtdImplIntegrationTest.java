/*
 * Copyright (C) 2022 twagoo
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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import com.google.common.collect.FluentIterable;
import eu.clarin.cmdi.vlo.mapping.RecordFieldValuesMapper;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isA;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.Test;

/**
 *
 * @author twagoo
 */
@Slf4j
public class VtdImplIntegrationTest extends AbstractVtdImplIntegrationTest {

    @Override
    protected VloMappingConfiguration getMappingConfig() {
        final VloMappingConfiguration config = super.getMappingConfig();
        config.setMappingDefinitionUri(getClass().getResource("/eu/clarin/cmdi/vlo/mapping/impl/vtdxml/integrationtest-mapping.xml").toString());
        return config;
    }

    /**
     * Test of mapRecordToFields method, of class RecordFieldValuesMapperImpl.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testMapRecordToFields() throws Exception {
        final URL recordUrl = getClass().getResource("/records/p_1288172614026.cmdi");
        final File file = new File(recordUrl.getFile());
        final RecordFieldValuesMapper instance = getFieldValuesMapper();
        final Map<String, Collection<ValueLanguagePair>> map = instance.mapRecordToFields(file);

        assertThat(map, hasValues("title", "WALS Online Resources for Abui"));
        assertThat(map, hasValues("format", "text/html"));
        assertThat(map, hasValues("language", "Anglais", "English"));
    }

    private static Matcher<Map<? extends String, ? extends Collection<ValueLanguagePair>>> hasValues(String field, String... values) {
        return hasEntry(equalTo(field), hasValueLanguagePairsWithValues(values));
    }

    private static Matcher<Iterable<?>> hasValueLanguagePairsWithValues(String[] values) {
        return IsIterableContainingInOrder.contains(// make list of value matchers 
                FluentIterable.from(values)
                        .transform(value -> isValueLanguagePairWithValue(value))
                        .toList());
    }

    private static Matcher<Object> isValueLanguagePairWithValue(String value) {
        return allOf(
                isA(ValueLanguagePair.class),
                hasProperty(
                        "value",
                        equalTo(value)));
    }

}
