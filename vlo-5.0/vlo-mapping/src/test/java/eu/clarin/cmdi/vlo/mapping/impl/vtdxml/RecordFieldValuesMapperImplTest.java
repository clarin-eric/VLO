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
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.VloMappingTestConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import org.junit.jupiter.api.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class RecordFieldValuesMapperImplTest {

    final VloMappingConfiguration mappingConfig = new VloMappingTestConfiguration();

    /**
     * Test of mapRecordToFields method, of class RecordFieldValuesMapperImpl.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testMapRecordToFields() throws Exception {
        final URL recordUrl = getClass().getResource("/records/p_1345561703673.cmdi");
        final File file = new File(recordUrl.getFile());
        final RecordFieldValuesMapper instance = new RecordFieldValuesMapperImpl(mappingConfig);
        Map<String, Collection<ValueLanguagePair>> result = instance.mapRecordToFields(file);
        log.info("Result for {} fields", result.keySet().size());
        assertThat(result, aMapWithSize(2));
        assertThat(result, hasEntry(equalTo("id"), allOf(isA(Collection.class), hasSize(1))));
        assertThat(result, hasEntry(equalTo("location"), allOf(isA(Collection.class), hasSize(2))));
    }

}
