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

import eu.clarin.cmdi.vlo.ResourceAvailabilityScore;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class AvailabilityScoreAccumulatorTest {

    private AvailabilityScoreAccumulator instance;
    private Map<URI, LinkStatus> map;

    @BeforeEach
    public void setUp() {
        instance = new AvailabilityScoreAccumulator();
        map = new HashMap<>();
    }

    @Test
    public void testCalculateAvailabilityScoreNoData() {
        assert map.isEmpty();

        {
            final ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
            assertEquals(ResourceAvailabilityScore.UNKNOWN, result);
        }

        // total number of resources should not make a difference
        {
            final ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, 100);
            assertEquals(ResourceAvailabilityScore.UNKNOWN, result);
        }
    }

    @Test
    public void testCalculateAvailabilityScoreAllUnknown() {
        addStatus(0, 0);
        assert map.size() == 2;

        {
            final ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
            assertEquals(ResourceAvailabilityScore.UNKNOWN, result);
        }

        // total number of resources should not make a difference
        {
            ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, 100);
            assertEquals(ResourceAvailabilityScore.UNKNOWN, result);
        }
    }

    @Test
    public void testCalculateAvailabilityScoreEffecitvelyUnknown() {
        addStatus(200, 200);
        assert map.size() == 2;

        {
            // all available but too few known statuses compared to total number of resources
            final ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, 100);
            assertEquals(ResourceAvailabilityScore.UNKNOWN, result);
        }

        addStatus(0, 0, 0);
        {
            // too few known statuses compared to total number of resources (although it equals map size)
            final ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
            assertEquals(ResourceAvailabilityScore.UNKNOWN, result);
        }
    }

    @Test
    public void testCalculateAvailabilityScoreAllAvailable() {
        addStatus(200, 200, 301, 302, 303);
        assert map.size() == 5;

        {
            ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
            assertEquals(ResourceAvailabilityScore.ALL_AVAILABLE, result);
        }

        //adding unknown should NOT result in 'all unavailable'
        {
            ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size() * 2);
            assertNotEquals(ResourceAvailabilityScore.ALL_AVAILABLE, result);
        }
    }

    @Test
    public void testCalculateAvailabilityScoreAllUnavailable() {
        addStatus(400, 403, 404, 500);
        assert map.size() == 4;

        {
            ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
            assertEquals(ResourceAvailabilityScore.ALL_UNAVAILABLE, result);
        }

        //adding one unknown should NOT result in 'all unavailable'
        {
            ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size() + 1);
            assertNotEquals(ResourceAvailabilityScore.ALL_UNAVAILABLE, result);
        }
    }

    @Test
    public void testCalculateAvailabilityScoreMostUnavailable() {
        addStatus(400, 403, 500, 200, 301);
        assert map.size() == 5;

        {
            ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
            assertEquals(ResourceAvailabilityScore.MOST_UNAVAILABLE, result);
        }

        //adding one unknown should NOT result in 'most unavailable'
        {
            ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size() + 1);
            assertNotEquals(ResourceAvailabilityScore.MOST_UNAVAILABLE, result);
        }
    }

    @Test
    public void testCalculateAvailabilityScoreMostRestricted() {
        addStatus(401, 401, 403, 200, 200);
        assert map.size() == 5;

        {
            ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
            assertEquals(ResourceAvailabilityScore.MOST_RESTRICTED_ACCESS, result);
        }

        //adding one unknown should NOT result in 'most restricted'
        {
            ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size() + 1);
            assertNotEquals(ResourceAvailabilityScore.MOST_RESTRICTED_ACCESS, result);
        }
    }

    @Test
    public void testCalculateAvailabilityScoreMostUnavailableWithRestricted() {
        addStatus(401, 401, 403, 404, 200);
        assert map.size() == 5;

        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
        assertEquals(ResourceAvailabilityScore.MOST_UNAVAILABLE, result);
    }

    @Test
    public void testCalculateAvailabilityScoreSomeUnavailable() {
        addStatus(400, 404, 200, 301);
        assert map.size() == 4;

        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
        assertEquals(ResourceAvailabilityScore.SOME_UNAVAILABLE, result);
    }

    @Test
    public void testCalculateAvailabilityScoreSomeUnavailableWithRestricted() {
        addStatus(400, 403, 200, 301);
        assert map.size() == 4;

        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
        assertEquals(ResourceAvailabilityScore.SOME_UNAVAILABLE, result);
    }

    @Test
    public void testCalculateAvailabilityScoreSomeRestricted() {
        addStatus(401, 403, 200, 301, 303);
        assert map.size() == 5;

        ResourceAvailabilityScore result = instance.calculateAvailabilityScore(map, map.size());
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
        final LinkStatus checkedLink = new LinkStatus() {
            @Override
            public String getUrl() {
                return uri;
            }

            @Override
            public Integer getStatus() {
                return status;
            }

            @Override
            public LocalDateTime getCheckingDate() {
                return null;
            }

            @Override
            public String getContentType() {
                return null;
            }

        };
        map.put(URI.create(uri), checkedLink);
    }

}
