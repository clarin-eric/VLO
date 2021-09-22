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
package eu.clarin.cmdi.vlo.batchimporter.parsing;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.batchimporter.AbstractBatchImporterTest;
import eu.clarin.cmdi.vlo.batchimporter.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.MappingInput;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MetadataFileParserTest extends AbstractBatchImporterTest {

    /**
     * Test of parseFile method, of class MetadataFileParser.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testParseFile() throws Exception {
        final Path testRecord1 = getTestResource("test_record1.xml");

        final MetadataFile inputFile = new MetadataFile("Test data root", testRecord1);
        final MetadataFileParser instance = new MetadataFileParser();
        final MappingInput result = instance.parseFile(inputFile);

        assertNotNull(result);
        assertEquals("http://hdl.handle.net/11356/1208@format=cmdi", result.getSelflink());
        assertEquals(testRecord1, Path.of(result.getSourcePath()));

        final Map<String, List<String>> valuesMap = result.getPathValuesMap();

        assertThat(valuesMap.get("/cmd:CMD/cmd:Components/cmdp:LINDAT_CLARIN/cmdp:bibliographicInfo/cmdp:projectUrl"),
                allOf(notNullValue(), hasSize(1)));
        assertThat(valuesMap.get("/cmd:CMD/cmd:Components/cmdp:LINDAT_CLARIN/cmdp:bibliographicInfo/cmdp:projectUrl"),
                contains("https://parlameter.si/"));
        
        // path without value should not be in map
        assertFalse(valuesMap.containsKey("/cmd:CMD/cmd:Components/cmdp:LINDAT_CLARIN/cmdp:bibliographicInfo/cmdp:authors/cmdp:author"));

        assertThat(valuesMap.get("/cmd:CMD/cmd:Components/cmdp:LINDAT_CLARIN/cmdp:bibliographicInfo/cmdp:authors/cmdp:author/cmdp:lastName"),
                allOf(notNullValue(), hasSize(3)));
    }

}
