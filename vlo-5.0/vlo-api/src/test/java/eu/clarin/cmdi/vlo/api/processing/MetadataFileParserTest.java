/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.api.processing;

import eu.clarin.cmdi.vlo.api.AbstractVloApiTest;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecord.Resource;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MetadataFileParserTest extends AbstractVloApiTest {

    /**
     * Test of parseFile method, of class MetadataFileParser.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testParseFile() throws Exception {
        final Path testRecord1 = getTestResource("test_record1.xml");

        final VloRecordMappingRequest requestInput = VloRecordMappingRequest.builder()
                .dataRoot("Test data root")
                .file(testRecord1.toString())
                .xmlContent(Files.readAllBytes(testRecord1))
                .build();
        final MetadataFileProcessorImpl instance = new MetadataFileProcessorImpl();
        final VloRecord result = instance.processMappingRequest(requestInput);

        assertNotNull(result);
        assertEquals("http://hdl.handle.net/11356/1208@format=cmdi", result.getSelflink());
        assertEquals(testRecord1, Path.of(result.getSourcePath()));
        assertThat(result.getResources(), allOf(notNullValue(), hasSize(4)));
        assertThat(result.getResources(), hasItem(allOf(
                Matchers.isA(Resource.class),
                hasProperty("id", equalTo("lp_1746")),
                hasProperty("ref", equalTo("http://hdl.handle.net/11356/1208"))
        )));

        final Map<String, List<String>> valuesMap = result.getPathValuesMap();

        assertThat(valuesMap.get("/cmd:CMD/cmd:Components/cmdp:LINDAT_CLARIN/cmdp:bibliographicInfo/cmdp:projectUrl"),
                allOf(notNullValue(), hasSize(1)));
        assertThat(valuesMap.get("/cmd:CMD/cmd:Components/cmdp:LINDAT_CLARIN/cmdp:bibliographicInfo/cmdp:projectUrl"),
                hasItem("https://parlameter.si/"));

        // path without value should not be in map
        assertFalse(valuesMap.containsKey("/cmd:CMD/cmd:Components/cmdp:LINDAT_CLARIN/cmdp:bibliographicInfo/cmdp:authors/cmdp:author"));

        assertThat(valuesMap.get("/cmd:CMD/cmd:Components/cmdp:LINDAT_CLARIN/cmdp:bibliographicInfo/cmdp:authors/cmdp:author/cmdp:lastName"),
                allOf(notNullValue(), hasSize(3)));
    }

}
