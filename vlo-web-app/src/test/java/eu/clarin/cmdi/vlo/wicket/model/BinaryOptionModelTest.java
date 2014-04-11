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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class BinaryOptionModelTest {

    public BinaryOptionModelTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getObject method, of class BinaryOptionModel.
     */
    @Test
    public void testGetObject() {
        IModel<String> wrapped = Model.of("");
        IModel<String> trueModel = Model.of("true");
        IModel<String> falseModel = Model.of("false");
        BinaryOptionModel instance = new BinaryOptionModel(wrapped, falseModel, trueModel);

        assertFalse(instance.getObject());
        wrapped.setObject("true");
        assertTrue(instance.getObject());

        trueModel.setObject("newTrue");
        assertFalse(instance.getObject());
        wrapped.setObject("newTrue");
        assertTrue(instance.getObject());

        wrapped.setObject("false");
        assertFalse(instance.getObject());
    }

    /**
     * Test of setObject method, of class BinaryOptionModel.
     */
    @Test
    public void testSetObject() {
        IModel<String> wrapped = Model.of("");
        IModel<String> trueModel = Model.of("true");
        IModel<String> falseModel = Model.of("false");
        BinaryOptionModel instance = new BinaryOptionModel(wrapped, falseModel, trueModel);

        assertEquals("", wrapped.getObject());
        instance.setObject(true);
        assertEquals("true", wrapped.getObject());
        instance.setObject(false);
        assertEquals("false", wrapped.getObject());
    }

}
