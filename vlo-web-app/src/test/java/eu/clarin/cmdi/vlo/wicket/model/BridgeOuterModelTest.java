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
public class BridgeOuterModelTest {

    /**
     * Test of getObject method, of class BridgeOuterModel.
     */
    @Test
    public void testGetObject() {
        IModel<String> inner = Model.of("inner value");
        IModel<Boolean> state = Model.of(false);

        BridgeOuterModel instance = new BridgeOuterModel(inner, state, "initial outer");

        assertEquals("initial outer", instance.getObject());

        //open bridge (should not affect value)
        state.setObject(true);
        assertEquals("initial outer", instance.getObject());

        //close bridge again (should not affect value)
        state.setObject(false);
        assertEquals("initial outer", instance.getObject());
    }

    /**
     * Test of setObject method, of class BridgeOuterModel.
     */
    @Test
    public void testSetObject() {
        IModel<String> inner = Model.of("inner value");
        IModel<Boolean> state = Model.of(false);

        BridgeOuterModel instance = new BridgeOuterModel(inner, state, "initial outer");

        // set while bridge closed
        instance.setObject("new outer");
        assertEquals("inner value", inner.getObject());

        // open bridge
        state.setObject(true);
        // this does not change the inner value until a value is set on the outer
        assertEquals("inner value", inner.getObject());
        // set a new value
        instance.setObject("new outer/inner");
        // should have been transferred to the inner
        assertEquals("new outer/inner", inner.getObject());
    }

}
