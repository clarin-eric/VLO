
package eu.clarin.cmdi.vlo.config;

import java.util.List;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Need to check: adapt the importerConfig class in the same way as the web
 * application configuration class. 
 * 
 * Definition of the VLO importer parameters.
 * 
 * Note: perhaps explain the mapping itself.
 *
 * @author keeloo
 */
@Root // directive for Simple
public class ImporterConfig extends ConfigFileParam {
    
    /**
     * Definition of the name of the configuration file.
     *
     * @return
     */
    @Override
    public String getFileName() {
        return "ImporterConfig.xml";
    }
    
    // 'override' the base class method
    public static synchronized ImporterConfig get() {
        return (ImporterConfig)ImporterConfig.get();
    }
    
    /**
     * Flag to signal the records in the data to be deleted before the ingestion
     * starts.
     */
    @Element // directive for Simple
    private boolean deleteAllFirst = false;
    
    /**
     * Flag that leads to the printing of XPATH mappings encountered. Note: need
     * to be more specific on this.
     */
    @Element
    private boolean printMapping = false;
    
    /**
     * A list of data roots, that is: directories from which the importer
     * collects meta data. Note: need to elaborate on this.
     */
    @ElementList // directive for Simple
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
