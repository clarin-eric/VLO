package eu.clarin.cmdi.vlo.importer;

import java.util.List;

/**
 * Represents the configuration of the metadata importer.
 */

public class ImporterConfig {

    /**
     * default location of config file.
     */
    public static final String CONFIG_FILE = "importerConfig.xml";

    /**
     * whether to delete all.
     */
    private boolean deleteAllFirst = false;

    /**
     * whether to print all xpath mappings found (to a file).
     */
    private boolean printMapping = false;

    /**
     * The list of all DataRoots (which define a directory to search for metadata files and some other things)
     */
    private List<DataRoot> dataRoots;

    // Getters and Setters below.


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
