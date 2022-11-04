/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.data.model;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MappingInput {

    private String id;
    private String sourcePath;
    private String selflink;
    private String dataRoot;
    private String profileId;
    private List<Resource> resources;

    //TODO: define a different structure for this that allows for disambiguating context
    //For instance two resource technical detail components with a @ref at component level and file size or access informationin a child element
    private Map<String, List<String>> pathValuesMap;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @ToString(onlyExplicitlyIncluded = true)
    public static class Resource {

        private String id;
        
        @ToString.Include
        private String ref;

        private String type; //TODO: enum?

        private String mediaType;
    }

}