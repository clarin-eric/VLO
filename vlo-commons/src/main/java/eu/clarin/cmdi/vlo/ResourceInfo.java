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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.io.IOException;
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
    private final String status;
    private final Long lastChecked;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ResourceInfo(@JsonProperty("url") String url, @JsonProperty("type") String type, @JsonProperty("status") String status, @JsonProperty("lastChecked") Long lastChecked) {
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
    public String getStatus() {
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

    public String toJson(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (IOException ex) {
            logger.error("Error while writing ResourceInfo object to JSON: {}", this, ex);
            return null;
        }
    }

    public static ResourceInfo fromJson(ObjectMapper objectMapper, String json) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }

        try {
            return objectMapper.readValue(json, ResourceInfo.class);
        } catch (IOException ex) {
            logger.error("Error while reading ResourceInfo object from JSON: {}", json, ex);
            return null;
        }
    }

}
