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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.clarin.linkchecker.persistence.model.Status;
import eu.clarin.linkchecker.persistence.model.Url;
import eu.clarin.linkchecker.persistence.service.StatusService;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class LinkcheckerAvailabilityStatusChecker implements ResourceAvailabilityStatusChecker {
    
    private final static Logger logger = LoggerFactory.getLogger(LinkcheckerAvailabilityStatusChecker.class);
    
    private final StatusService statusService;
    private final Consumer<Writer> statusWriter;
    private final Callable closeHandler;
    
    public LinkcheckerAvailabilityStatusChecker(StatusService statusService, Consumer<Writer> satusWriter, Callable closeHandler) {
        this.statusService = statusService;
        this.statusWriter = satusWriter;
        this.closeHandler = closeHandler;
    }
    
    @Override
    public Map<String, LinkStatus> getLinkStatusForRefs(Stream<String> hrefs) throws IOException {
        final Map<String, Status> status = statusService.getStatus(hrefs.toArray(String[]::new));
        return ImmutableMap.copyOf(Maps.transformEntries(status, (k, v) -> newLinkStatus(v)));
    }
    
    @Override
    public void writeStatusSummary(Writer writer) throws IOException {
        if (statusWriter != null) {
            statusWriter.accept(writer);
        }
    }
    
    @Override
    public void close() throws IOException {
        try {
            if (closeHandler != null) {
                closeHandler.call();
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error while closing status checker", ex);
        }
    }

    /**
     * Materialize a linkchecker Status into a LinkStatus object
     *
     * @param status
     * @return materialized LinkStatus
     */
    private static LinkStatus newLinkStatus(Status status) {
        final Url url = status.getUrl();
        if (url == null) {
            throw new NullPointerException("Null URL in status " + status);
        }
        return new BasicLinkStatus(url.getName(), status.getStatusCode(), status.getCheckingDate(), status.getContentType());
    }
    
}
