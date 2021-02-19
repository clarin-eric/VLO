/*
 * Copyright (C) 2015 CLARIN
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
package eu.clarin.cmdi.vlo.service.impl;

import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.vlo.service.FacetConfigurationService;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class FacetConfigurationServiceImpl implements FacetConfigurationService {

    private final static Logger logger = LoggerFactory.getLogger(FacetConfigurationServiceImpl.class);

    private final Map<String, Facet> facets = new CopyOnWriteHashMap<>();
    private final FacetsConfiguration facetsConfiguration;

    public FacetConfigurationServiceImpl(FacetsConfiguration facetsConfiguration) {
        this.facetsConfiguration = facetsConfiguration;
    }

    @PostConstruct
    protected void init() {
        for (Facet facet : facetsConfiguration.getFacet()) {
            if (facet.getDescription() != null) {
                logger.debug("Found facet configuration '{}'", facet.getName());
                facets.put(facet.getName(), facet);
            }
        }
    }

    @Override
    public String getDescription(String facetName) {
        return Optional.ofNullable(facets.get(facetName))
                .map(Facet::getDescription)
                .orElse(null);
    }

    /**
     *
     * @return all facet fields, including collection facet (arbitrary order
     * unspecified)
     * @see #getFacetFieldNames()
     * @see #getCollectionFacet()
     */
    @Override
    public List<String> getFacetsInSearch() {
        //TODO
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        final ArrayList<String> allFacets = new ArrayList<String>(getFacetFieldNames());
//        final String collection = getCollectionFacet();
//        if (collection != null) {
//            allFacets.add(collection);
//        }
//        return allFacets;
    }

    @Override
    public Collection<String> getIgnoredFieldNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<String> getTechnicalFieldNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<String> getSearchResultFieldNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getFacetFieldNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<String> getPrimaryFacetFieldNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

    

//    /**
//     * Get the value of the FacetFields parameter<br>
//     * <br>
//     *
//     * For a description of the parameter, refer to the general VLO documentation.
//     *
//     * @return List of field-keys
//     */
//    public List<String> getFacetFieldKeys() {
//        return this.facetField;
//    }
//
//    /**
//     * @return List of resolved field-names
//     */
//    public List<String> getFacetFieldNames() {
//        return this.facetField.stream().map(key -> this.fields.get(key)).collect(Collectors.toList());
//    }
//
//    /**
//     * @return Set of field-keys
//     */
//    public Set<String> getPrimaryFacetFieldKeys() {
//        return this.primaryFacetField;
//    }
//
//    /**
//     * @return Set of resolved field-names
//     */
//    public Set<String> getPrimaryFacetFieldNames() {
//        return this.primaryFacetField.stream().map(key -> this.fields.get(key)).collect(Collectors.toSet());
//    }
//
//    public void setPrimaryFacetFields(Set<String> primaryFacetField) {
//        this.primaryFacetField = primaryFacetField;
//    }
//
//    /**
//     *
//     * @return all facet fields, including collection facet (arbitrary order
//     *         unspecified)
//     * @see #getFacetFieldNames()
//     * @see #getCollectionFacet()
//     */
//    public List<String> getFacetsInSearch() {
//        final ArrayList<String> allFacets = new ArrayList<String>(getFacetFieldNames());
//        final String collection = getCollectionFacet();
//        if (collection != null) {
//            allFacets.add(collection);
//        }
//        return allFacets;
//    }
//
//    /**
//     * Set the value of the FacetFields parameter<br>
//     * <br>
//     *
//     * For a description of the parameter, refer to the general VLO documentation.
//     *
//     * @param param the value, a list of facet field-keys
//     */
//    public void setFacetFieldKeys(List<String> param) {
//        facetField = param;
//    }
//
//    /**
//     * @return Collection of field-keys
//     */
//    public Collection<String> getSearchResultFieldKeys() {
//        return searchResultField;
//    }
//
//    /**
//     * @return Collection of resolved field-names
//     */
//    public Collection<String> getSearchResultFieldNames() {
//        return searchResultField.stream().map(key -> this.fields.get(key)).collect(Collectors.toList());
//    }
//
//    public void setSearchResultFieldKeys(Set<String> searchResultField) {
//        this.searchResultField = searchResultField;
//    }
//    
//    
//
//    /**
//     * @return Set of field-keys
//     */
//    public Set<String> getIgnoredFieldKeys() {
//        return this.ignoredField;
//    }
//
//    /**
//     * @return Set of resolved field-names
//     */
//    public Set<String> getIgnoredFieldNames() {
//        return this.ignoredField.stream().map(key -> this.fields.get(key)).collect(Collectors.toSet());
//    }
//
//    public void setIgnoredFieldKeys(Set<String> ignoredFields) {
//        this.ignoredField = ignoredFields;
//    }
//
//    /**
//     * @return Set of field-keys
//     */
//    public Set<String> getTechnicalFieldKeys() {
//        return this.technicalField;
//    }
//
//    /**
//     * @return Set of resolved field-names
//     */
//    public Set<String> getTechnicalFieldNames() {
//        return this.technicalField.stream().map(key -> this.fields.get(key)).collect(Collectors.toSet());
//    }
//
//    public void setTechnicalFieldKeys(Set<String> technicalFields) {
//        this.technicalField = technicalFields;
//    }
