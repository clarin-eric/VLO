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
package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.rasa.links.CheckedLink;
import eu.clarin.cmdi.vlo.ResourceAvailabilityScore;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class AvailabilityScoreAccumulator {

    public ResourceAvailabilityScore calculateAvailabilityScore(Map<URI, CheckedLink> linkStatusMap) {
        if (linkStatusMap.isEmpty()) {
            //no status information for any link
            return ResourceAvailabilityScore.UNKNOWN;
        } else {
            final Collection<Integer> knownStatus = linkStatusMap.values().stream()
                    .map(CheckedLink::getStatus)
                    .filter((status) -> (status != null && status > 0))
                    .collect(Collectors.toList());

            final long knownCount = knownStatus.size();
            if (knownCount == 0) {
                //only status information is unknown
                return ResourceAvailabilityScore.UNKNOWN;
            } else {
                //any resources that are not openly available?
                if (knownStatus.stream().anyMatch(this::isNotAvailable)) {
                    //no majority of restricted/procted resources; availability?
                    final long unavailableCount = knownStatus.stream().filter(this::isNotAvailable).count();
                    final long restrictedCount = knownStatus.stream().filter(this::isRestricted).count();

                    if (unavailableCount > restrictedCount) {
                        //one or more unavailable but not restricted (this takes priority)
                        if (unavailableCount == knownCount) {
                            return ResourceAvailabilityScore.ALL_UNAVAILABLE;
                        } else if (unavailableCount * 2 > knownCount) {
                            return ResourceAvailabilityScore.MOST_UNAVAILABLE;
                        } else {
                            return ResourceAvailabilityScore.SOME_UNAVAILABLE;
                        }
                    } else {
                        //all unavailable resources are restricted (i.e. no 404 etc)
                        if (restrictedCount * 2 > knownCount) {
                            //more than half are restricted
                            return ResourceAvailabilityScore.MOST_RESTRICTED_ACCESS;
                        } else {
                            //less than half are restricted
                            return ResourceAvailabilityScore.SOME_RESTRICTED_ACCESS;
                        }
                    }
                } else {
                    //all are available
                    return ResourceAvailabilityScore.ALL_AVAILABLE;
                }
            }
        }
    }

    private boolean isNotAvailable(int status) {
        return status >= 400;
    }

    private boolean isRestricted(int status) {
        return status == 401 || status == 403;
    }
}
