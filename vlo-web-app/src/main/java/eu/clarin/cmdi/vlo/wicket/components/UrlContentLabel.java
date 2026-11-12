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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Renders the content of a URL as unescaped markup, so only use it for trusted
 * URLs from the application's configuration.
 */
public class UrlContentLabel extends Label {

    public UrlContentLabel(String id, String url) {
        super(id, new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                try (InputStream in = URI.create(url).toURL().openStream()) {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });
        setEscapeModelStrings(false);
    }
}
