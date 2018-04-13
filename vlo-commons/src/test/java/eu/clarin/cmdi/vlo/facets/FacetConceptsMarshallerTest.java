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
package eu.clarin.cmdi.vlo.facets;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class FacetConceptsMarshallerTest {

    public FacetConceptsMarshallerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of unmarshal method, of class FacetConceptsMarshaller.
     */
    @Test
    public void testUnmarshal() throws Exception {
        final Source source = new StreamSource(getClass().getResourceAsStream("/facetConcepts-test.xml"));
        final FacetConceptsMarshaller instance = new FacetConceptsMarshaller();

        final FacetConcepts result = instance.unmarshal(source);
        assertEquals(24, result.getFacetConcept().size());

        final FacetConcept concept = result.getFacetConcept().get(3);
        assertEquals("projectName", concept.getName());
        assertEquals("The project within which the resource or tool was created", concept.getDescription());
    }

}
