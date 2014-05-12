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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import org.apache.solr.common.SolrDocument;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class SolrDocumentModelTest {

    private final Mockery context = new JUnit4Mockery();

    private SolrDocumentService service;
    private SolrDocument expected;

    @Before
    public void setUp() {
        expected = new SolrDocument();
        expected.setField(FacetConstants.FIELD_ID, "id");
        service = context.mock(SolrDocumentService.class);
    }

    /**
     * Test of load method, of class SolrDocumentModel.
     */
    @Test
    public void testGetObject() {
        // construct with a document id (lazy loading)
        final SolrDocumentModel instance = new SolrDocumentModel("id") {

            @Override
            protected SolrDocumentService getDocumentService() {
                return service;
            }

        };

        context.checking(new Expectations() {
            {
                exactly(2).of(service).getDocument("id");
                will(returnValue(expected));
            }
        });

        // get object, should call service to load object
        assertEquals(expected, instance.getObject());
        // call once more, should not call service (already loaded)
        assertEquals(expected, instance.getObject());
        // detach, will invalidate loaded object
        instance.detach();
        // getting object will require a service call to reload
        assertEquals(expected, instance.getObject());
    }

    /**
     * Test of load method, of class SolrDocumentModel.
     */
    @Test
    public void testGetObjectInitialised() {
        // construct with an existing solr document
        final SolrDocumentModel instance = new SolrDocumentModel(expected) {

            @Override
            protected SolrDocumentService getDocumentService() {
                return service;
            }

        };

        context.checking(new Expectations() {
            {
                exactly(1).of(service).getDocument("id");
                will(returnValue(expected));
            }
        });

        // call once more, should not call service (already loaded) at construction
        assertEquals(expected, instance.getObject());
        // detach, will invalidate loaded object
        instance.detach();
        // getting object will require a service call to reload
        assertEquals(expected, instance.getObject());
    }

}
