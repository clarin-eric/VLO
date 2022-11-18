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

import eu.clarin.linkchecker.persistence.service.StatusService;
import java.util.Map;
import java.util.stream.Stream;
import javax.sql.DataSource;
import static org.junit.Assert.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@SpringBootTest
public class LinkcheckerAvailabilityStatusCheckerTest {

    @Autowired
    private StatusService statusService;

    @Autowired
    private DataSource dataSource;
//    
//    @Autowired
//    private DataSourceInitializer dataSourceInitializer;

    private LinkcheckerAvailabilityStatusChecker instance;

    @BeforeEach
    public void setUp() {
        instance = new LinkcheckerAvailabilityStatusChecker(statusService);
    }

    /**
     * Test of getLinkStatusForRefs method, of class
     * LinkcheckerAvailabilityStatusChecker.
     */
    @Test
    public void testGetLinkStatusForRefs() throws Exception {
        Stream<String> hrefs = Stream.of("http://www.clarin.eu");
        Map<String, LinkStatus> result = instance.getLinkStatusForRefs(hrefs);
        assertNotNull(result);
    }

}
