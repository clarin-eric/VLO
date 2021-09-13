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

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import eu.clarin.cmdi.vlo.batchimporter.model.MetadataFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
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

    private final String recordsDirectoryPath;

    public MetadataFilesBatchReaderFactory(String recordsDirectoryPath) {
        this.recordsDirectoryPath = recordsDirectoryPath;
    }

    @Override
    public ItemReader<MetadataFile> getObject() throws Exception {
        log.info("Instantiating file reader in {}", recordsDirectoryPath);
        final Iterator<Path> filesIterator
                = Files.newDirectoryStream(Path.of(recordsDirectoryPath))
                        .iterator();

        final UnmodifiableIterator<Path> filteredIterator
                = Iterators.filter(filesIterator,
                        path -> path.getFileName().toString().endsWith(".xml"));

        final Iterator<MetadataFile> filteredMetadataFileIterator
                = Iterators.transform(filteredIterator, MetadataFile::new);

        return new IteratorItemReader<>(filteredMetadataFileIterator);
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
