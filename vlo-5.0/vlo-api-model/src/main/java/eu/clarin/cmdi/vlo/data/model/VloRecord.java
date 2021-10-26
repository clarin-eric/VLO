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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.MultiField;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Document(indexName = "record", createIndex = true)
public class VloRecord {

    @Id
    @ToString.Include
    private String id;

    @Field(type = FieldType.Text)
    @ToString.Include
    private String dataRoot;

    @Field(type = FieldType.Text)
    @ToString.Include
    private String sourcePath;

    @Field(type = FieldType.Text)
    @ToString.Include
    private String selflink;

    @Field(type = FieldType.Text)
    private String profileId;

    private List<Resource> resources;

    //TODO: define a different structure for this that allows for disambiguating context
    //For instance two resource technical detail components with a @ref at component level and file size or access informationin a child element
    private Map<String, List<String>> pathValuesMap;

    private final Map<String, List<Object>> fields = Maps.newHashMap();

    public void removeField(String name) {
        fields.remove(name);
    }

    public boolean containsKey(String name) {
        return fields.containsKey(name);
    }

    public Collection<Object> getFieldValues(String name) {
        return fields.get(name);
    }

    public void addField(String name, Object value) {
        fields.computeIfAbsent(name, v -> Lists.newArrayList()).add(value);
    }

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
