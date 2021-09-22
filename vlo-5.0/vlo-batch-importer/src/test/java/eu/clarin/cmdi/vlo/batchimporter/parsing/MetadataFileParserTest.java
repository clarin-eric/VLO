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

import eu.clarin.cmdi.vlo.batchimporter.AbstractBatchImporterTest;
import eu.clarin.cmdi.vlo.batchimporter.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.MappingInput;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MetadataFileParserTest extends AbstractBatchImporterTest {

    /**
     * Test of parseFile method, of class MetadataFileParser.
     * @throws java.lang.Exception
     */
    @Test
    public void testParseFile() throws Exception {
        System.out.println("parseFile");
        MetadataFile inputFile = new MetadataFile("Test data root", getTestResource("test_record1.xml"));
        MetadataFileParser instance = new MetadataFileParser();
        MappingInput result = instance.parseFile(inputFile);

        assertNotNull(result);
        assertEquals("http://hdl.handle.net/11356/1208@format=cmdi", result.getSelflink());
    }

}
