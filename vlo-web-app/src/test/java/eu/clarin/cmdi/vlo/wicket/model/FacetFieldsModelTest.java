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
package eu.clarin.cmdi.vlo.wicket.model;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import java.util.Arrays;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author twagoo
 */
public class FacetFieldsModelTest {

    private final Mockery context = new JUnit4Mockery();

    /**
     * Test of load method, of class FacetFieldsModel.
     */
    @Test
    public void testGetObject() {
        final FacetFieldsService service = context.mock(FacetFieldsService.class);
        final QueryFacetsSelection selection = new QueryFacetsSelection();
        final IModel<QueryFacetsSelection> selectionModel = new Model(selection);
        final List<String> facets = Arrays.asList("facet1", "facet2", "facet3", "facetX");
        final FacetFieldsModel instance = new FacetFieldsModel(service, facets, selectionModel, 20);

        context.checking(new Expectations() {
            {
                oneOf(service).getFacetFields(selection, facets, 20);
                will(returnValue(Arrays.asList(
                        new FacetField("facet1"),
                        new FacetField("facet2"),
                        new FacetField("facet3")
                )));
            }
        });

        final List<FacetField> result = instance.getObject();
        // included facets
        assertThat(result, hasFacetField("facet1")); // in selection
        assertThat(result, hasFacetField("facet2")); // in selection
        assertThat(result, hasFacetField("facet3")); // in selection
        // excluded facets
        assertThat(result, not(hasFacetField("facetX"))); // in selection but not in result

        // make another call, should not trigger a call to service because model is loadabledetachable
        final List<FacetField> result2 = instance.getObject();
        assertEquals(result, result2);
    }

    private static Matcher<Iterable<? super FacetField>> hasFacetField(String facetName) {
        return Matchers.<FacetField>hasItem(Matchers.<FacetField>hasProperty("name", equalTo(facetName)));
    }

}
