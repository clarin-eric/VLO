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
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class SearchContextModelTest {

    private final Mockery context = new JUnit4Mockery();

    private SearchContextModel instance;
    private QueryFacetsSelection selection;
    private final static long index = 123;
    private final static long resultCount = 400;

    @Before
    public void setUp() {
        selection = new QueryFacetsSelection();
        instance = new SearchContextModel(index, resultCount, Model.of(selection));
    }

    /**
     * Test of getSelectionModel method, of class SearchContextModel.
     */
    @Test
    public void testGetSelectionModel() {
        final IModel<QueryFacetsSelection> result = instance.getSelectionModel();
        assertNotNull(result);
        assertEquals(selection, result.getObject());
    }

    /**
     * Test of getSelection method, of class SearchContextModel.
     */
    @Test
    public void testGetSelection() {
        assertEquals(selection, instance.getSelection());
    }

    /**
     * Test of getResultCount method, of class SearchContextModel.
     */
    @Test
    public void testGetResultCount() {
        assertEquals(resultCount, instance.getResultCount());
    }

    /**
     * Test of getIndex method, of class SearchContextModel.
     */
    @Test
    public void testGetIndex() {
        assertEquals(index, instance.getIndex());
    }

    /**
     * Test of getObject method, of class SearchContextModel.
     */
    @Test
    public void testGetObject() {
        SearchContext result = instance.getObject();
        assertEquals(index, result.getIndex());
        assertEquals(resultCount, result.getResultCount());
        assertEquals(selection, result.getSelection());
    }

    /**
     * Test of detach method, of class SearchContextModel.
     */
    @Test
    public void testDetach() {
        final IModel selectionModel = context.mock(IModel.class);
        instance = new SearchContextModel(index, resultCount, selectionModel);
        context.checking(new Expectations() {
            {
                oneOf(selectionModel).detach();
            }
        });
        instance.detach();
    }

    /**
     * Test of next method, of class SearchContextModel.
     */
    @Test
    public void testNext() {
        final SearchContextModel result = SearchContextModel.next(instance);
        assertEquals(index + 1, result.getIndex());
        assertEquals(resultCount, result.getResultCount());
        assertEquals(selection, result.getSelection());
    }

    /**
     * Test of next method, of class SearchContextModel.
     */
    @Test
    public void testLastNext() {
        final SearchContextModel lastRecordContext = new SearchContextModel(9, 10, Model.of(selection));
        assertNull(SearchContextModel.next(lastRecordContext));
    }

    /**
     * Test of previous method, of class SearchContextModel.
     */
    @Test
    public void testPrevious() {
        final SearchContextModel result = SearchContextModel.previous(instance);
        assertEquals(index - 1, result.getIndex());
        assertEquals(resultCount, result.getResultCount());
        assertEquals(selection, result.getSelection());
    }

    /**
     * Test of previous method, of class SearchContextModel.
     */
    @Test
    public void testFirstPrevious() {
        final SearchContextModel lastRecordContext = new SearchContextModel(0, 10, Model.of(selection));
        assertNull(SearchContextModel.previous(lastRecordContext));
    }

}
