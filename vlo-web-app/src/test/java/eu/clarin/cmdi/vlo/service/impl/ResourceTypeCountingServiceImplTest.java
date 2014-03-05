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
import eu.clarin.cmdi.vlo.pojo.ResourceType;
import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author twagoo
 */
public class ResourceTypeCountingServiceImplTest {

    /**
     * Test of countResourceTypes method, of class
     * ResourceTypeCountingServiceImpl.
     */
    @Test
    public void testCountResourceTypes() {
        System.out.println("countResourceTypes");
        Collection<String> resources = Arrays.asList(
                "video/mpeg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myvideo",
                "video/mpeg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myvideo",
                "audio/ogg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myaudio",
                "audio/ogg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myaudio",
                "audio/ogg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myaudio",
                "audio/ogg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myaudio",
                "text/plain" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "mytext",
                "application/pdf" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "mypdf", // pdf = text
                "text/x-chat" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "mytext", // annotation
                "application/zip" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myzip"
        );
        ResourceTypeCountingServiceImpl instance = new ResourceTypeCountingServiceImpl();
        Collection<ResourceTypeCount> result = instance.countResourceTypes(resources);
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.VIDEO, 2)));
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.AUDIO, 4)));
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.TEXT, 2)));
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.ANNOTATION, 1)));
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.OTHER, 1))); // the zip
    }

}
