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
import eu.clarin.cmdi.rasa.filters.CheckedLinkFilter;
import eu.clarin.cmdi.rasa.linkResources.CheckedLinkResource;
import eu.clarin.cmdi.vlo.importer.linkcheck.RasaResourceAvailabilityStatusChecker.RasaResourceAvailabilityStatusCheckerConfiguration;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;
import static org.jmock.AbstractExpectations.any;
import static org.jmock.AbstractExpectations.returnValue;
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

    private final ImmutableMap<String, CheckedLink> checkedLinksMap = ImmutableMap.<String, CheckedLink>builder()
                .put(createResponseMapEntry("http://uri1", 200))
                .put(createResponseMapEntry("http://uri2", 404))
                .build();
    
    @Before
    public void setUp() throws SQLException {
        checkedLinkResource = context.mock(CheckedLinkResource.class);
        checkedLinkFilter = context.mock(CheckedLinkFilter.class);

        instance = new RasaResourceAvailabilityStatusChecker(checkedLinkResource,
                new RasaResourceAvailabilityStatusCheckerConfiguration(Duration.ofDays(10))) {
            @Override
            public void writeStatusSummary(Writer writer) throws IOException {
                writer.write("Status - test " + getClass());
            }
        };
    }

    /**
     * Test of getLinkStatusForRefs method, of class
     * RasaResourceAvailabilityStatusChecker.
     */
    @Test
    public void testGetLinkStatusForRefs() throws Exception {        
        context.checking(new Expectations() {
            {
                atLeast(1).of(checkedLinkResource).getCheckedLinkFilter();
                will(returnValue(checkedLinkFilter));

                atLeast(1).of(checkedLinkResource).getMap(with(any(CheckedLinkFilter.class)));
                will(returnValue(checkedLinksMap));

                oneOf(checkedLinkFilter).setUrlIn(with(any(String[].class)));

                oneOf(checkedLinkFilter).setCheckedBetween(with(any(Timestamp.class)), with(any(Timestamp.class)));

            }
        });
        
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

    public static AbstractMap.SimpleImmutableEntry<String, CheckedLink> createResponseMapEntry(
            String url, int status) {
        final CheckedLink checkedLink = new CheckedLink();
        checkedLink.setUrl(url);
        checkedLink.setStatus(status);
        return new AbstractMap.SimpleImmutableEntry<>(url, checkedLink);
    }
}
