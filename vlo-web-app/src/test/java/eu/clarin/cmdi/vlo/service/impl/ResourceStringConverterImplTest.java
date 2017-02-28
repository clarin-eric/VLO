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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.pojo.ResourceType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class ResourceStringConverterImplTest {

    private ResourceStringConverterImpl instance;

    @Before
    public void setUp() {
        instance = new ResourceStringConverterImpl();
    }

    /**
     * Test of getResourceInfo method, of class ResourceStringConverterImpl.
     */
    @Test
    public void testGetResourceInfoVideo() {
        String resourceString = "video/mpeg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "href"; //invalid URI
        ResourceInfo result = instance.getResourceInfo(resourceString);
        assertEquals("href", result.getHref());
        assertEquals("video/mpeg", result.getMimeType());
        assertEquals(ResourceType.VIDEO, result.getResourceType());
    }

    /**
     * Test of getResourceInfo method, of class ResourceStringConverterImpl.
     */
    @Test
    public void testGetResourceInfoVideoInvalid() {
        String resourceString = "video/mpeg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "video href"; //invalid URI
        ResourceInfo result = instance.getResourceInfo(resourceString);
        assertEquals("video href", result.getHref());
    }

    /**
     * Test of getResourceInfo method, of class ResourceStringConverterImpl.
     */
    @Test
    public void testGetResourceInfoAudio() {
        String resourceString = "audio/ogg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "href";
        ResourceInfo result = instance.getResourceInfo(resourceString);
        assertEquals("href", result.getHref());
        assertEquals("audio/ogg", result.getMimeType());
        assertEquals(ResourceType.AUDIO, result.getResourceType());
    }

    /**
     * Test of getResourceInfo method, of class ResourceStringConverterImpl.
     */
    @Test
    public void testGetResourceInfoText() {
        String resourceString = "text/plain" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "href";
        ResourceInfo result = instance.getResourceInfo(resourceString);
        assertEquals("href", result.getHref());
        assertEquals("text/plain", result.getMimeType());
        assertEquals(ResourceType.TEXT, result.getResourceType());
    }

    /**
     * Test of getResourceInfo method, of class ResourceStringConverterImpl.
     */
    @Test
    public void testGetResourceInfoAnnotation() {
        String resourceString = "text/x-chat" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "href";
        ResourceInfo result = instance.getResourceInfo(resourceString);
        assertEquals("href", result.getHref());
        assertEquals("text/x-chat", result.getMimeType());
        assertEquals(ResourceType.ANNOTATION, result.getResourceType());
    }

    /**
     * Test of getResourceInfo method, of class ResourceStringConverterImpl.
     */
    @Test
    public void testGetResourceInfoArchive() {
        String resourceString = "application/zip" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "href";
        ResourceInfo result = instance.getResourceInfo(resourceString);
        assertEquals("href", result.getHref());
        assertEquals("application/zip", result.getMimeType());
        assertEquals(ResourceType.ARCHIVE, result.getResourceType());
    }

    /**
     * Test of getResourceInfo method, of class ResourceStringConverterImpl.
     */
    @Test
    public void testGetResourceInfoOther() {
        String resourceString = "application/octet-stream" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "href";
        ResourceInfo result = instance.getResourceInfo(resourceString);
        assertEquals("href", result.getHref());
        assertEquals("application/octet-stream", result.getMimeType());
        assertEquals(ResourceType.OTHER, result.getResourceType());
    }
    
    @Test 
    public void testGetResourceInfoFilename() {
        assertFileNameResult("file.txt", "http://myserver.com/file.txt");
        assertFileNameResult("file.txt", "file:/somehwere/on/my/fs/this/file.txt");
        assertFileNameResult("http://myserver.com", "http://myserver.com");
        assertFileNameResult("hdl:12345", "hdl:12345");
    }
    
    private void assertFileNameResult(String expected, String href){
        String resourceString = "application/test" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + href;
        ResourceInfo result = instance.getResourceInfo(resourceString);
        assertEquals(expected, result.getFileName());
    }

}
