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

import org.apache.wicket.model.IModel;
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
public class HandleLinkModelTest {

    private final Mockery context = new JUnit4Mockery();

    public HandleLinkModelTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetObjectWithUrl() {
        final IModel<String> inner = context.mock(IModel.class, "String");
        final PIDLinkModel instance = new PIDLinkModel(inner);

        {
            // model holds ordinary URL (no handle)
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("http://some/uri"));
                }
            });
            final String result = instance.getObject();
            // should return unchanged
            assertEquals("http://some/uri", result);
        }
        {
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("https://some/uri"));
                }
            });
            final String result = instance.getObject();
            // should return unchanged
            assertEquals("https://some/uri", result);
        }
    }

    @Test
    public void testGetObjectWithHandle() {
        final IModel<String> inner = context.mock(IModel.class, "String");
        final PIDLinkModel instance = new PIDLinkModel(inner);
        {
            // model holds a handle
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("hdl:1234/5678-90"));
                }
            });

            final String result = instance.getObject();
            // handle proxy should be prepended
            assertEquals("http://hdl.handle.net/1234/5678-90", result);
        }
        {
            // model holds a handle resolver URL
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("http://hdl.handle.net/1234/5678-90"));
                }
            });

            final String result = instance.getObject();
            // handle proxy should be prepended
            assertEquals("Expecting unchanged URL", "http://hdl.handle.net/1234/5678-90", result);
        }
        {
            // model holds a handle resolver URL
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("https://hdl.handle.net/1234/5678-90"));
                }
            });

            final String result = instance.getObject();
            // handle proxy should be prepended
            assertEquals("Expecting unchanged URL", "https://hdl.handle.net/1234/5678-90", result);
        }
    }

    @Test
    public void testGetObjectWithUrnNbn() {
        final IModel<String> inner = context.mock(IModel.class, "String");
        final PIDLinkModel instance = new PIDLinkModel(inner);
        {
            // model holds a handle
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("urn:nbn:de:kobv:b4-200905193201"));
                }
            });

            final String result = instance.getObject();
            // handle proxy should be prepended
            assertEquals("https://nbn-resolving.org/urn:nbn:de:kobv:b4-200905193201", result);
        }
        {
            // model holds a URN NBN link
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("http://urn.fi/urn:nbn:fi:lb-2017021504"));
                }
            });

            final String result = instance.getObject();
            // handle proxy should be prepended
            assertEquals("Expecting unchanged URL", "http://urn.fi/urn:nbn:fi:lb-2017021504", result);
        }

    }

    @Test
    public void testGetObjectWithDoi() {
        final IModel<String> inner = context.mock(IModel.class, "String");
        final PIDLinkModel instance = new PIDLinkModel(inner);
        {
            // model holds a doi
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("doi:123/456"));
                }
            });

            final String result = instance.getObject();
            // handle proxy should be prepended
            assertEquals("Expecting resolver URL", "https://doi.org/123/456", result);
        }
        {
            // model holds a doi
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("https://doi.org/123/456"));
                }
            });

            final String result = instance.getObject();
            // original value should be returned
            assertEquals("Expecting unchanged URL", "https://doi.org/123/456", result);
        }
        {
            // model holds a doi
            context.checking(new Expectations() {
                {
                    oneOf(inner).getObject();
                    will(returnValue("http://dx.doi.org/123/456"));
                }
            });

            final String result = instance.getObject();
            // original value should be returned
            assertEquals("Expecting unchanged URL", "http://dx.doi.org/123/456", result);
        }
    }

    @Test
    public void testGetObjectWithNull() {
        final IModel<String> inner = context.mock(IModel.class, "String");
        final PIDLinkModel instance = new PIDLinkModel(inner);
        // model holds a null reference
        context.checking(new Expectations() {
            {
                oneOf(inner).getObject();
                will(returnValue(null));
            }
        });

        final String result = instance.getObject();
        assertNull(result);
    }

    @Test
    public void testDetach() {
        final IModel<String> inner = context.mock(IModel.class, "String");
        final PIDLinkModel instance = new PIDLinkModel(inner);

        // detaching model should detach inner model
        context.checking(new Expectations() {
            {
                oneOf(inner).detach();
            }
        });
        instance.detach();
    }

}
