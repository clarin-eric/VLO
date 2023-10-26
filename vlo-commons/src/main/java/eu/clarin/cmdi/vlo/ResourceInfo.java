/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.vlo;

import com.google.common.base.Strings;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ResourceInfo {

    private final static Logger logger = LoggerFactory.getLogger(ResourceInfo.class);

    private final String url;
    private final String type;
    private final Integer status;
    private final Long lastChecked;

    @JsonbCreator
    public ResourceInfo(@JsonbProperty("url") String url, @JsonbProperty("type") String type, @JsonbProperty("status") Integer status, @JsonbProperty("lastChecked") Long lastChecked) {
        this.url = url;
        this.type = type;
        this.status = status;
        this.lastChecked = lastChecked;
    }

    /**
     * Get the value of status
     *
     * @return the value of status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Get the value of type
     *
     * @return the value of type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the URL
     *
     * @return the value of URL
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @return timestamp of last availability check
     */
    public Long getLastChecked() {
        return lastChecked;
    }

    @Override
    public String toString() {
        return "ResourceInfo{" + "url=" + url + ", type=" + type + ", status=" + status + ", lastChecked=" + lastChecked + '}';
    }

    public String toJson(Jsonb jsonb) {
        try {
            return jsonb.toJson(this);
        } catch (JsonbException ex) {
            logger.error("Error while writing ResourceInfo object to JSON: {}", this, ex);
            return null;
        }
    }

    public static ResourceInfo fromJson(Jsonb jsonb, String json) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }

        try {
            return jsonb.fromJson(json, ResourceInfo.class);
        } catch (JsonbException ex) {
            logger.error("Error while reading ResourceInfo object from JSON: {}", json, ex);
            return null;
        }
    }

}
