/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.vlo.importer.linkcheck;

import eu.clarin.cmdi.vlo.importer.linkcheck.AvailabilityScoreAccumulator;
import eu.clarin.cmdi.rasa.links.CheckedLink;
import eu.clarin.cmdi.vlo.ResourceAvailabilityScore;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class AvailabilityScoreAccumulatorTest {

    private AvailabilityScoreAccumulator instance;
    private Map<URI, CheckedLink> map;

    @Before
    public void setUp() {
        instance = new AvailabilityScoreAccumulator();
        map = new HashMap<>();
    }

    @Test
    public void testCalculateAvailabilityScoreNoData() {
        assert map.isEmpty();

        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.UNKNOWN, result);
    }

    @Test
    public void testCalculateAvailabilityScoreAllUnknown() {
        addStatus(0, 0);
        assert map.size() == 2;
        
        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.UNKNOWN, result);
    }

    @Test
    public void testCalculateAvailabilityScoreAllAvailable() {
        addStatus(200, 200, 301, 302, 303);
        assert map.size() == 5;
        
        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.ALL_AVAILABLE, result);
    }

    @Test
    public void testCalculateAvailabilityScoreAllUnavailable() {
        addStatus(400, 403, 404, 500);
        assert map.size() == 4;
        
        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.ALL_UNAVAILABLE, result);
    }

    @Test
    public void testCalculateAvailabilityScoreMostUnavailable() {
        addStatus(400, 403, 500, 200, 301);
        assert map.size() == 5;
        
        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.MOST_UNAVAILABLE, result);
    }
    
    @Test
    public void testCalculateAvailabilityScoreMostRestricted() {
        addStatus(401, 401, 403, 200, 200);
        assert map.size() == 5;
        
        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.MOST_RESTRICTED_ACCESS, result);
    }
    
    @Test
    public void testCalculateAvailabilityScoreMostUnavailableWithRestricted() {
        addStatus(401, 401, 403, 404, 200);
        assert map.size() == 5;
        
        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.MOST_UNAVAILABLE, result);
    }

    @Test
    public void testCalculateAvailabilityScoreSomeUnavailable() {
        addStatus(400, 404, 200, 301);
        assert map.size() == 4;
        
        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.SOME_UNAVAILABLE, result);
    }

    @Test
    public void testCalculateAvailabilityScoreSomeUnavailableWithRestricted() {
        addStatus(400, 403, 200, 301);
        assert map.size() == 4;
        
        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.SOME_UNAVAILABLE, result);
    }
    
    @Test
    public void testCalculateAvailabilityScoreSomeRestricted() {
        addStatus(401, 403, 200, 301, 303);
        assert map.size() == 5;
        
        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map);
        assertEquals(ResourceAvailabilityScore.SOME_RESTRICTED_ACCESS, result);
    }

    private void addStatus(int... statuses) {
        for (int status : statuses) {
            addStatus(status);
        }
    }

    private void addStatus(int status) {
        addStatus("uri" + map.size(), status);
    }

    private void addStatus(String uri, int status) {
        final CheckedLink checkedLink = new CheckedLink();
        checkedLink.setUrl(uri);
        checkedLink.setStatus(status);
        map.put(URI.create(uri), checkedLink);
    }

}
