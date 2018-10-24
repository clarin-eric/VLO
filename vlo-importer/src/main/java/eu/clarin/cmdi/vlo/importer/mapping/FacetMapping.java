package eu.clarin.cmdi.vlo.importer.mapping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A list of facets. One FacetConfiguration for each facet.
 */
public class FacetMapping implements Serializable, Cloneable {

    private final static Logger LOG = LoggerFactory.getLogger(FacetMapping.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private LinkedHashMap<String, FacetConfiguration> facetsMap = new LinkedHashMap<String, FacetConfiguration>();

    public Collection<FacetConfiguration> getFacetConfigurations() {
        return this.facetsMap.values();
    }

    public Collection<String> getFacetConfigurationNames() {
        return this.facetsMap.keySet();
    }

    public FacetConfiguration getFacetConfiguration(String facetName) {
        return this.facetsMap.computeIfAbsent(facetName, fn -> new FacetConfiguration(this, fn));
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
