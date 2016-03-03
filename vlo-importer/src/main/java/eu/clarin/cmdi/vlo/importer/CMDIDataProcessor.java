package eu.clarin.cmdi.vlo.importer;

import java.io.File;

public interface CMDIDataProcessor {

    /**
     * Extract content from CMDI file
     * @param file CMDI file
     * @return
     * @throws Exception 
     */
    public CMDIData process(File file) throws Exception ;

    /**
     * Extract mdSelfLink from CMDI file (stored in /CMD/Header/MdSelfLink)
     * @param file CMDI file
     * @return mdSelfLink, null if /CMD/Header/MdSelfLink does not exist
     * @throws Exception 
     */
    public String extractMdSelfLink(File file) throws Exception;
}
