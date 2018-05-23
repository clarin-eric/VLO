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

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.service.RatingStore;
import eu.clarin.cmdi.vlo.wicket.model.RatingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RethinkRatingStore implements RatingStore {

    public static final Logger logger = LoggerFactory.getLogger(RethinkRatingStore.class);

    private final VloConfig vloConfig;

    public RethinkRatingStore(VloConfig vloConfig) {
        this.vloConfig = vloConfig;
    }

    @Override
    public void storeRating(RatingLevel rating, String comment) {
        final long now = System.currentTimeMillis();
        logger.debug("Storing rating and comment: '{}', '{}', '{}'", now, rating.getDescription(), comment);
    }

}
