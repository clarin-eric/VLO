package eu.clarin.cmdi.vlo.importer;

import java.util.List;

public class ImporterConfig {

    private boolean deleteFirst = false;
    
    private List<DataRoot> dataRoots;

    public List<DataRoot> getDataRoots() {
        return dataRoots;
    }
    
    public void setDataRoots(List<DataRoot> dataRoots) {
        this.dataRoots = dataRoots;
    }

    public void setDeleteFirst(boolean deleteFirst) {
        this.deleteFirst = deleteFirst;
    }

    public boolean isDeleteFirst() {
        return deleteFirst;
    }

}
