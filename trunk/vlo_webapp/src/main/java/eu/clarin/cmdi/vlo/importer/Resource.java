package eu.clarin.cmdi.vlo.importer;

public class Resource {

    private final String resourceName;
    private final String mimeType;

    public Resource(String resourceName, String mimeType) {
        this.resourceName = resourceName;
        this.mimeType = mimeType == "" ? null : mimeType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getMimeType() {
        return mimeType;
    }

}
