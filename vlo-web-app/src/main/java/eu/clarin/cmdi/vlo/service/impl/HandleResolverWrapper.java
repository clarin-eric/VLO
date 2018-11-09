/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.service.PIDResolver;
import java.net.URI;
import nl.mpi.archiving.corpusstructure.core.handle.HandleResolver;
import nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class HandleResolverWrapper implements PIDResolver {

    private final static Logger logger = LoggerFactory.getLogger(HandleResolverWrapper.class);
    private final HandleResolver handleResolver;

    public HandleResolverWrapper(HandleResolver handleResolver) {
        this.handleResolver = handleResolver;
    }

    @Override
    public URI resolve(URI uri) {
        try {
            return handleResolver.resolve(uri);
        } catch (InvalidHandleException ex) {
            logger.error("Cannot resolve", ex);
            return null;
        }
    }

}
