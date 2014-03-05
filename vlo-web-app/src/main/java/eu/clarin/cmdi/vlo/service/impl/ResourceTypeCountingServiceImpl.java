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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.ResourceType;
import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Counts resource types in a resource string collection
 *
 * @author twagoo
 */
public class ResourceTypeCountingServiceImpl implements ResourceTypeCountingService {

    private final static String SPLIT_PATTERN = Pattern.quote(FacetConstants.FIELD_RESOURCE_SPLIT_CHAR);

    @Override
    public Collection<ResourceTypeCount> countResourceTypes(Collection<String> resources) {
        if (resources == null || resources.isEmpty()) {
            return Collections.emptySet();
        }

        final Multiset<ResourceType> countBag = HashMultiset.<ResourceType>create(ResourceType.values().length);

        // loop over resources and count types
        for (String resourceString : resources) {
            // split resource string to find mime type
            final String[] tokens = resourceString.split(SPLIT_PATTERN, 2);
            final String mimeType = tokens[0];
            // normalise
            final String normalizeMimeType = CommonUtils.normalizeMimeType(mimeType);
            // map to ResourceType and add to bag (TODO: normalize to ResourceType directly?)
            if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_ANNOTATION)) {
                countBag.add(ResourceType.ANNOTATION);
            } else if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_AUDIO)) {
                countBag.add(ResourceType.AUDIO);
            } else if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_IMAGE)) {
                countBag.add(ResourceType.IMAGE);
            } else if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_TEXT)) {
                countBag.add(ResourceType.TEXT);
            } else if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_VIDEO)) {
                countBag.add(ResourceType.VIDEO);
            }
        }

        // count items in bag for each resource type
        final Collection<ResourceTypeCount> counts = new ArrayList<ResourceTypeCount>(countBag.elementSet().size());
        for (ResourceType type : ResourceType.values()) {
            final int count = countBag.count(type);
            // don't add zero counts
            if (count > 0) {
                counts.add(new ResourceTypeCount(type, count));
            }
        }
        return counts;
    }

}
