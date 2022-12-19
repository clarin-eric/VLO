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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import lombok.extern.slf4j.Slf4j;
import eu.clarin.cmdi.vlo.mapping.VloMappingTestConfiguration;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.Context;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class ProfileContextMapFactoryImplTest {

    final VloMappingConfiguration mappingConfig = new VloMappingTestConfiguration();

    final VTDProfileParser parser = new TestResourceVTDProfileParser(mappingConfig);

    /**
     * Test of readProfile method, of class ProfileReaderImpl.
     */
    @Test
    public void testReadProfile1() throws Exception {

        final ProfileContextMapFactory instance = new ProfileContextMapFactoryImpl(mappingConfig, parser);

        final String profileId = "clarin.eu:cr1:p_1345561703673";
        final Map<String, Context> xpathConceptPathMap = instance.createProfileContextMap(profileId);

        assertNotNull(xpathConceptPathMap);
        assertThat(xpathConceptPathMap, hasKey("/cmd:CMD/cmd:Components/cmdp:ArthurianFiction/text()"));
        assertThat("Context's own concept", xpathConceptPathMap, hasEntry(
                //key
                equalTo("/cmd:CMD/cmd:Components/cmdp:ArthurianFiction/cmdp:manuscript/cmdp:id/text()"),
                //value
                hasProperty("conceptPath", hasItem("http://hdl.handle.net/11459/CCR_C-3894_4d08cc31-25fe-af0c-add4-ca7bdc12f5f7"))
        ));
        assertThat("Context's ancestor concept", xpathConceptPathMap, hasEntry(
                //key
                equalTo("/cmd:CMD/cmd:Components/cmdp:ArthurianFiction/cmdp:manuscript/cmdp:id/text()"),
                //value
                hasProperty("conceptPath", hasItem("http://hdl.handle.net/11459/CCR_C-4347_728552dd-4c23-0a69-390c-10214c05983f"))
        ));
    }

    @Test
    public void testReadProfile2() throws Exception {

        final ProfileContextMapFactory instance = new ProfileContextMapFactoryImpl(mappingConfig, parser);

        final String profileId = "clarin.eu:cr1:p_1288172614026";
        final Map<String, Context> map = instance.createProfileContextMap(profileId);

        assertNotNull(map);
        assertThat(map, hasKey("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:date/text()"));
        assertThat(map, hasKey("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:date/@dcterms-type"));
        assertThat("Attribute concept path (without attribute concept)", map, hasEntry(
                //key
                equalTo("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:date/@dcterms-type"),
                //value
                hasProperty("conceptPath",
                        contains(
                                "",
                                "http://purl.org/dc/terms/date",
                                ""))
        ));
        assertThat("Attribute concept path (with attribute concept)", map, hasEntry(
                //key
                equalTo("/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:language/@olac-language"),
                //value
                hasProperty("conceptPath",
                        contains(
                                "http://hdl.handle.net/11459/CCR_C-2482_08eded24-4086-7e3f-88e5-e0807fb01e17",
                                "http://purl.org/dc/terms/language",
                                ""))
        ));

    }

}
