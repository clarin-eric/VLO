package eu.clarin.cmdi.vlo.importer;

import java.util.List;

/**
 * Defines the interface for a postprocessor.
 *
 * Such a postprossor is called on a single facet value after this facet value
 * is inserted into the solr document during import.
 *
 * at the start of MetadataImporter which postprocessors (if any) are used for
 * which facet are defined.
 */
public interface PostProcessor {

    /**
     *
     * @param value value to post-process; can be null
     * @param cmdiData processing context, can be null or incomplete
     * @return list of post-processed values
     */
    public List<String> process(String value, CMDIData cmdiData);

    /**
     *
     * @return whether the postprocessor should also be called in case no
     * matching value was found (with <pre>value == null</pre>)
     */
    public boolean doesProcessNoValue();
}
