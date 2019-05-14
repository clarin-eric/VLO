/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.vlo.service.centreregistry;

import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class EndpointProvider implements Serializable {
    
    private String centreName;
    private String centreWebsiteUrl;
    private Integer centreKey;
    private Collection<String> endpointUrls;

    public Integer getCentreKey() {
        return centreKey;
    }

    public EndpointProvider setKey(Integer key) {
        this.centreKey = key;
        return this;
    }

    public String getCentreName() {
        return centreName;
    }

    public EndpointProvider setName(String name) {
        this.centreName = name;
        return this;
    }

    public String getCentreWebsiteUrl() {
        return centreWebsiteUrl;
    }

    public EndpointProvider setWebsiteUrl(String websiteUrl) {
        this.centreWebsiteUrl = websiteUrl;
        return this;
    }

    public Collection<String> getEndpointUrls() {
        return endpointUrls;
    }

    public EndpointProvider setEndpointUrl(Collection<String> endpointUrl) {
        this.endpointUrls = endpointUrl;
        return this;
    }
    
}
