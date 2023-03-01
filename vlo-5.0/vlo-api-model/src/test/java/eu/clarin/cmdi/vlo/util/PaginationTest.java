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
package eu.clarin.cmdi.vlo.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author twagoo
 */
public class PaginationTest {

    /**
     * Test of pageRequestFor method, of class Pagination.
     */
    @Test
    public void testPageRequest_f0_s10() {
        Pageable result = Pagination.pageRequestFor(0, 10);
        assertEquals(10, result.getPageSize());
        assertEquals(0, result.getPageNumber());
        assertEquals(0, result.getOffset());
    }

    @Test
    public void testPageRequest_f10_s10() {
        Pageable result = Pagination.pageRequestFor(10, 10);
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getPageNumber());
        assertEquals(10, result.getOffset());
    }

    @Test
    public void testPageRequest_f25_s10() {
        Pageable result = Pagination.pageRequestFor(25, 10);
        assertEquals(10, result.getPageSize());
        assertEquals(2, result.getPageNumber());
        assertEquals(20, result.getOffset());
    }

}
