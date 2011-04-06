package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="facetConcepts")
public class FacetConceptMapping {
    
    @XmlElement(name="facetConcept")
    private List<FacetConcept> facetConcepts;

    
    public List<FacetConcept> getFacetConcepts() {
        return facetConcepts;
    }

    
    public void setFacetConcepts(List<FacetConcept> facetConcepts) {
        this.facetConcepts = facetConcepts;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name="facetConcept")
    public static class FacetConcept {
        @XmlAttribute
        private String name;
        
        @XmlAttribute
        private boolean isCaseInsensitive = false;
        
        @XmlElement(name="concept")
        private List<String> concepts = new ArrayList<String>();
        
        @XmlElement(name="pattern")
        private List<String> patterns = new ArrayList<String>();

        public void setConcepts(List<String> concepts) {
            this.concepts = concepts;
        }

        public List<String> getConcepts() {
            return concepts;
        }

        
        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setCaseInsensitive(boolean isCaseInsensitive) {
            this.isCaseInsensitive = isCaseInsensitive;
        }

        public boolean isCaseInsensitive() {
            return isCaseInsensitive;
        }

        public void setPatterns(List<String> patterns) {
            this.patterns = patterns;
        }

        public List<String> getPatterns() {
            return patterns;
        }
        @Override
        public String toString() {
            return "name="+name+", patterns="+patterns+", concepts="+concepts;
        }
    }


}
