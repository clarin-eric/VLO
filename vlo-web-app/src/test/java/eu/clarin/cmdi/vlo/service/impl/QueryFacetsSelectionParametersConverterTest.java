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

import com.google.common.collect.HashMultimap;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.List;
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
        params.add("fq", ""); // not a valid facet selection
        params.add("fq", "invalid"); // not a valid facet selection

        final QueryFacetsSelection result = instance.fromParameters(params);
        assertEquals("query", result.getQuery());
        assertEquals(2, result.getFacets().size());
        assertThat(result.getFacets(), hasItem("facet1"));
        assertThat(result.getFacets(), hasItem("facet2"));
        assertThat(result.getSelectionValues("facet1"), hasItem("valueA"));
        assertThat(result.getSelectionValues("facet1"), hasItem("valueB"));
        assertThat(result.getSelectionValues("facet2"), hasItem("valueC"));
    }

    /**
     * Test of toParameters method, of class
     * QueryFacetsSelectionParametersConverter.
     */
    @Test
    public void testToParameters() {
        final String query = "query";
        final HashMultimap<String, String> map = HashMultimap.<String, String>create();
        map.put("facet1", "valueA");
        map.put("facet1", "valueB");
        map.put("facet2", "valueC");

        QueryFacetsSelection selection = new QueryFacetsSelection(query, map.asMap());
        PageParameters result = instance.toParameters(selection);

        assertThat(result.get("q"), equalTo(StringValue.valueOf("query")));

        final List<StringValue> fq = result.getValues("fq");
        assertEquals(3, fq.size());
        assertThat(fq, hasItem(StringValue.valueOf("facet1:valueA")));
        assertThat(fq, hasItem(StringValue.valueOf("facet1:valueB")));
        assertThat(fq, hasItem(StringValue.valueOf("facet2:valueC")));
    }

}
