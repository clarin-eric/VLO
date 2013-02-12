package eu.clarin.cmdi.vlo.importer;

import java.io.File;

public class DataRoot {

    /**
     * The name of this dataroot. Some string. Try to keep it descriptive. E.g. "MPI CMDIfied IMDI archive", "LRT Inventory"
     */
    private String originName;
    /**
     * The directory under which to search for metadata files.
     */
    private File rootFile;
    /**
     * The location on some webserver where we can directly link to each metadata file found. E.g. http://localhost/cmdi/
     */
    private String prefix;
    /**
     * The bit of the full-path to the file to strip from the file name such that, when combined with the prefix string, one can link directly to the cmdi file.
     *
     * For example:
     * CMDI file found in /var/wwwroot/cmdi/1.cmdi
     * We want to convert this to: http://localhost/cmdi/1.cmdi
     * our "tostrip" and our "rootFile" can both be /cat/wwwroot/cmdi
     * and our "prefix" can be like above.
     */
    private String tostrip;
    /**
     * whether to delete all occurences in the solr server from this originName
     */
    private boolean deleteFirst = false;

    // Getters and Setters below.

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

    /**
     * Root cmdi file (Metadata links defined in the file will be read) or root directory (*.cmdi and *.xml files will be recursively read).
     * @param rootFile
     */
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
