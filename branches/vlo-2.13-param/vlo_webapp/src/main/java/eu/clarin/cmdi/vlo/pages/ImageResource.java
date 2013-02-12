package eu.clarin.cmdi.vlo.pages;

import org.apache.wicket.Resource;

public class ImageResource {

    private final Resource resource;
    private final String title;

    public ImageResource(Resource resource, String title) {
        this.resource = resource;
        this.title = title;
    }

    public Resource getResource() {
        return resource;
    }

    public String getTitle() {
        return title;
    }

}
