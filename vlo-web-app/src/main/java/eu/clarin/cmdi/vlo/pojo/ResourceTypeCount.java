/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.pojo;

import java.io.Serializable;

/**
 * Representation of the number of resources of a certain type
 *
 * @author twagoo
 */
public class ResourceTypeCount implements Serializable {

    /**
     * Enumeration for singular/plural distinction. {@link #toString() }
     * returns "singular" or "plural" respectively to make the resource bundle
     * files look clean (no screaming)
     */
    public enum Number {

        SINGULAR("singular"),
        PLURAL("plural");

        private final String numberString;

        Number(String numberString) {
            this.numberString = numberString;
        }

        @Override
        public String toString() {
            return numberString;
        }

    }

    private final ResourceType resourceType;
    private final Integer count;

    public ResourceTypeCount(ResourceType resourceType, Integer count) {
        this.resourceType = resourceType;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    /**
     *
     * @return {@link Number#SINGULAR } or {@link Number#PLURAL } depending on
     * value of {@link #getCount() }
     */
    public Number getNumber() {
        return getCount() == 1 ? Number.SINGULAR : Number.PLURAL;
    }

    @Override
    public String toString() {
        return String.format("%d %s", getCount(), getResourceType());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.resourceType != null ? this.resourceType.hashCode() : 0);
        hash = 89 * hash + (this.count != null ? this.count.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceTypeCount other = (ResourceTypeCount) obj;
        if (this.resourceType != other.resourceType) {
            return false;
        }
        if (this.count != other.count && (this.count == null || !this.count.equals(other.count))) {
            return false;
        }
        return true;
    }

}
