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

import eu.clarin.cmdi.vlo.pojo.ResourceType;
import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author twagoo
 */
public class ResourceTypeCountingServiceImpl implements ResourceTypeCountingService {

    @Override
    public Collection<ResourceTypeCount> countResourceTypes(Collection<String> resources) {
        //mock
        return Arrays.asList(new ResourceTypeCount(ResourceType.TEXT, 2),
                new ResourceTypeCount(ResourceType.VIDEO, 1),
                new ResourceTypeCount(ResourceType.OTHER, 1)
                );
    }
    
}
