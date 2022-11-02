/*
 * Copyright (C) 2022 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.mapping;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class XPathUtilsTest {

    /**
     * Test of normalize method, of class XPathUtils.
     */
    @Test
    public void testNormalize() {
        {
            String xpath = "test";
            String expResult = "./test";
            String result = XPathUtils.normalize(xpath);
            assertEquals(expResult, result);
        }
        
        {
            String xpath = "test1|test2";
            String expResult = "./test1|./test2";
            String result = XPathUtils.normalize(xpath);
            assertEquals(expResult, result);
        }

        {
            String xpath = "/test";
            String expResult = "/test";
            String result = XPathUtils.normalize(xpath);
            assertEquals(expResult, result);
        }

        {
            String xpath = " /test  ";
            String expResult = "/test";
            String result = XPathUtils.normalize(xpath);
            assertEquals(expResult, result);
        }

        {
            String xpath = " /test1|test2  ";
            String expResult = "/test1|./test2";
            String result = XPathUtils.normalize(xpath);
            assertEquals(expResult, result);
        }
    }

}
