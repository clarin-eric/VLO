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
package eu.clarin.cmdi.vlo.batchimporter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.data.model.MetadataFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.ItemReader;

import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MetadataFilesBatchReaderFactoryTest {

    /**
     * Test of getObject method, of class MetadataFilesBatchReaderFactory.
     */
    @Test
    public void testGetObject(@TempDir Path tempDir) throws Exception {
        final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.<String, String>builder();
        mapBuilder.put("root1", Files.createDirectory(tempDir.resolve("root1")).toString());
        mapBuilder.put("root2", Files.createDirectory(tempDir.resolve("root2")).toString());
        final ImmutableMap<String, String> rootsMap = mapBuilder.build();
        log.info("Data roots map with temp directories created: {}", rootsMap);

        // Content for root1
        Path root1Coll1 = Files.createDirectory(Path.of(rootsMap.get("root1"), "collection1"));
        Path root1Coll2 = Files.createDirectory(Path.of(rootsMap.get("root1"), "collection2"));
        Files.createFile(root1Coll1.resolve("file1.xml"));      //root1/coll1/file1.xml
        Files.createFile(root1Coll1.resolve("file2.xml"));      //root1/coll1/file2.xml
        Files.createFile(root1Coll2.resolve("file3.cmdi"));     //root1/coll2/file2.cmdi

        // Content for root2        
        Path root2Coll1 = Files.createDirectory(Path.of(rootsMap.get("root2"), "collection1"));
        Files.createFile(root2Coll1.resolve("file1.xml"));      //root2/coll1/file1.xml
        Files.createFile(root2Coll1.resolve("FILE2.CMDI"));     //root2/coll1/FILE2.CMDI
        Files.createFile(root2Coll1.resolve("file3.txt"));      //root2/coll1/file2.txt (should be skipped, not an XML or CMDI file)

        final MetadataFilesBatchReaderFactory instance = new MetadataFilesBatchReaderFactory(rootsMap);
        final ItemReader<MetadataFile> reader = instance.getObject();

        final List<MetadataFile> results = Lists.newArrayList();
        MetadataFile result;
        do {
            result = reader.read();
            if (result != null) {
                results.add(result);
            }
        } while (result != null);

        log.info("Items in item reader: {}", results);

        assertEquals(5, results.size());
    }

}
