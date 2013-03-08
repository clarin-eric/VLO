
package eu.clarin.cmdi.vlo.config;

import java.io.File;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Description of the location from which the importer collects meta data.
 * 
 * @author keeloo
 */
@Root // Simple directive 
public class DataRoot extends Object {
    
    /**
     * Constructor method
     */
    public DataRoot (){
    }
    
    /**
     * Constructor method
     * 
     * @param originName
     * @param rootFile
     * @param prefix
     * @param toStrip
     * @param deleteFirst 
     */
    public DataRoot(String originName, File rootFile, String prefix, String toStrip, Boolean deleteFirst) {
        this.originName = originName;
        this.rootFile = rootFile;
        this.prefix = prefix;
        this.tostrip = toStrip;
        this.deleteFirst = deleteFirst;
    }
    
    /**
     *
     * @param dataRoot
     * @return
     */
    @Override
    public boolean equals (Object object){
        boolean equal = false;
        
        if (object == null) {
            // define this object to be different from nothing
        } else {
            if (! (object instanceof DataRoot)) {
                // the object is not a DataRoot, define it not to be equal 
            } else {
                equal = this.originName.equals(((DataRoot) object).originName);
                equal = this.rootFile.equals(((DataRoot) object).rootFile) && equal;
                equal = this.prefix.equals(((DataRoot) object).prefix) && equal;
                equal = this.tostrip.equals(((DataRoot) object).tostrip) && equal;

                equal = this.deleteFirst == ((DataRoot) object).deleteFirst && equal;
            }
        }
        
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.originName != null ? this.originName.hashCode() : 0);
        hash = 29 * hash + (this.rootFile != null ? this.rootFile.hashCode() : 0);
        hash = 29 * hash + (this.prefix != null ? this.prefix.hashCode() : 0);
        hash = 29 * hash + (this.tostrip != null ? this.tostrip.hashCode() : 0);
        hash = 29 * hash + (this.deleteFirst ? 1 : 0);
        return hash;
    }

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
     * importer has submitted to the SOLR server.
     */
    @Element // Simple directive
    private String originName;
 
    /**
     * Meta data to be imported. The rootFile can take the form of a root CMDI
     * file, a file for which the importer will follow the links defined in it.
     * It can also be a directory with .xml and or .cmdi files to be imported.
     * The importer will process the directories recursively.
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
     * you get the URL at which the importer looks for files. In the case of the
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

