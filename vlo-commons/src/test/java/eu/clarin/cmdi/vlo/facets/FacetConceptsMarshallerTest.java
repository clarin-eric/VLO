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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class FacetConceptsMarshallerTest {

    public FacetConceptsMarshallerTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
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
        assertEquals(25, result.getFacetConcept().size());

        final FacetConcept concept = result.getFacetConcept().get(3);
        assertEquals("projectName", concept.getName());
    }

}
