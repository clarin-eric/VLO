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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import eu.clarin.cmdi.vlo.mapping.ContextFactory;
import eu.clarin.cmdi.vlo.mapping.VloMappingException;
import eu.clarin.cmdi.vlo.mapping.model.CmdRecord;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import java.io.IOException;
import java.util.stream.Stream;
import javax.xml.transform.stream.StreamSource;
import eu.clarin.cmdi.vlo.mapping.CmdRecordFactory;

/**
 * Generates all contexts (concept + XML paths with values) for an XML document
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class ContextFactoryImpl implements ContextFactory {

    private final CmdRecordFactory recordFactory;

    public ContextFactoryImpl(CmdRecordFactory recordFactory) {
        this.recordFactory = recordFactory;
    }

    @Override
    public Stream<ValueContext> createContexts(StreamSource source) throws IOException, VloMappingException {
        final CmdRecord record = recordFactory.getRecord(source);
        //TODO: get contexts from the record object
        return record.getContexts().stream();
    }

}
