package eu.clarin.cmdi.vlo.importer;

import java.io.File;

public interface CMDIDataProcessor extends SelfLinkExtractor {

    /**
     * Extract content from CMDI file
     * @param file CMDI file
     * @return
     * @throws Exception 
     */
    public CMDIData process(File file, ResourceStructureGraph resourceStructureGraph) throws Exception ;
}
