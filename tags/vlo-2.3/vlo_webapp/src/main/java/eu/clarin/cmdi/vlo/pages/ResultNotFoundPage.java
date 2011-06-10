package eu.clarin.cmdi.vlo.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.time.Duration;

public class ResultNotFoundPage extends BasePage {

    public ResultNotFoundPage(final PageParameters parameters) {
	super(parameters);
	Link link = new Link("redirectPage") {
	    private static final long serialVersionUID = 1L;

	    public void onClick() {
		setResponsePage(FacetedSearchPage.class, parameters);
	    };
	};
	link.add(new AbstractAjaxTimerBehavior(Duration.seconds(3)) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected void onTimer(AjaxRequestTarget target) {
		setResponsePage(FacetedSearchPage.class, parameters);
	    }
	});
	add(link);
    }

}
