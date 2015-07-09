package eu.clarin.cmdi.vlo.importer;

public class Resource {

    private final String resourceName;
    private final String mimeType;
    private final String type;

    public Resource(String resourceName,String type, String mimeType) {
        this.resourceName = resourceName;
        this.mimeType = mimeType == "" ? null : mimeType;
        this.type = type;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getType(){
        return type;
    }

}
