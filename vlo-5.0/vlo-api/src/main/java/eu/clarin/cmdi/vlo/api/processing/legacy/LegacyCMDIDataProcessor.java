package eu.clarin.cmdi.vlo.api.processing.legacy;

import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;

import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;

/**
 * Service that processes a single file and creates and populates a
 * {@link CMDIData} object representing it
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @param <T> Type of storage for a single document
 */
public interface LegacyCMDIDataProcessor<T> {

    /**
     * Extract content from CMDI file
     *
     * @param request mapping request
     * @param resourceStructureGraph
     * @return
     * @throws Exception
     */
    public CMDIData<T> process(VloRecordMappingRequest request, ResourceStructureGraph resourceStructureGraph) throws Exception;
}
