package eu.clarin.cmdi.vlo.importer.processor;

import java.io.File;
import java.io.IOException;

public interface SelfLinkExtractor {

    /**
     * Extract mdSelfLink from CMDI file (stored in /CMD/Header/MdSelfLink)
     * @param file CMDI file
     * @return mdSelfLink, null if /CMD/Header/MdSelfLink does not exist
     * @throws IOException 
     */
    public String extractMdSelfLink(File file) throws IOException;
}
