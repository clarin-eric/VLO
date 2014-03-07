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

import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.pojo.ResourceType;
import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;

/**
 *
 * @author twagoo
 */
public class ResourceTypeCountingServiceImplTest {

    private final Mockery context = new JUnit4Mockery();

    /**
     * Test of countResourceTypes method, of class
     * ResourceTypeCountingServiceImpl.
     */
    @Test
    public void testCountResourceTypes() {
        System.out.println("countResourceTypes");
        Collection<String> resources = Arrays.asList(
                "video resource string1",
                "video resource string2",
                "audio resource string",
                "text resource string",
                "annotation resource string",
                "other resource string"
        //                "video/mpeg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myvideo",
        //                "audio/ogg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myaudio",
        //                "audio/ogg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myaudio"
        //                "audio/ogg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myaudio",
        //                "audio/ogg" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myaudio",
        //                "text/plain" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "mytext",
        //                "application/pdf" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "mypdf", // pdf = text
        //                "text/x-chat" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "mytext", // annotation
        //                "application/zip" + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR + "myzip"
        );
        final ResourceStringConverter converter = context.mock(ResourceStringConverter.class);
        context.checking(new Expectations() {
            {
                exactly(6).of(converter).getResourceInfo(with(any(String.class)));
                will(onConsecutiveCalls(
                        returnValue(new ResourceInfo("href1", "fileName1", "video/mpeg", ResourceType.VIDEO)),
                        returnValue(new ResourceInfo("href2", "fileName2", "video/mpeg", ResourceType.VIDEO)),
                        returnValue(new ResourceInfo("href3", "fileName3", "audio/ogg", ResourceType.AUDIO)),
                        returnValue(new ResourceInfo("href4", "fileName4", "audio/ogg", ResourceType.TEXT)),
                        returnValue(new ResourceInfo("href5", "fileName5", "audio/ogg", ResourceType.ANNOTATION)),
                        returnValue(new ResourceInfo("href6", "fileName6", "audio/ogg", ResourceType.OTHER))
                ));
            }
        });
        ResourceTypeCountingServiceImpl instance = new ResourceTypeCountingServiceImpl(converter);
        Collection<ResourceTypeCount> result = instance.countResourceTypes(resources);
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.VIDEO, 2)));
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.AUDIO, 1)));
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.TEXT, 1)));
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.ANNOTATION, 1)));
        assertThat(result, hasItem(new ResourceTypeCount(ResourceType.OTHER, 1)));
    }

}
