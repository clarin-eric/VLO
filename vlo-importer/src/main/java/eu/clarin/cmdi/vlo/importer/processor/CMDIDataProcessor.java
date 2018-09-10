package eu.clarin.cmdi.vlo.importer.processor;

import java.io.File;

import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;

/**
 * Service that processes a single file and creates and populates a
 * {@link CMDIData} object representing it
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @param <T> Type of storage for a single document
 */
public interface CMDIDataProcessor<T> {

    /**
     * Extract content from CMDI file
     *
     * @param file CMDI file
     * @param resourceStructureGraph
     * @return
     * @throws Exception
     */
    public CMDIData<T> process(File file, ResourceStructureGraph resourceStructureGraph) throws Exception;
}
