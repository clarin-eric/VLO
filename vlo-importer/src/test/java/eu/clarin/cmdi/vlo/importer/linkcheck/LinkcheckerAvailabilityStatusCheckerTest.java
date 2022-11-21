/*
 * Copyright (C) 2022 CLARIN
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

import eu.clarin.linkchecker.persistence.model.Status;
import eu.clarin.linkchecker.persistence.model.Url;
import eu.clarin.linkchecker.persistence.repository.StatusRepository;
import eu.clarin.linkchecker.persistence.repository.UrlRepository;
import eu.clarin.linkchecker.persistence.service.StatusService;
import eu.clarin.linkchecker.persistence.utils.Category;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@SpringBootTest
public class LinkcheckerAvailabilityStatusCheckerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {"eu.clarin.linkchecker.persistence"})
    @EntityScan(basePackages = "eu.clarin.linkchecker.persistence.model")
    @EnableJpaRepositories(basePackages = "eu.clarin.linkchecker.persistence.repository")
    public static class Configuration {
        // Configuration class for this test
    }
    @Autowired
    private StatusService statusService;
    @Autowired
    private UrlRepository uRep;
    @Autowired
    private StatusRepository sRep;

    private LinkcheckerAvailabilityStatusChecker instance;

    @BeforeEach
    public void setUp() {
        instance = new LinkcheckerAvailabilityStatusChecker(statusService, null, null);
    }

    /**
     * Test of getLinkStatusForRefs method, of class
     * LinkcheckerAvailabilityStatusChecker.
     *
     * @throws java.lang.Exception
     */
    @Test
    @Transactional
    public void testGetLinkStatusForRefs() throws Exception {
        final String href1 = "http://www.clarin.eu";
        final String href2 = "http://www.wowasa.com/page2";

        {
            final Map<String, LinkStatus> result = instance.getLinkStatusForRefs(Stream.of(href1));
            assertNotNull(result);
            assertThat(result.entrySet(), hasSize(0));
        }
        Url url1 = uRep.save(new Url(href1, "www.clarin.eu", true));
        Url url2 = uRep.save(new Url(href2, "www.wowasa.com", true));

        sRep.save(new Status(url1, Category.Blocked_By_Robots_txt, "", LocalDateTime.now()));
        sRep.save(new Status(url2, Category.Broken, "", LocalDateTime.now()));
        {
            final Map<String, LinkStatus> result = instance.getLinkStatusForRefs(Stream.of(href1, href2));
            assertThat(result, allOf(aMapWithSize(2)));
            assertThat(result, hasEntry(equalTo(href1), hasProperty("url", equalTo(href1))));
            assertThat(result, hasEntry(equalTo(href2), hasProperty("url", equalTo(href2))));
        }
    }
}
