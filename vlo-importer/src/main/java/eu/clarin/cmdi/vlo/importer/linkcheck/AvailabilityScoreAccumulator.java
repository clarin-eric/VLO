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

import com.google.common.base.Predicates;
import eu.clarin.cmdi.vlo.ResourceAvailabilityScore;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class AvailabilityScoreAccumulator {

    public ResourceAvailabilityScore calculateAvailabilityScore(Map<?, LinkStatus> linkStatusMap, int allResourcesCount) {
        final Supplier<Stream<Integer>> knownStatusStreamProvider
                = () -> linkStatusMap.values().stream()
                        .map(LinkStatus::getStatus)
                        .filter((status) -> (status != null && status > 0));

        if (!knownStatusStreamProvider.get().findAny().isPresent()) {
            //only unkown status information
            return ResourceAvailabilityScore.UNKNOWN;
        } else {
            final Collection<Integer> knownStatus = knownStatusStreamProvider.get().collect(Collectors.toList());
            return calculateScore(knownStatus, allResourcesCount);
        }
    }

    private ResourceAvailabilityScore calculateScore(final Collection<Integer> knownStatuses, int allResourcesCount) {
        final int knownCount = knownStatuses.size();

        //any resources that are not openly available?
        if (knownStatuses.stream().anyMatch(this::isNotAvailable)) {
            //no majority of restricted/procted resources; availability?

            if (knownStatuses.stream().filter(this::isNotAvailable).anyMatch(Predicates.not(this::isRestricted))) {
                //one or more unavailable but not restricted (this takes priority)
                final long unavailableCount = knownStatuses.stream().filter(this::isNotAvailable).count();
                if (unavailableCount == allResourcesCount) { //TODO: only if unavailableCount == (knownCount + unknownCount)
                    return ResourceAvailabilityScore.ALL_UNAVAILABLE;
                } else if (unavailableCount * 2 > allResourcesCount) {
                    return ResourceAvailabilityScore.MOST_UNAVAILABLE;
                } else {
                    return ResourceAvailabilityScore.SOME_UNAVAILABLE;
                }
            } else {
                //all unavailable resources are restricted (i.e. no 404 etc)
                final long restrictedCount = knownStatuses.stream().filter(this::isRestricted).limit(1 + knownCount / 2).count();
                if (restrictedCount * 2 > allResourcesCount) {
                    //more than half are restricted
                    return ResourceAvailabilityScore.MOST_RESTRICTED_ACCESS;
                } else {
                    //less than half are restricted
                    return ResourceAvailabilityScore.SOME_RESTRICTED_ACCESS;
                }
            }
        } else if (knownCount * 2 > allResourcesCount) {
            //more than half known and all available - assume available
            return ResourceAvailabilityScore.ALL_AVAILABLE;
        } else {
            //not enough information to give a score
            return ResourceAvailabilityScore.UNKNOWN;
        }
    }

    private boolean isNotAvailable(int status) {
        return status >= 400;
    }

    private boolean isRestricted(int status) {
        return status == 401 || status == 403;
    }
}
