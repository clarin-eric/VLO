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

import com.google.common.io.ByteStreams;
import eu.clarin.cmdi.vlo.batchimporter.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.MappingInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MetadataFileParserTest {

    @TempDir
    public Path tempDir;

    public final String[] testResources = {
        "test_record1.xml"
    };

    @BeforeEach
    public void copyTestResourcesToDisk() throws IOException {
        for (String resource : testResources) {
            final Path target = getTestResourcePath(resource);
            log.debug("Copying test resource to disk: {} -> {}", resource, target);
            copyTestResourceToDisk(resource, target);
        }
    }

    private void copyTestResourceToDisk(String name, Path target) throws IOException {
        try (InputStream resourceAsStream = MetadataFileParserTest.class.getResourceAsStream(name)) {
            try (OutputStream outputStream = Files.newOutputStream(target)) {
                ByteStreams.copy(resourceAsStream, outputStream);
            }
        }
    }

    private Path getTestResourcePath(String name) {
        return tempDir.resolve(name);
    }

    /**
     * Test of parseFile method, of class MetadataFileParser.
     */
    @Test
    public void testParseFile() throws Exception {
        System.out.println("parseFile");
        MetadataFile inputFile = new MetadataFile("Test data root", getTestResourcePath(testResources[0]));
        MetadataFileParser instance = new MetadataFileParser();
        MappingInput result = instance.parseFile(inputFile);

        assertNotNull(result);
        assertEquals("http://hdl.handle.net/11356/1208@format=cmdi", result.getSelflink());
    }

}
