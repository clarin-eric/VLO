/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public final class Pagination {

    protected Pagination() {
    }

    /**
     *
     * @param offset 1-based index of first result
     * @param size size of result set
     * @return
     */
    public static Pageable pageRequestFor(int offset, int size) {
        try {
            int page = Math.floorDiv(offset - 1, size);

            return PageRequest.of(page, size);
        } catch (ArithmeticException ex) {
            throw new ArithmeticException("Arithmetic exception while converting offset / size to Pageable" + ex.getMessage());
        }
    }

}
