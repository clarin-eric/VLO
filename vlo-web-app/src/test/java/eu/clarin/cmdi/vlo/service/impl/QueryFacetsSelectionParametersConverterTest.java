/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.service.solr.impl.QueryFacetsSelectionParametersConverter;
import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class QueryFacetsSelectionParametersConverterTest {

    private QueryFacetsSelectionParametersConverter instance;

    @Before
    public void setUp() {
        instance = new QueryFacetsSelectionParametersConverter();
    }

    /**
     * Test of fromParameters method, of class
     * QueryFacetsSelectionParametersConverter.
     */
    @Test
    public void testFromParameters() {
        final PageParameters params = new PageParameters();
        params.add("q", "query");
        params.add("q", "ignored"); // only one query param is selected
        params.add("fq", "facet1:valueA");
        params.add("fq", "facet1:valueB");
        params.add("fq", "facet2:valueC");
        params.add("fq", "illegal-no-colon"); //should get ignored
        params.add("fq", ""); // not a valid facet selection
        params.add("fq", "invalid"); // not a valid facet selection
        params.add("fqType", "facet1:or");
        params.add("fqType", "facet3:not_empty");
        params.add("fqType", "facet4:illegaltype"); //should get ignored
        params.add("fqType", "illegal-no-colon"); //should get ignored

        final QueryFacetsSelection result = instance.fromParameters(params);
        assertEquals("query", result.getQuery());
        assertEquals(3, result.getFacets().size());
        assertThat(result.getFacets(), hasItem("facet1"));
        assertThat(result.getFacets(), hasItem("facet2"));
        assertThat(result.getSelectionValues("facet1").getValues(), hasItem("valueA"));
        assertThat(result.getSelectionValues("facet1").getValues(), hasItem("valueB"));
        assertThat(result.getSelectionValues("facet2").getValues(), hasItem("valueC"));
        // OR explicitly set
        assertEquals(FacetSelectionType.OR, result.getSelectionValues("facet1").getSelectionType());
        // AND is default
        assertEquals(FacetSelectionType.AND, result.getSelectionValues("facet2").getSelectionType());
        // NOT_EMPTY explicitly set
        assertEquals(FacetSelectionType.NOT_EMPTY, result.getSelectionValues("facet3").getSelectionType());
    }

    /**
     * Test of fromParameters method, of class
     * QueryFacetsSelectionParametersConverter.
     */
    @Test
    public void testFromParametersSerializable() {
        final PageParameters params = new PageParameters();
        params.add("q", "query");
        params.add("q", "ignored"); // only one query param is selected
        params.add("fq", "facet1:valueA");
        params.add("fq", "facet1:valueB");
        params.add("fq", "facet2:valueC");
        params.add("fq", ""); // not a valid facet selection
        params.add("fq", "invalid"); // not a valid facet selection

        SerializationUtils.roundtrip(instance.fromParameters(params));
    }

    /**
     * Test of toParameters method, of class
     * QueryFacetsSelectionParametersConverter.
     */
    @Test
    public void testToParameters() {
        final String query = "query";
        final Map<String, FacetSelection> map = Maps.newHashMapWithExpectedSize(3);
        map.put("facet1", new FacetSelection(Arrays.asList("valueA", "valueB")));
        map.put("facet2", new FacetSelection(Collections.singleton("valueC")));

        QueryFacetsSelection selection = new QueryFacetsSelection(query, map);
        PageParameters result = instance.toParameters(selection);

        assertThat(result.get("q"), equalTo(StringValue.valueOf("query")));

        final List<StringValue> fq = result.getValues("fq");
        assertNotNull(fq);
        assertThat(fq, hasItem(StringValue.valueOf("facet1:valueA")));
        assertThat(fq, hasItem(StringValue.valueOf("facet1:valueB")));
        assertThat(fq, hasItem(StringValue.valueOf("facet2:valueC")));
    }

}
