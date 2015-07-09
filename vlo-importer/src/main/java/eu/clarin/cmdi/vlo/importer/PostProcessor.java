package eu.clarin.cmdi.vlo.importer;

import java.util.List;

/**
 * Defines the interface for a postprocessor.
 *
 * Such a postprossor is called on a single facet value after this facet value is inserted into the solr document during import.
 *
 * at the start of MetadataImporter which postprocessors (if any) are used for which facet are defined.
 */

public interface PostProcessor {

    public List<String> process(String value);

}
