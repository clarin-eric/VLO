package eu.clarin.cmdi.vlo.pages;

/**
 *
 * @author paucas
 */
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

class AnchorPanel extends Panel {

    public AnchorPanel(String id, String value) {
        super(id);
        ExternalLink link = new ExternalLink("anchorName", value);
        link.add(new AttributeAppender("name", new Model(value), ""));
        add(link);
        add(new Label("anchorLabel", value));
    }
}
