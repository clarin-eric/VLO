
package eu.clarin.cmdi.vlo.config;

import java.io.File;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Description of the location from which the ingester collects meta data.
 * 
 * @author keeloo
 */
@Root // Simple directive 
public class DataRoot {

    /**
     * Label of the data root. For example 
     * 
     * "MPI CMDIfied IMDI archive"
     * 
     * or 
     * 
     * "LRT Inventory"
     * 
     * The originName label can be used to reference the records the
     * ingester has submitted to the SOLR server.
     */
    @Element // Simple directive
    private String originName;
 
    /**
     * Meta data to be ingested. The rootFile can take the form of a root CMDI
     * file, a file for which the ingester will follow the links defined in it.
     * It can also be a directory with .xml and or .cmdi files to be ingested.
     * The ingester will process the directories recursively.
     *    
     * Example:
     *
     * /lat/apache/htdocs/oai-harvester/mpi-self-harvest/harvested/results/cmdi/
     * 
     * Normally, rootFile resides under a web server.
     */
    @Element
    private File rootFile;
    
    /**
     * Web equivalent of the toStrip. For example: 
     * 
     * /lat/apache/htdocs/
     */
    @Element  
    private String prefix;

    /**
     * Filesystem equivalent of the prefix. For example:
     * 
     * http://catalog.clarin.eu/
     * 
     * If you remove toStrip from rootFile, and append the result to the prefix,
     * you get the URL at which the ingester looks for files. In the case of the
     * examples this would be the result: 
     * 
     * http://catalog.clarin.eu/oai-harvester/mpi-self-harvest/harvested/results/cmdi/
     */
    @Element
    private String tostrip; // refactor this to toStrip
    
    /**
     * Flag to signal the removal of records SOLR server. This flag overrides
     * the flag in the VLOConfig class. The deleteFirst flag controls the
     * removal of the records originating from originName.
     */
    @Element
    private boolean deleteFirst = false;
    
    
    /**
     * Remove the get and set methods declared below one by one.
     */

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTostrip() {
        return tostrip;
    }

    public void setTostrip(String tostrip) {
        this.tostrip = tostrip;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public File getRootFile() {
        return rootFile;
    }

    public void setRootFile(File rootFile) {
        this.rootFile = rootFile;
    }

    public void setDeleteFirst(boolean deleteFirst) {
        this.deleteFirst = deleteFirst;
    }

    public boolean isDeleteFirst() {
        return deleteFirst;
    }
}

