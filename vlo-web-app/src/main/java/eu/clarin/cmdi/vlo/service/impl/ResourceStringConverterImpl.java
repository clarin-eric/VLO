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

import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.pojo.ResourceType;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author twagoo
 */
public class ResourceStringConverterImpl implements ResourceStringConverter {

    private final static String SPLIT_PATTERN = Pattern.quote(FacetConstants.FIELD_RESOURCE_SPLIT_CHAR);

    @Override
    public ResourceInfo getResourceInfo(String resourceString) {
        // split resource string to find href and mime type
        final String[] tokens = resourceString.split(SPLIT_PATTERN, 2);
        final String mimeType = tokens[0];
        final String href = tokens[1];

        final String fileName = getFileName(href);
        // determine resource type based on mime type
        final ResourceType resourceType = determineResourceType(mimeType);
        return new ResourceInfo(href, fileName, mimeType, resourceType);
    }

    private String getFileName(final String href) {
        try {
            //analyse URI
            final URI uri = new URI(href);
            final String scheme = uri.getScheme();
            final String path = uri.getPath();
            // in case of path information or handle, return original href
            if (path == null || path.isEmpty() || (scheme != null && scheme.equals("hdl"))) {
                return href;
            } else {
                return FilenameUtils.getName(path);
            }
        } catch (URISyntaxException ex) {
            return href;
        }
    }

    private ResourceType determineResourceType(final String mimeType) {
        final String normalizeMimeType = CommonUtils.normalizeMimeType(mimeType);
        // map to ResourceType and add to bag
        if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_ANNOTATION)) {
            return ResourceType.ANNOTATION;
        } else if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_AUDIO)) {
            return ResourceType.AUDIO;
        } else if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_IMAGE)) {
            return ResourceType.IMAGE;
        } else if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_TEXT)) {
            return ResourceType.TEXT;
        } else if (normalizeMimeType.equals(FacetConstants.RESOURCE_TYPE_VIDEO)) {
            return ResourceType.VIDEO;
        } else {
            return ResourceType.OTHER;
        }
    }

}
