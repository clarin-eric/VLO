/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.components;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.wicket.util.tester.WicketTester;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileContentLabelTest {

    @TempDir
    Path tempDir;

    private WicketTester tester;

    @BeforeEach
    public void setUp() {
        tester = new WicketTester();
    }

    @AfterEach
    public void tearDown() {
        tester.destroy();
    }

    @Test
    public void testRendersFileContentUnescaped() throws IOException {
        final Path file = tempDir.resolve("snippet.html");
        Files.writeString(file, "<b>hello</b>");

        tester.startComponentInPage(new FileContentLabel("label", file.toUri().toString()));

        tester.assertContains("<b>hello</b>");
    }

    @Test
    public void testThrowsForMissingFile() {
        final String fileUrl = tempDir.resolve("missing.html").toUri().toString();
        final FileContentLabel label = new FileContentLabel("label", fileUrl);

        assertThrows(UncheckedIOException.class, () -> label.getDefaultModel().getObject());
    }

    @Test
    public void testRejectsNonFileUrl() {
        final FileContentLabel label = new FileContentLabel("label", "https://example.org/snippet.html");

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> label.getDefaultModel().getObject());

        assertTrue(ex.getMessage().startsWith("Expected a file: URL"), ex.getMessage());
    }

}
