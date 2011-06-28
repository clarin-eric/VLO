package eu.clarin.cmdi.vlo.importer;

import java.io.File;

public class DataRoot {

    private String originName;
    private File rootFile;
    private String prefix;
    private String tostrip;
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
