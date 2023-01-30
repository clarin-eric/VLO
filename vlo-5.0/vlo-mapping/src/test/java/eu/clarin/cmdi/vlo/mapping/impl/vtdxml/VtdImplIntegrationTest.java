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
import static eu.clarin.cmdi.vlo.mapping.VloMappingTestHelper.createStreamSourceForResource;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.Collection;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
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
        config.setMappingDefinitionUri(getClass().getResource("/mappings/integrationtest-mapping.xml").toString());
        return config;
    }

    /**
     * Test of mapRecordToFields method, of class RecordFieldValuesMapperImpl.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testMapRecordToFields() throws Exception {
        final StreamSource source = createStreamSourceForResource(getClass(), "/records/p_1288172614026.cmdi");
        final RecordFieldValuesMapper instance = getFieldValuesMapper();
        final Map<String, Collection<ValueLanguagePair>> map = instance.mapRecordToFields(source);

        assertThat(map, hasValues("title", "WALS Online Resources for Abui"));
        assertThat(map, hasValues("format", "text/html"));
        // language: field values filter should keep highest scoring values only, i.e. 'Anglais' value is discared
        assertThat(map, hasValues("language", "English"));
        assertThat(map, hasValues("subject", "abz"));
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
