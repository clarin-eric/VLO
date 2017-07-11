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
package clarin.cmdi.vlo.statistics.model;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
@XmlRootElement
public class VloReport {

    private long recordCount;
    private List<CollectionCount> collections;
    private List<Facet> facets;

    /**
     * Get the value of recordCount
     *
     * @return the value of recordCount
     */
    public long getRecordCount() {
        return recordCount;
    }

    /**
     * Set the value of recordCount
     *
     * @param recordCount new value of recordCount
     */
    public void setRecordCount(long recordCount) {
        this.recordCount = recordCount;
    }

    /**
     * Get the value of collections
     *
     * @return the value of collections
     */
    public List<CollectionCount> getCollections() {
        return collections;
    }

    /**
     * Set the value of collections
     *
     * @param collections new value of collections
     */
    public void setCollections(List<CollectionCount> collections) {
        this.collections = collections;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public static class CollectionCount {

        private String collection;

        private long count;

        /**
         * Get the value of count
         *
         * @return the value of count
         */
        public long getCount() {
            return count;
        }

        /**
         * Set the value of count
         *
         * @param count new value of count
         */
        public void setCount(long count) {
            this.count = count;
        }

        /**
         * Get the value of collection
         *
         * @return the value of collection
         */
        public String getCollection() {
            return collection;
        }

        /**
         * Set the value of collection
         *
         * @param collection new value of collection
         */
        public void setCollection(String collection) {
            this.collection = collection;
        }

    }

    public static class Facet {

        private String name;
        private long valueCount;

        /**
         * Get the value of valueCount
         *
         * @return the value of valueCount
         */
        public long getValueCount() {
            return valueCount;
        }

        /**
         * Set the value of valueCount
         *
         * @param valueCount new value of valueCount
         */
        public void setValueCount(long valueCount) {
            this.valueCount = valueCount;
        }

        /**
         * Get the value of name
         *
         * @return the value of name
         */
        public String getName() {
            return name;
        }

        /**
         * Set the value of name
         *
         * @param name new value of name
         */
        public void setName(String name) {
            this.name = name;
        }

    }

}
