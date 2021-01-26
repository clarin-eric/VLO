package eu.clarin.cmdi.vlo.importer.mapping;

import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A list of facets. One FacetConfiguration for each facet.
 */
public class FacetsMapping implements Serializable, Cloneable {

    private final static Logger LOG = LoggerFactory.getLogger(FacetsMapping.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final Map<String, FacetDefinition> facetsMap = new LinkedHashMap<>();
    private final Map<String, Facet> facetsConfigurationMap;

    public FacetsMapping(Map<String, Facet> facetsConfigurationMap) {
        if (facetsConfigurationMap instanceof Serializable) {
            this.facetsConfigurationMap = facetsConfigurationMap;
        } else {
            throw new IllegalArgumentException("facets configuration map must be Serializable");
        }
    }

    public Map<String, Facet> getFacetsConfigurations() {
        return facetsConfigurationMap;
    }

    public Collection<FacetDefinition> getFacetDefinitions() {
        return this.facetsMap.values();
    }

    public Collection<String> getFacetConfigurationNames() {
        return this.facetsMap.keySet();
    }

    public Optional<Facet> getFacetConfiguration(String facetName) {
        return Optional.ofNullable(facetsConfigurationMap.get(facetName));
    }

    public FacetDefinition getFacetDefinition(String facetName) {
        return this.facetsMap.computeIfAbsent(facetName, fn -> new FacetDefinition(this, fn));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(baos).writeObject(this);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return new ObjectInputStream(bais).readObject();
        } catch (IOException | ClassNotFoundException ex) {
            LOG.error("at least one involved object is not serializable", ex);
            throw new CloneNotSupportedException();
        }
    }

    @Override
    public String toString() {
        return facetsMap.toString();
    }
}
