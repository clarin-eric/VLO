/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.api.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author twagoo
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@ToString
public class VloRecordsRequest {

    /**
     * query for filtering (may be null or empty)
     */
    private final String query;
    /**
     * filter queries in the format 'field -> [OR] values' (may
     */
    private final Map<String, ? extends Iterable<String>> filters;
    /**
     * records to skip (0 to request from first)
     */
    private final int from;
    /**
     * records to include in the results
     */
    private final int size;

}
