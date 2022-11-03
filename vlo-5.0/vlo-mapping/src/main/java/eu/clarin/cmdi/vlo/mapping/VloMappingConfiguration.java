/*
 * Copyright (C) 2022 CLARIN
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
package eu.clarin.cmdi.vlo.mapping;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class VloMappingConfiguration {

    private String vocabularyRegistryUrl = "";
    private String profileSchemaUrl = "";
    private String mappingDefinitionUri = "";

    public void setVocabularyRegistryUrl(String vocabularyRegistryUrl) {
        this.vocabularyRegistryUrl = vocabularyRegistryUrl;
    }

    public String getVocabularyRegistryUrl() {
        return vocabularyRegistryUrl;
    }

    /**
     * Get the value of the ProfileSchemaUrl by profileId parameter<br>
     * <br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @return the value
     */
    public String getComponentRegistryProfileSchema(String id) {
        return profileSchemaUrl.replace("{PROFILE_ID}", id);
    }

    public void setProfileSchemaUrl(String profileSchemaUrl) {
        this.profileSchemaUrl = profileSchemaUrl;
    }

    public String getMappingDefinitionUri() {
        return mappingDefinitionUri;
    }

    public void setMappingDefinitionUri(String mappingDefinitionUri) {
        this.mappingDefinitionUri = mappingDefinitionUri;
    }

}
