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

import com.google.common.collect.Maps;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.filters.CheckedLinkFilter;
import eu.clarin.cmdi.rasa.linkResources.CheckedLinkResource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
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
    private final RasaResourceAvailabilityStatusCheckerConfiguration config;

    public RasaResourceAvailabilityStatusChecker(CheckedLinkResource checkedLinkResource, RasaResourceAvailabilityStatusCheckerConfiguration config) {
        this.checkedLinkResource = checkedLinkResource;
        this.config = config;
    }

    @Override
    public Map<String, LinkStatus> getLinkStatusForRefs(Stream<String> hrefs) throws IOException {
        try {
            final CheckedLinkFilter filter = checkedLinkResource.getCheckedLinkFilter();
            filter.setUrlIn(hrefs.toArray(String[]::new));
            filter.setCheckedBetween(config.getAgeLimitLowerBound(), config.getAgeLimitUpperBound());
            return Maps.transformEntries(checkedLinkResource.getMap(filter), (k, v) -> new RasaLinkStatus(v));
        } catch (SQLException ex) {
            throw new IOException("Could not retrieve link status", ex);
        }
    }

    /**
     * Override to implement logic to be executed on close
     *
     * @throws java.io.IOException
     */
    public void onClose() throws IOException {
        // do nothing
    }

    @Override
    public final void close() throws IOException {
        onClose();
    }

    private static class RasaLinkStatus implements LinkStatus {

        private final CheckedLink checkedLink;

        public RasaLinkStatus(CheckedLink checkedLink) {
            this.checkedLink = checkedLink;
        }

        @Override
        public String getUrl() {
            return checkedLink.getUrl();
        }

        @Override
        public Integer getStatus() {
            return checkedLink.getStatus();
        }

        @Override
        public LocalDateTime getCheckingDate() {
            return checkedLink.getCheckingDate().toLocalDateTime();
        }

        @Override
        public String getContentType() {
            return checkedLink.getContentType();
        }

    }

}
