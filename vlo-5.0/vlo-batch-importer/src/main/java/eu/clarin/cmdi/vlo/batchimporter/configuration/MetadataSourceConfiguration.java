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
package eu.clarin.cmdi.vlo.batchimporter.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for metadata input (the data roots)
 *
 * Example:
 * <pre>
    vlo:
      importer:
        metadata-source:
          roots: 
            - name: 'First root'
              path: /my/data/roots/first
            - name: 'Smaller test set'
              path: /my/data/roots/second
 * </pre>
 * 
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ConfigurationProperties(prefix = "vlo.importer.metadata-source")
public class MetadataSourceConfiguration {

    private List<DataRootConfiguration> roots;

    public List<DataRootConfiguration> getRoots() {
        return roots;
    }

    public void setRoots(List<DataRootConfiguration> roots) {
        this.roots = roots;
    }

    public static class DataRootConfiguration {

        private String name;
        private String path;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

}
