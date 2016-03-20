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

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class VloReport {

    private int recordCount;
    private List<CollectionCount> collections;

    /**
     * Get the value of recordCount
     *
     * @return the value of recordCount
     */
    public int getRecordCount() {
        return recordCount;
    }

    /**
     * Set the value of recordCount
     *
     * @param recordCount new value of recordCount
     */
    public void setRecordCount(int recordCount) {
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

}
