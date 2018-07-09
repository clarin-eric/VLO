package eu.clarin.cmdi.vlo.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A dataRoot describes the meta data sources.
 *
 * In an XML file, a dataRoot is reflected like this:<br><br>
 *
 * {@literal <DataRoot>}
 *      {@literal<originName>}name{@literal</originName>}
 *      {@literal<rootFile>}topLevelMetadataDirectory/{@literal</rootFile>}
 *      {@literal<prefix>}startOfUrl{@literal</prefix>}
 *      {@literal<tostrip>}leftPartOfRootFile{@literal</tostrip>}
 *      {@literal<deleteFirst>}falseOrTrue{@literal</deleteFirst>}
 * {@literal</DataRoot>}
 *
 * @author keeloo
 */
@XmlRootElement(name = "DataRoot")
public class DataRoot extends Object {
    /**
     * name describing the meta data
     */
    private String originName;

    /**
     * top level directory in which the meta data is stored
     */
    private File rootFile;

    /**
     * Web equivalent of the toStrip. For example:
     *
     * /lat/apache/htdocs/
     */
    private String prefix;

    /**
     * Left part of the rootFile
     *
     * By first removing {@literal tostrip} from {@literal rootFile} and then
     * append the result of that operation to the {@literal prefix} you obtain
     * the URL to the meta data.
     */
    @XmlElement(name = "tostrip")
    private String toStrip;

    /**
     * Flag to signal the removal of records from the Solr server
     *
     * The value of this flag overrides the value defined in the {
     *
     * @lieteral VloConfig.xml} file. With the deleteFirst flag you can control
     * the removal of the records originating from originName.
     */
    @XmlElement(name="deleteFirst")
    private boolean deleteFirst = false;
    
    /**
     * String containing all directories that shouldn't be used for building a
     * hierarchy graph (comma separated)
     */
    @XmlAttribute
    private String ignoreHierarchyDirs;

    /**
     * Get the value of the prefix element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @return the value
     */


    /**
     * Constructor method
     */
    public DataRoot() {
    }

    /**
     * Constructor method
     *
     * @param originName name describing the meta data
     * @param rootFile top level directory in which the meta data is stored
     * @param prefix left part of the rootFile
     * @param toStrip if you want to create the URL to the meta data, this is
     * the part to be removed from the rootFile
     * @param deleteFirst
     * @param processHierarchyDirs list of directories for which a hierarchy graph
     * will be created (comma separated) or "*" for including all directories
     * @param ignoreHierarchyDirs String containing all directories that shouldn't be used for building a
     * hierarchy graph (comma separated)
     */
    public DataRoot(String originName, File rootFile, String prefix, String toStrip, Boolean deleteFirst, String processHierarchyDirs, String ignoreHierarchyDirs) {
        this.originName = originName;
        this.rootFile = rootFile;
        this.prefix = prefix;
        this.toStrip = toStrip;
        this.deleteFirst = deleteFirst;
        this.processHierarchyDirs = processHierarchyDirs;
        this.ignoreHierarchyDirs = ignoreHierarchyDirs;
    }

    public DataRoot(String originName, File rootFile, String prefix, String toStrip, Boolean deleteFirst) {
        // by default create structure graph for all directories
        this(originName, rootFile, prefix, toStrip, deleteFirst, "*", "");
    }

    /**
     * Test for equality of the object itself and the object passed to it
     *
     * @param object
     * @return true if the object equals this, false otherwise
     */
    @Override
    public boolean equals(Object object) {
        boolean equal = false;

        if (object == null) {
            // define this object to be different from nothing
        } else {
            if (!(object instanceof DataRoot)) {
                // the object is not a DataRoot, define it not to be equal 
            } else {
                equal = this.originName.equals(((DataRoot) object).originName);
                equal = this.rootFile.equals(((DataRoot) object).rootFile) && equal;
                equal = this.prefix.equals(((DataRoot) object).prefix) && equal;
                equal = this.toStrip.equals(((DataRoot) object).toStrip) && equal;

                equal = this.deleteFirst == ((DataRoot) object).deleteFirst && equal;
            }
        }

        return equal;
    }

    /**
     * Generate by the ide
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.originName != null ? this.originName.hashCode() : 0);
        hash = 29 * hash + (this.rootFile != null ? this.rootFile.hashCode() : 0);
        hash = 29 * hash + (this.prefix != null ? this.prefix.hashCode() : 0);
        hash = 29 * hash + (this.toStrip != null ? this.toStrip.hashCode() : 0);
        hash = 29 * hash + (this.deleteFirst ? 1 : 0);
        return hash;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the value of the prefix element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @param prefix the value
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the value of the {@literal tostrip} element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getToStrip() {
        return toStrip;
    }

    /**
     * Set the value of the {@literal tostrip} element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @param tostrip the value
     */
    public void setTostrip(String tostrip) {
        this.toStrip = tostrip;
    }

    /**
     * Get the value of the originName element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getOriginName() {
        return originName;
    }

    /**
     * Set the value of the originName element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @param originName the value
     */
    public void setOriginName(String originName) {
        this.originName = originName;
    }

    /**
     * Get the value of the rootFile element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @return the value
     */
    public File getRootFile() {
        return rootFile;
    }

    /**
     * Set the value of the rootFile element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @param rootFile the value
     */
    public void setRootFile(File rootFile) {
        this.rootFile = rootFile;
    }

    /**
     * Set the value of the deleteFirst element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @param deleteFirst the value
     */
    public void setDeleteFirst(boolean deleteFirst) {
        this.deleteFirst = deleteFirst;
    }

    /**
     * Get the value of the deleteFirst element<br><br>
     *
     * For a description of the element, refer to the general VLO documentation.
     *
     * @return the value
     */
    public boolean deleteFirst() {
        return deleteFirst;
    }

    /**
     * String containing all directories that should be used for building a
     * hierarchy graph or '*' if all directories of this data root should
     * be used.
     */
    @XmlAttribute
    private String processHierarchyDirs;

    public void setProcessHierarchyDirs(String processHierarchyDirs) {
        this.processHierarchyDirs = processHierarchyDirs;
    }

    public List<String> getProcessHierarchyDirList() {
        // by default: all directories ('*') will be used
        if(processHierarchyDirs == null) {
            List<String> defaultList = new ArrayList<>();
            defaultList.add("*");
            return defaultList;
        } else {
            return Arrays.asList(processHierarchyDirs.trim().split(","));
        }
    }


    public void setIgnoreHierarchyDirs(String ignoreHierarchyDirs) {
        this.ignoreHierarchyDirs = ignoreHierarchyDirs;
    }

    public List<String> getIgnoreHierarchyDirList() {
        // by default: no directory will be ignored
        if(ignoreHierarchyDirs == null)
            return new ArrayList<>();
        else
            return Arrays.asList(ignoreHierarchyDirs.trim().split(","));
    }

    @Override
    public String toString() {
        return String.format("originName: %s; rootFile: %s; prefix: %s; toStrip: %s; deleteFirst: %b", originName, rootFile, prefix, toStrip, deleteFirst);
    }

}
