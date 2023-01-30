/*
 * Copyright (C) 2022 CLARIN ERIC <clarin@clarin.eu>
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

import eu.clarin.cmdi.vlo.mapping.RecordFieldValuesMapper;
import static eu.clarin.cmdi.vlo.mapping.VloMappingTestHelper.createStreamSourceForResource;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.Collection;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class RecordFieldValuesMapperImplTest extends AbstractVtdImplIntegrationTest {

    /**
     * Test of mapRecordToFields method, of class RecordFieldValuesMapperImpl.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testMapRecordToFields() throws Exception {
        final StreamSource source = createStreamSourceForResource(getClass(), "/records/p_1345561703673.cmdi");
        final RecordFieldValuesMapper instance = getFieldValuesMapper();
        Map<String, Collection<ValueLanguagePair>> result = instance.mapRecordToFields(source);
        log.info("Result for {} fields", result.keySet().size());
        assertThat(result, aMapWithSize(4));
        assertThat(result, hasEntry(equalTo("id"), allOf(isA(Collection.class), hasSize(1))));
        assertThat(result, hasEntry(equalTo("identifier"), allOf(isA(Collection.class), hasSize(1))));
        assertThat(result, hasEntry(equalTo("location"), allOf(isA(Collection.class), hasSize(2))));
        assertThat(result, hasEntry(equalTo("locationNormalized"), allOf(
                isA(Collection.class),
                hasItem(hasProperty("value", equalTo("locationX"))),
                hasItem(hasProperty("value", equalTo("location1"))))));
    }

}
