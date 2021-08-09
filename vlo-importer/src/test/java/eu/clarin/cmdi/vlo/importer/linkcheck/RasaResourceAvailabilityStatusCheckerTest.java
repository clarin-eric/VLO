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

import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.rasa.DAO.Statistics.CategoryStatistics;
import eu.clarin.cmdi.rasa.DAO.Statistics.Statistics;
import eu.clarin.cmdi.rasa.DAO.Statistics.StatusStatistics;
import eu.clarin.cmdi.rasa.filters.CheckedLinkFilter;
import eu.clarin.cmdi.rasa.linkResources.CheckedLinkResource;
import eu.clarin.cmdi.vlo.importer.linkcheck.RasaResourceAvailabilityStatusChecker.RasaResourceAvailabilityStatusCheckerConfiguration;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RasaResourceAvailabilityStatusCheckerTest {

    private final Mockery context = new JUnit4Mockery();
    
    private CheckedLinkResource checkedLinkResource;
    private RasaResourceAvailabilityStatusChecker instance;
    private CheckedLinkFilter checkedLinkFilter;

    @Before
    public void setUp() {
        checkedLinkFilter = context.mock(CheckedLinkFilter.class);
        context.checking(new Expectations(){{
            allowing(checkedLinkFilter).setUrlIn(with(any(String[].class)));
            allowing(checkedLinkFilter).setCheckedBetween(with(any(Timestamp.class)), with(any(Timestamp.class)));
        }});
        
        checkedLinkResource
                = new CheckedLinkResource() {

            @Override
            public Map<String, CheckedLink> getMap(CheckedLinkFilter filter) throws SQLException {
                return ImmutableMap.<String, CheckedLink>builder()
                        .put(createResponseMapEntry("http://uri1", 200))
                        .put(createResponseMapEntry("http://uri2", 404))
                        .build();
            }

            @Override
            public Map<String, CheckedLink> get(
                    Collection<String> uri, Optional<CheckedLinkFilter> filter) {

                throw new UnsupportedOperationException("Not supported"); // not needed for test
            }

            @Override
            public Boolean save(CheckedLink checkedLink) throws SQLException {
                throw new UnsupportedOperationException("Not supported"); // not needed for test
            }

            @Override
            public Stream<CheckedLink> get(CheckedLinkFilter filter) throws SQLException {
                throw new UnsupportedOperationException("Not supported"); // not needed for test
            }

            @Override
            public int getCount(CheckedLinkFilter filter) throws SQLException {
                throw new UnsupportedOperationException("Not supported"); // not needed for test
            }

            @Override
            public Statistics getStatistics(CheckedLinkFilter filter) throws SQLException {
                throw new UnsupportedOperationException("Not supported"); // not needed for test
            }

            @Override
            public Stream<CategoryStatistics> getCategoryStatistics(CheckedLinkFilter filter)
                    throws SQLException {
                throw new UnsupportedOperationException("Not supported"); // not needed for test
            }

            @Override
            public Stream<StatusStatistics> getStatusStatistics(CheckedLinkFilter filter)
                    throws SQLException {
                throw new UnsupportedOperationException("Not supported"); // not needed for test
            }

            @Override
            public CheckedLinkFilter getCheckedLinkFilter() {
                return checkedLinkFilter;
            }
        };
        instance = new RasaResourceAvailabilityStatusChecker(checkedLinkResource,
                new RasaResourceAvailabilityStatusCheckerConfiguration(Duration.ofDays(10))
        ) {
            @Override
            public void writeStatusSummary(Writer writer) throws IOException {
                writer.write("Status - test " + getClass());
            }
        };
    }

    public static AbstractMap.SimpleImmutableEntry<String, CheckedLink> createResponseMapEntry(
            String url, int status) {
        final CheckedLink checkedLink = new CheckedLink();
        checkedLink.setUrl(url);
        checkedLink.setStatus(status);
        return new AbstractMap.SimpleImmutableEntry<>(url, checkedLink);
    }

    /**
     * Test of getLinkStatusForRefs method, of class
     * RasaResourceAvailabilityStatusChecker.
     */
    @Test
    public void testGetLinkStatusForRefs() throws IOException {
        System.out.println("getLinkStatusForRefs");
        Stream<String> hrefs = Stream.of("http://uri3", "http://uri2", "http://uri1");
        Map<String, CheckedLink> result = instance.getLinkStatusForRefs(hrefs);
        assertEquals(2, result.size());
        assertNotNull(result.get("http://uri1"));
        assertEquals("http://uri1", result.get("http://uri1").getUrl());
        assertNotNull(result.get("http://uri1").getStatus());
        assertEquals(200, result.get("http://uri1").getStatus().intValue());
        assertNotNull(result.get("http://uri2"));
        assertEquals("http://uri2", result.get("http://uri2").getUrl());
        assertNotNull(result.get("http://uri2").getStatus());
        assertEquals(404, result.get("http://uri2").getStatus().intValue());
    }
}
