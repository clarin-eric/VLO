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

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public abstract class AbstractVloImporterTest {

    private final static String TEST_RECORDS_RESOURCES_DIRECTORY = "test-records";

    @TempDir
    public Path tempDir;

    protected Path getTestResource(String name) {
        final Path targetPath = getTestResourceTargetPath(name);

        try {
            if (!Files.exists(targetPath)) {
                copyTestResourceToFileSystem(name, targetPath);
            }
        } catch (IOException ex) {
            throw new RuntimeException("An error has occured while trying to copy internal resource to local file system", ex);
        }

        return targetPath;
    }

    private Path getTestResourceTargetPath(String name) {
        return tempDir.resolve(name);
    }

    private void copyTestResourceToFileSystem(String resourceName, Path fsTarget) throws IOException {
        log.debug("Copying test resource '{}' to file system location '{}'", resourceName, fsTarget);
        try (InputStream resourceAsStream = getTestRecordResourceStream(resourceName)) {
            try (OutputStream outputStream = Files.newOutputStream(fsTarget)) {
                if (resourceAsStream == null) {
                    throw new RuntimeException(String.format("No input stream for resource '%s'. Does not exist?", resourceName));
                }
                if (outputStream == null) {
                    throw new RuntimeException(String.format("No output stream for filesystem location '%s'. Does not exist?", fsTarget));
                }
                ByteStreams.copy(resourceAsStream, outputStream);
            }
        }
    }

    private InputStream getTestRecordResourceStream(String resourceName) {
        final String resourcePath
                = Path.of(TEST_RECORDS_RESOURCES_DIRECTORY).resolve(resourceName).toString();
        return AbstractVloImporterTest.class.getResourceAsStream(resourcePath);
    }
}
