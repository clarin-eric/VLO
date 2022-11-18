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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public final class RasaResourceAvailabilityStatusCheckerConfiguration {
    
    private final Timestamp ageLimitLowerBound;
    private final Optional<Timestamp> ageLimitUpperBound;

    /**
     *
     * @param checkAgeThreshold Maximum allowed age of link checking information.
     */
    public RasaResourceAvailabilityStatusCheckerConfiguration(TemporalAmount checkAgeThreshold) {
        if (checkAgeThreshold.get(ChronoUnit.SECONDS) < 1) {
            throw new IllegalArgumentException("checkAgeThreshold can not be less than 1 day");
        }
        ageLimitLowerBound = Timestamp.from(Instant.now().minus(checkAgeThreshold));
        ageLimitUpperBound = Optional.empty();
    }

    public Timestamp getAgeLimitLowerBound() {
        return ageLimitLowerBound;
    }

    public Timestamp getAgeLimitUpperBound() {
        return ageLimitUpperBound.orElse(Timestamp.from(Instant.now()));
    }
    
}
