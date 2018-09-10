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
package eu.clarin.cmdi.vlo.importer;

/**
 * Stores metadata provided by the OAI-PMH harvester for every endpoint
 * @author Thomas Eckart
 */
public class EndpointDescription {

    private final String oaiEndpointUrl;
    private final String centreName;
    private final String nationalProject;

    public EndpointDescription(String oaiEndpointUrl, String centreName, String nationalProject) {
        this.oaiEndpointUrl = oaiEndpointUrl;
        this.centreName = centreName;
        this.nationalProject = nationalProject;
    }

    public String getOaiEndpointUrl() {
        return oaiEndpointUrl;
    }

    public String getCentreName() {
        return centreName;
    }

    public String getNationalProject() {
        return nationalProject;
    }

    @Override
    public String toString() {
        return "Endpoint \"" + oaiEndpointUrl + "\" of \"" + centreName + "\" (" + nationalProject + ")";
    }
    
}
