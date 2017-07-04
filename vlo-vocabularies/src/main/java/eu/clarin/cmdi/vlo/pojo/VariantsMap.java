package eu.clarin.cmdi.vlo.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.vlo.normalization.NormalizationVocabulary;
import eu.clarin.cmdi.vlo.normalization.VocabularyEntry;

/**
 * @author dostojic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings")
public class VariantsMap {

    @XmlAttribute
    private String field;

    @XmlElement(name = "mapping")
    private List<Mapping> mappings;

    public List<Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    /**
     * returns inverted map: variant-normalizedVal
     *
     */
    public Map<String, String> getInvertedMap() {
        Map<String, String> invMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Mapping m : mappings) {
            if (m.getVariants() != null) {
                for (Variant variant : m.getVariants()) {
                    if (!variant.isRegExp()) {
                        invMap.put(variant.getValue(), m.getValue());
                    }
                }
            }
        }

        return invMap;
    }

    public NormalizationVocabulary getMap() {
        final List<VocabularyEntry> listOfEntries = new ArrayList<>(mappings.size());
        boolean containsRegEx = false;

        for (Mapping m : mappings) {
            if (m.getVariants() != null) {
                String normalizedValue = m.getValue().trim();
                //add one entry for normalised values
                listOfEntries.add(new VocabularyEntry(normalizedValue.toLowerCase(), normalizedValue, false, null));

                for (Variant v : m.getVariants()) {
                    if (!containsVocabularyEntry(listOfEntries, v.getValue().trim().toLowerCase())) {
                        listOfEntries.add(new VocabularyEntry(v.getValue().trim().toLowerCase(), normalizedValue, v.isRegExp(), v.getCrossMappings()));
                    }
                    if (v.isRegExp()) {
                        containsRegEx = true;
                    }
                }
            }
        }

        return new NormalizationVocabulary(listOfEntries, containsRegEx);

    }

    private boolean containsVocabularyEntry(List<VocabularyEntry> listOfEntries, String val) {
        for (VocabularyEntry entry : listOfEntries) {
            if (entry.getOriginalVal().equals(val)) {
                return true;
            }
        }

        return false;
    }

}
