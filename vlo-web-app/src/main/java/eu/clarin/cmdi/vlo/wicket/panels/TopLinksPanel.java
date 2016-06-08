/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.wicket.panels;

import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.panels.BootstrapDropdown.DropdownMenuItem;
import java.io.Serializable;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.slf4j.LoggerFactory;

/**
 * A panel with three links:
 * <ul>
 * <li>A link to toggle a text input which shows a bookmarkable link to the
 * current page with parameters representing the current model (permalink)</li>
 * <li>A link to the help pages (/help)</li>
 * <li>A feedback link for the current page (base URL taken from {@link VloConfig#getFeedbackFromUrl()
 * })</li>
 * </ul>
 *
 * @author twagoo
 */
public class TopLinksPanel extends Panel {

    @SpringBean
    private VloConfig vloConfig;

    private final Model<Boolean> linkVisibilityModel;
    private final IModel<String> linkModel;
    private final IModel<String> pageTitleModel;

    public TopLinksPanel(String id, final IModel<String> linkModel, final IModel<String> pageTitleModel) {
        super(id);
        this.linkModel = linkModel;
        this.pageTitleModel = pageTitleModel != null ? pageTitleModel : new Model<String>(null);
        this.linkVisibilityModel = new Model<>(false);

        add(new BootstrapDropdown("shareOptions", new ListModel<>(getShareMenuOptions())) {
            @Override
            protected Component createDropDownLink(String id) {

                return super.createDropDownLink(id)
                        .add(new AttributeAppender("class", "btn-sm", " "));
            }

            @Override
            protected Serializable getButtonIconClass() {
                return "fa fa-share-alt";
            }

        });

        // action to link to request the permalink
        add(createPermaLink("linkRequest"));
        // field that holds the actual link
        add(createLinkField("linkfield", linkModel));

        add(new Link("feedback") {

            @Override
            public void onClick() {
                // construct a feedback URL; this takes the current page URL as a parameter
                // (needs to be URL encoded)
                final String thisPageUrlParam = UrlEncoder.QUERY_INSTANCE.encode(linkModel.getObject(), "UTF-8");
                final String feedbackUrl = vloConfig.getFeedbackFromUrl() + thisPageUrlParam;
                // tell Wicket to redirect to the constructed feedback URL
                getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(feedbackUrl));
            }
        });
    }

    private List<DropdownMenuItem> getShareMenuOptions() {
        return Lists
                .newArrayList(new DropdownMenuItem("Bookmark this", "fa fa-bookmark fw") {
                    @Override
                    protected Link getLink(String id) {
                        return new Link(id) {
                            @Override
                            public void onClick() {
                                //TODO
                            }
                        };
                    }
                }, new DropdownMenuItem("Copy link", "fa fa-clipboard fw") {
                    @Override
                    protected Link getLink(String id) {
                        return new Link(id) {
                            @Override
                            public void onClick() {
                                //TODO
                            }
                        };
                    }
                }, new DropdownMenuItem("Send link by e-mail", "fa fa-envelope fw") {
                    @Override
                    protected Link getLink(String id) {
                        return new Link(id) {
                            @Override
                            public void onClick() {
                                final String url
                                        = String.format("mailto:?subject=%s&body=%s",
                                                //interestingly, for 'mailto' links it seems that the parameters need to be encoded using the path strategy...
                                                //see http://stackoverflow.com/a/1211256 and https://en.wikipedia.org/wiki/Mailto
                                                encodePath(pageTitleModel.getObject()),
                                                encodePath(linkModel.getObject()));
                                throw new RedirectToUrlException(url);
                            }
                        };
                    }
                }, new DropdownMenuItem("Share this on Twitter", "fa fa-twitter-square fw") {
                    @Override
                    protected Link getLink(String id) {
                        return (Link) new Link(id) {
                            @Override
                            public void onClick() {
                                final String url
                                        = String.format("https://twitter.com/home?status=%s",
                                                encodeParam(String.format("%s %s",
                                                        pageTitleModel.getObject(),
                                                        linkModel.getObject())));
                                throw new RedirectToUrlException(url);
                            }
                        }.add(new AttributeAppender("target", "_blank"));
                    }
                }, new DropdownMenuItem("Share this on Facebook", "fa fa-facebook-square fw") {
                    @Override
                    protected Link getLink(String id) {
                        return (Link) new Link(id) {
                            @Override
                            public void onClick() {
                                final String url
                                        = String.format("http://www.facebook.com/sharer/sharer.php?u=%s",
                                                encodeParam(linkModel.getObject()));
                                throw new RedirectToUrlException(url);
                            }
                        }.add(new AttributeAppender("target", "_blank"));
                    }
                }, new DropdownMenuItem("Share this on LinkedIn", "fa fa-linkedin-square fw") {
                    @Override
                    protected Link getLink(String id) {
                        return (Link) new Link(id) {
                            @Override
                            public void onClick() {
                                final String url
                                        = String.format("https://www.linkedin.com/shareArticle?url=%s&title=%s",
                                                encodeParam(linkModel.getObject()),
                                                encodeParam(pageTitleModel.getObject()));
                                throw new RedirectToUrlException(url);
                            }
                        }.add(new AttributeAppender("target", "_blank"));
                    }
                }
                );
    }

    private Component createPermaLink(String id) {
        // Create a form with a button to toggle permalink rather than an action link
        // to prevent people from confusing the link generated by wicket with
        // the actual permalink generated by the application
        final Form form = new Form(id) {

            @Override
            protected void onConfigure() {
                setVisible(linkModel != null);
            }
        };

        form.add(new AjaxFallbackButton("linkRequestButton", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                // toggle
                linkVisibilityModel.setObject(!linkVisibilityModel.getObject());

                if (target != null && linkVisibilityModel.getObject()) {
                    target.appendJavaScript("permalinkShown();");
                }

                // callback to react to change
                onChange(target);
            }

        });

        return form;
    }

    private TextField<String> createLinkField(String id, final IModel<String> linkModel) {
        final TextField<String> linkField = new TextField<String>(id, linkModel) {

            @Override
            protected void onConfigure() {
                setVisible(linkVisibilityModel.getObject());
            }

        };
        return linkField;
    }

    protected void onChange(AjaxRequestTarget target) {
        if (target != null) {
            target.add(getPage());
        }
    }

    @Override
    protected void onConfigure() {
        LoggerFactory.getLogger(getClass()).debug("top links panel onconfigure");
    }

    private static String encodeParam(String param) {
        return UrlEncoder.QUERY_INSTANCE.encode(param, "UTF-8");
    }

    private static String encodePath(String param) {
        return UrlEncoder.PATH_INSTANCE.encode(param, "UTF-8");
    }

}
