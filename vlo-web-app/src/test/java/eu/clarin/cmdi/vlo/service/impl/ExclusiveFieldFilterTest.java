/*
 * Copyright (C) 2014 CLARIN
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

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class ExclusiveFieldFilterTest {

    /**
     * Test of allowField method, of class ExclusiveFieldFilter.
     */
    @Test
    public void testAllowField() {
        final ExclusiveFieldFilter instance = new ExclusiveFieldFilter(Arrays.asList("exclude 1", "exclude 2"));
        //explicitly excluded
        assertFalse(instance.allowField("exclude 1"));
        //explicitly excluded
        assertFalse(instance.allowField("exclude 2"));
        //all fields starting with _ are excluded too
        assertFalse(instance.allowField("_other"));
        //other fields should be allowed
        assertTrue(instance.allowField("other"));
    }

}
