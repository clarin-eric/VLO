/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.config;

import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RatingConfig {

    @Value("${eu.clarin.cmdi.vlo.rating.enabled:false}")
    private boolean enabled;

    @Value("${eu.clarin.cmdi.vlo.rating.couchdb.url:}")
    private String couchDbBaseUrl;

    @Value("${eu.clarin.cmdi.vlo.rating.couchdb.user:}")
    private String couchDbUser;

    @Value("${eu.clarin.cmdi.vlo.rating.couchdb.password:}")
    private String couchDbPassword;

    @Value("${eu.clarin.cmdi.vlo.rating.service:vlo}")
    private String serviceName;

    public boolean isEnabled() {
        return enabled;
    }

    public String getCouchDbBaseUrl() {
        return couchDbBaseUrl;
    }

    public String getCouchDbUser() {
        return couchDbUser;
    }

    public String getCouchDbPassword() {
        return couchDbPassword;
    }

    public String getServiceName() {
        return serviceName;
    }

}
