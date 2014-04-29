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
package eu.clarin.cmdi.vlo.wicket.model;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author twagoo
 */
public class TruncatingStringModelTest {

    public static final int truncatePoint = 2;
    public static final int maxLength = 5;
    private IModel<String> innerModel;
    private TruncatingStringModel instance;

    @Before
    public void setUp() {
        innerModel = Model.of("");
        instance = new TruncatingStringModel(innerModel, maxLength, truncatePoint);
    }

    @Test
    public void testGetObjectFits() {
        innerModel.setObject("abcd"); // 4 characters < max
        // should not truncate
        assertEquals("abcd", instance.getObject());

        innerModel.setObject("abcde"); // 5 characters == max
        // should not truncate
        assertEquals("abcde", instance.getObject());
    }

    @Test
    public void testGetObjectTruncated() {
        innerModel.setObject("abcdef"); // 6 characters == max + 1
        // should truncate at truncate point
        assertEquals("ab\u2026", instance.getObject());
    }

    @Test
    public void testGetObjectNull() {
        innerModel.setObject(null);
        assertNull(instance.getObject());
    }

}
