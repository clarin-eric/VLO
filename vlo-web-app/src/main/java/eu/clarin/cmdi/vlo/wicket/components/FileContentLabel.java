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
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Renders the content of a local file (passed as a {@code file:} URL) as
 * unescaped markup, so it can be used only for files from the vlo's configuration.
 */
public class FileContentLabel extends Label {

    public FileContentLabel(String id, String fileUrl) {
        super(id, LoadableDetachableModel.of(() -> read(fileUrl)));
        setEscapeModelStrings(false);
    }

    private static String read(String fileUrl) {
        try {
            // Only a local file: URL converts to a path; any other URL throws
            return Files.readString(toPath(fileUrl));
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read file at " + fileUrl, ex);
        }
    }

    private static Path toPath(String fileUrl) {
        try {
            return Path.of(URI.create(fileUrl));
        } catch (IllegalArgumentException | FileSystemNotFoundException | NullPointerException ex) {
            throw new IllegalArgumentException("Expected a file: URL, got: " + fileUrl, ex);
        }
    }
}
