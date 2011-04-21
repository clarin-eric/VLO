package eu.clarin.cmdi.vlo.importer;

import java.util.List;

public class ImporterConfig {

    public static final String CONFIG_FILE = "importerConfig.xml";

    private boolean deleteAllFirst = false;
    private boolean printMapping = false;
    
    private List<DataRoot> dataRoots;

    public List<DataRoot> getDataRoots() {
        return dataRoots;
    }
    
    public void setDataRoots(List<DataRoot> dataRoots) {
        this.dataRoots = dataRoots;
    }

    public void setDeleteAllFirst(boolean deleteAllFirst) {
        this.deleteAllFirst = deleteAllFirst;
    }

    public boolean isDeleteAllFirst() {
        return deleteAllFirst;
    }

    public void setPrintMapping(boolean printMapping) {
        this.printMapping = printMapping;
    }

    public boolean isPrintMapping() {
        return printMapping;
    }

}
