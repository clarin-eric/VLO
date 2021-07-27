/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.vlo.importer.linkcheck;

import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.linkResources.CheckedLinkResource;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public abstract class RasaResourceAvailabilityStatusChecker implements ResourceAvailabilityStatusChecker {

    protected final static Logger logger = LoggerFactory.getLogger(RasaResourceAvailabilityStatusChecker.class);

    private final CheckedLinkResource checkedLinkResource;

    public RasaResourceAvailabilityStatusChecker(CheckedLinkResource checkedLinkResource) {
        this.checkedLinkResource = checkedLinkResource;
    }

    @Override
    public Map<String, CheckedLink> getLinkStatusForRefs(Stream<String> hrefs) throws IOException {
        try {
            return checkedLinkResource.get(hrefs.collect(Collectors.toSet()), Optional.empty());
        } catch (SQLException ex) {
            throw new IOException("Could not retrieve link status", ex);
        }
    }

    /**
     * Override to implement logic to be executed on close
     * @throws java.io.IOException
     */
    public void onClose() throws IOException {
        // do nothing
    }
    
    @Override
    public final void close() throws IOException {
        onClose();
    }
}
