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
package eu.clarin.cmdi.vlo.importer.linkcheck;

import com.google.common.collect.Maps;
import eu.clarin.linkchecker.persistence.model.Status;
import eu.clarin.linkchecker.persistence.service.StatusService;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class LinkcheckerAvailabilityStatusChecker implements ResourceAvailabilityStatusChecker {

    private final StatusService statusService;

    public LinkcheckerAvailabilityStatusChecker(StatusService statusService) {
        this.statusService = statusService;
    }

    @Override
    public Map<String, LinkStatus> getLinkStatusForRefs(Stream<String> hrefs) throws IOException {
        final Map<String, Status> status = statusService.getStatus(hrefs.toArray(String[]::new));
        return Maps.transformEntries(status, (k, v) -> new LinkcheckerLinkStatus(v));
    }

    @Override
    public void writeStatusSummary(Writer writer) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private static class LinkcheckerLinkStatus implements LinkStatus {

        private final Status status;

        public LinkcheckerLinkStatus(Status status) {
            this.status = status;
        }

        @Override
        public String getUrl() {
            return status.getUrl().toString();
        }

        @Override
        public Integer getStatus() {
            return status.getStatusCode();
        }

        @Override
        public LocalDateTime getCheckingDate() {
            return status.getCheckingDate();
        }

        @Override
        public String getContentType() {
            return status.getContentType();
        }
    }

}
