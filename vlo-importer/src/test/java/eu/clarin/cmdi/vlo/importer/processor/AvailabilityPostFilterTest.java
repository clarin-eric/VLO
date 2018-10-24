/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.importer.processor;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class AvailabilityPostFilterTest {

    AvailabilityPostFilter instance;

    final TargetFacet availabilityTargetFacet = new TargetFacet(new FacetConfiguration(null, "availability"), null);
    final TargetFacet licenseType = new TargetFacet(new FacetConfiguration(null, "licenseType"), null);

    @Before
    public void setUp() {
        instance = new AvailabilityPostFilter("availability", "licenseType");
    }

    @Test
    public void testFilterSimple() {
        final FacetValuesMap map = new FacetValuesMap();
        map.addToTarget(new ValueSet(0, null, availabilityTargetFacet, Pair.of("PUB", "und"), true, true));
        map.addToTarget(new ValueSet(0, null, availabilityTargetFacet, Pair.of("RES", "und"), true, true));
        map.addToTarget(new ValueSet(0, null, availabilityTargetFacet, Pair.of("ACA", "und"), true, true));

        instance.filter(map);
        final List<ValueSet> availability = map.get("availability");
        assertEquals(1, availability.size());
        assertEquals("RES", availability.get(0).getValue());
    }

    @Test
    public void testFilterOtherTag() {
        final FacetValuesMap map = new FacetValuesMap();
        map.addToTarget(new ValueSet(0, null, availabilityTargetFacet, Pair.of("BLA", "und"), true, true));
        map.addToTarget(new ValueSet(0, null, availabilityTargetFacet, Pair.of("VLA", "und"), true, true));

        instance.filter(map);
        final List<ValueSet> availability = map.get("availability");
        assertEquals(2, availability.size());

        final Iterator<ValueSet> iterator = availability.iterator();
        assertEquals("BLA", iterator.next().getValue());
        assertEquals("VLA", iterator.next().getValue());
    }

    @Test
    public void testFilterMixed() {
        final FacetValuesMap map = new FacetValuesMap();
        map.addToTarget(new ValueSet(0, null, availabilityTargetFacet, Pair.of("PUB", "und"), true, true));
        map.addToTarget(new ValueSet(0, null, availabilityTargetFacet, Pair.of("BLA", "und"), true, true));
        map.addToTarget(new ValueSet(0, null, availabilityTargetFacet, Pair.of("RES", "und"), true, true));
        map.addToTarget(new ValueSet(0, null, availabilityTargetFacet, Pair.of("VLA", "und"), true, true));

        instance.filter(map);
        final List<ValueSet> availability = map.get("availability");
        assertEquals(3, availability.size());

        final Iterator<ValueSet> iterator = availability.iterator();
        assertEquals("RES", iterator.next().getValue());
        assertEquals("BLA", iterator.next().getValue());
        assertEquals("VLA", iterator.next().getValue());
    }

}
