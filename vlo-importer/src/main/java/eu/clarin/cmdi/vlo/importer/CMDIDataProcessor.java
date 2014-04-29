package eu.clarin.cmdi.vlo.importer;

import java.io.File;

public interface CMDIDataProcessor {

    public CMDIData process(File file) throws Exception ;
}
