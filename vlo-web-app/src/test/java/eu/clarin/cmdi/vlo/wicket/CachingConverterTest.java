/*
 * Copyright (C) 2015 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import java.util.Locale;
import org.apache.wicket.util.convert.IConverter;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class CachingConverterTest {

    private final Mockery context = new JUnit4Mockery();

    /**
     * Test of convertToObject method, of class CachingConverter.
     */
    @Test
    public void testConvertToObject() {
        final IConverter<Object> inner = context.mock(IConverter.class, "Object");
        final CachingConverter instance = new CachingConverter<>(inner);
        final Object result = new Object();
        context.checking(new Expectations() {
            {
                // expect this call ONLY ONCE
                oneOf(inner).convertToObject("value", Locale.CANADA_FRENCH);
                will(returnValue(result));
            }
        });
        Object actual = instance.convertToObject("value", Locale.CANADA_FRENCH);
        assertSame(result, actual);
        // do again - cache should kick in
        actual = instance.convertToObject("value", Locale.CANADA_FRENCH);
        assertSame(result, actual);
        actual = instance.convertToObject("value", Locale.CANADA_FRENCH);
        assertSame(result, actual);

        // once more with different locale & value
        context.checking(new Expectations() {
            {
                oneOf(inner).convertToObject("value", Locale.FRENCH);
                will(returnValue(result));
                oneOf(inner).convertToObject("otherValue", Locale.FRENCH);
                will(returnValue(result));
            }
        });
        actual = instance.convertToObject("value", Locale.FRENCH);
        assertSame(result, actual);
        actual = instance.convertToObject("otherValue", Locale.FRENCH);
        assertSame(result, actual);
    }

    /**
     * Test of convertToString method, of class CachingConverter.
     */
    @Test
    public void testConvertToString() {
        final IConverter<Object> inner = context.mock(IConverter.class, "Object");
        final CachingConverter instance = new CachingConverter<>(inner);
        final Object value = new Object();
        final String result = "result";
        context.checking(new Expectations() {
            {
                // expect this call ONLY ONCE
                oneOf(inner).convertToString(value, Locale.PRC);
                will(returnValue(result));
            }
        });
        String actual = instance.convertToString(value, Locale.PRC);
        assertSame(result, actual);
        // do again - cache should kick in
        actual = instance.convertToString(value, Locale.PRC);
        assertSame(result, actual);
        actual = instance.convertToString(value, Locale.PRC);
        assertSame(result, actual);

        // once more with different locale & value
        final Object value2 = new Object();
        context.checking(new Expectations() {
            {
                oneOf(inner).convertToString(value, Locale.CHINA);
                will(returnValue(result));
                oneOf(inner).convertToString(value2, Locale.CHINA);
                will(returnValue(result));
            }
        });
        actual = instance.convertToString(value, Locale.CHINA);
        assertSame(result, actual);
        actual = instance.convertToString(value2, Locale.CHINA);
        assertSame(result, actual);
    }

}
