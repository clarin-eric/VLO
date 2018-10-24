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

import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public interface CMDIData<T> extends DocFieldContainer {

    /**
     * Sets a field in the doc to a certain value. Well, at least calls another
     * (private) method that actually does this.
     *
     * @param valueSet
     * @param caseInsensitive
     */
    void addDocField(ValueSet valueSet, boolean caseInsensitive);

    void addDocField(String fieldName, Object value, boolean caseInsensitive);

    void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive);

    /**
     * Add a meta data resource to the list of resources of that type.
     *
     * Whenever the type is not one of a type supported by the CMDI
     * specification, a warning is logged.
     *
     * @param resource meta data resource
     * @param type type of the resource
     * @param mimeType mime type associated with the resource
     */
    void addResource(String resource, String type, String mimeType);

    List<Resource> getDataResources();

    Collection<Object> getDocField(String name);

    T getDocument();

    String getId();

    /**
     * Return the list of landing page resources.
     *
     * @return list of landing page resources
     */
    List<Resource> getLandingPageResources();

    List<Resource> getMetadataResources();

    /**
     * Return the list of search page resources.
     *
     * @return the list
     */
    List<Resource> getSearchPageResources();

    /**
     * Returns list of all search interfaces (preferably CQL interfaces)
     *
     * @return list of search interface resources
     */
    List<Resource> getSearchResources();

    /**
     * Checks if any resources (metadata, landingpage, etc.) are available
     *
     * @return Returns true if at least one resource is available
     */
    boolean hasResources();

    void replaceDocField(ValueSet valueSet, boolean caseInsensitive);

    void replaceDocField(String name, Object value, boolean caseInsensitive);
    
    void removeField(String name);
    
    boolean hasField(String name);
    
    Collection<Object> getFieldValues(String name);

    void setId(String id);
    
}
