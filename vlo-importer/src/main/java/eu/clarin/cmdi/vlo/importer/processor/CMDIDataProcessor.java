package eu.clarin.cmdi.vlo.importer.processor;

import java.io.File;

import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;

public interface CMDIDataProcessor {

    /**
     * Extract content from CMDI file
     * @param file CMDI file
     * @return
     * @throws Exception 
     */
    public CMDIData process(File file, ResourceStructureGraph resourceStructureGraph) throws Exception ;
}
