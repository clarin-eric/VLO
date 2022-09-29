/*
 * Copyright (C) 2022 CLARIN
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
package eu.clarin.cmdi.vlo.importer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class LoggerOutputStream extends OutputStream {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(10_000);
    private final String prefix;
    private final Consumer<String> logCommand;

    public LoggerOutputStream(Consumer<String> logCommand, String prefix) {
        this.logCommand = logCommand;
        this.prefix = prefix;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            final String line = buffer.toString();
            buffer.reset();
            logCommand.accept(prefix + line);
        } else {
            buffer.write(b);
        }
    }

    @Override
    public void close() throws IOException {
        if(buffer.size() > 0) {
            logCommand.accept(prefix + buffer.toString());
        }
        super.close();
    }

}
