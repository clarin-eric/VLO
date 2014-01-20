package eu.clarin.cmdi.vlo.pages;

public class ImageResource {

    private final String resource;
    private final String title;

    public ImageResource(String resource, String title) {
        this.resource = resource;
        this.title = title;
    }

    public String getResource() {
        return resource;
    }

    public String getTitle() {
        return title;
    }

}
