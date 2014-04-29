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
public class BridgeModelTest {

    /**
     * Test of getObject method, of class BridgeModel.
     */
    @Test
    public void testGetObject() {
        IModel<String> inner = Model.of("inner value");
        IModel<String> outer = Model.of("outer value");
        IModel<Boolean> state = Model.of(false);
        String falseValue = "false";

        BridgeModel instance = new BridgeModel(inner, outer, state, falseValue);

        assertFalse(instance.getObject());
        instance.setObject(true);
        assertTrue(instance.getObject());
    }

    /**
     * Test of setObject method, of class BridgeModel.
     */
    @Test
    public void testSetObject() {
        IModel<String> inner = Model.of("inner value");
        IModel<String> outer = Model.of("outer value");
        IModel<Boolean> state = Model.of(false);
        String falseValue = "bridge closed";

        BridgeModel instance = new BridgeModel(inner, outer, state, falseValue);

        // initial values
        assertEquals("inner value", inner.getObject());
        assertFalse(state.getObject());

        //open the bridge
        instance.setObject(true);
        assertTrue(state.getObject());
        assertEquals("outer value", inner.getObject());
        
        // close bridge
        instance.setObject(false);
        assertFalse(state.getObject());
        assertEquals("bridge closed", inner.getObject());
    }

}
