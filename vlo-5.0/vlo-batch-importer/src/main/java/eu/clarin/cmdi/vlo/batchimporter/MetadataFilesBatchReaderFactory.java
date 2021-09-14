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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import eu.clarin.cmdi.vlo.batchimporter.model.MetadataFile;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.FactoryBean;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MetadataFilesBatchReaderFactory implements FactoryBean<ItemReader<MetadataFile>> {

    private final Map<String, String> dataRootsMap;
    private final Predicate<Path> filesFilter;

    private final static Predicate<Path> DEFAULT_FILES_FILTER = Predicates.compose(
            Predicates.or(
                    pathString -> pathString.endsWith(".xml"),
                    pathString -> pathString.endsWith(".cmdi")),
            (path) -> path.getFileName().toString().toLowerCase());

    public MetadataFilesBatchReaderFactory(Map<String, String> dataRootsMap) {
        this(dataRootsMap, DEFAULT_FILES_FILTER);
    }

    public MetadataFilesBatchReaderFactory(Map<String, String> dataRootsMap, Predicate<Path> filesFilter) {
        this.dataRootsMap = dataRootsMap;
        this.filesFilter = filesFilter;
    }

    private Iterator<MetadataFile> newMetadataFileIterator(String dataRootName, String dataRootPath) {
        log.info("Instantiating file iterator for data root '{}' in '{}'", dataRootName, dataRootPath);
        
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Path.of(dataRootPath), filesFilter::apply)) {
            return Iterators.transform(directoryStream.iterator(), (path) -> new MetadataFile(dataRootName, path));
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Error while creating file iterator for data root with name '%s' and path '%s'", dataRootName, dataRootPath), ex);
        }
    }

    @Override
    public ItemReader<MetadataFile> getObject() throws Exception {
        final Iterator[] iteratorsArray = dataRootsMap.entrySet().stream()
                .map(entry -> newMetadataFileIterator(entry.getKey(), entry.getValue()))
                .toArray(Iterator[]::new);

        return new IteratorItemReader(Iterators.concat(iteratorsArray));
    }

    @Override
    public Class<?> getObjectType() {
        return ItemReader.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
