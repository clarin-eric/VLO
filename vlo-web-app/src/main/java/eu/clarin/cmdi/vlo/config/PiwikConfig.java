/*
 * Copyright (C) 2016 CLARIN
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
 * Configuration for communication with the Piwik instance
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 * @see https://piwik.org
 * @see https://stats.clarin.eu
 */
public class PiwikConfig {

    @Value("${eu.clarin.cmdi.vlo.piwik.enableTracker:false}")
    private boolean enabled;
    
    @Value("${eu.clarin.cmdi.vlo.piwik.siteId:3}")
    private String piwikSiteId;

    @Value("${eu.clarin.cmdi.vlo.piwik.host:https://stats.clarin.eu/}")
    private String piwikHost;

    @Value("${eu.clarin.cmdi.vlo.piwik.domains:*.vlo.clarin.eu}")
    private String domains;

    public boolean isEnabled() {
        return enabled;
    }

    public String getSiteId() {
        return piwikSiteId;
    }

    public String getPiwikHost() {
        return piwikHost;
    }

    public String getDomains() {
        return domains;
    }

}
