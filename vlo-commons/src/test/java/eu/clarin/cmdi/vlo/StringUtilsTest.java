/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class StringUtilsTest {

    /**
     * Test of toMultiLineHtml method, of class StringUtils.
     */
    @Test
    public void testToMultiLineHtml() {
        System.out.println("toMultiLineHtml");
        String s = "what\nis\n\rthis\n\n\n\n\n\nlater";
        String expResult = "what<br/>is<br/>this<br/><br/>later";
        CharSequence result = StringUtils.toMultiLineHtml(s);
        assertEquals(expResult, result.toString());
    }

    /**
     * Test of normalizeIdString method, of class StringUtils.
     */
    @Test
    public void testNormalizeIdString() {
        System.out.println("normalizeIdString");
        String idString = "a!b*c\\d(e)f;g:h@i&j=k+l$m,n/o?p#q[r]s";
        String expResult = "a_33_b_42_c\\d_40_e_41_f_59_g_58_h_64_i_38_j_61_k_43_l_36_m_44_n_47_o_63_p_35_q_91_r_93_s";
        String result = StringUtils.normalizeIdString(idString);
        assertEquals(expResult, result);
    }

    /**
     * Test of uncapitalizeFirstLetter method, of class StringUtils.
     */
    @Test
    public void testUncapitalizeFirstLetter() {
        System.out.println("uncapitalizeFirstLetter");
        String value = "A quick brown fox";
        String expResult = "a quick brown fox";
        String result = StringUtils.uncapitalizeFirstLetter(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of capitalizeFirstLetter method, of class StringUtils.
     */
    @Test
    public void testCapitalizeFirstLetter() {
        System.out.println("capitalizeFirstLetter");
        String value = "etaoin shrdlu";
        String expResult = "Etaoin shrdlu";
        String result = StringUtils.capitalizeFirstLetter(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of createStringFromArray method, of class StringUtils.
     */
    @Test
    public void testCreateStringFromArray() {
        System.out.println("createStringFromArray");
        String[] values = new String[]{"Lorem ipsum dolor sit amet, ", "consectetur adipiscing elit, ", "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."};
        String expResult = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
        String result = StringUtils.createStringFromArray(values);
        assertEquals(expResult, result);
    }

}
