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
package eu.clarin.cmdi.vlo.pojo;

import java.io.Serializable;

/**
 *
 * @author twagoo
 */
public class ResourceInfo implements Serializable {

    private final String href;
    private final String fileName;
    private final String mimeType;
    private final ResourceType resourceType;

    public ResourceInfo(String href, String fileName, String mimeType, ResourceType resourceType) {
        this.href = href;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.resourceType = resourceType;
    }

    public String getHref() {
        return href;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

}
