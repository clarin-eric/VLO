/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.mapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author twagoo
 */
public class VloMappingTestHelper {
    // TODO: make this a general test util

    public static StreamSource createStreamSourceForResource(String resource) throws IOException {
        return createStreamSourceForResource(VloMappingTestHelper.class, resource);
    }

    public static StreamSource createStreamSourceForResource(Class context, String resource) throws IOException {
        final URL recordUrl = context.getResource(resource);
        final StreamSource source = new StreamSource(recordUrl.toString());
        source.setInputStream(recordUrl.openStream());
        return source;
    }

    public static StreamSource createStreamSourceForFile(final File file) throws FileNotFoundException {
        final StreamSource source = new StreamSource(file);
        source.setInputStream(new FileInputStream(file));
        return source;
    }
}
