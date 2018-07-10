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
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
 * A panel with two components:
 * <ul>
 * <li>A dropdown menu with various share options, including two that trigger
 * {@link BookmarkLinkPanel}</li>
 * <li>A feedback link for the current page (base URL taken from {@link VloConfig#getFeedbackFromUrl()
 * })</li>
 * </ul>
 *
 * @author twagoo
 */
public class TopLinksPanel extends Panel {

    @SpringBean
    private VloConfig vloConfig;

    private final IModel<String> linkModel;
    private final IModel<String> pageTitleModel;
    private final IModel<Boolean> inlineBookmarkLinkPanelVisibilityModel;

    private final BookmarkLinkPanel modalBookmarkLinkPanel;
    private final BookmarkLinkPanel inlineBookmarkLinkPanel;
    private final BootstrapModal linkModal;
    private final BootstrapDropdown shareMenu;

    public TopLinksPanel(String id, final IModel<String> linkModel, final IModel<String> pageTitleModel) {
        super(id);
        this.linkModel = linkModel;
        this.pageTitleModel = pageTitleModel != null ? pageTitleModel : new Model<String>(null);
        shareMenu = new BootstrapDropdown("shareOptions", new ListModel<>(getShareMenuOptions())) {
            @Override
            protected Component createDropDownLink(String id) {

                return super.createDropDownLink(id)
                        .add(new AttributeAppender("class", "btn-sm", " "));
            }

            @Override
            protected Serializable getButtonIconClass() {
                return "fa fa-share-alt";
            }

        };

        add(shareMenu);

        // modal dialogue for bookmark/copy link
        linkModal = new BootstrapModal("linkPanel") {
            @Override
            protected IModel<String> getTitle() {
                return Model.of("Page link");
            }
        };
        modalBookmarkLinkPanel = new BookmarkLinkPanel(linkModal.getContentId(), linkModel, pageTitleModel);
        add(linkModal.add(modalBookmarkLinkPanel));

        // inline 'dialogue' for bookmark/copy link (non-js alternative for modal)
        inlineBookmarkLinkPanelVisibilityModel = Model.of(false);
        add(new WebMarkupContainer("inlineBookmarkPanel") {
            @Override
            protected void onConfigure() {
                setVisible(inlineBookmarkLinkPanelVisibilityModel.getObject());
            }
        }
                .add(inlineBookmarkLinkPanel = new BookmarkLinkPanel("linkPanel", linkModel, pageTitleModel))
                .add(new Link("close") {
                    @Override
                    public void onClick() {
                        inlineBookmarkLinkPanelVisibilityModel.setObject(false);
                    }
                })
        );

        // feedback link
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

    private void showLinkModal(AjaxRequestTarget target) {
        if (target == null) {
            inlineBookmarkLinkPanelVisibilityModel.setObject(Boolean.TRUE);
        } else {
            target.appendJavaScript("onModalShown();");
            linkModal.show(target);
        }
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

    @Override
    public void detachModels() {
        super.detachModels();
        if (linkModel != null) {
            linkModel.detach();
        }
        if (pageTitleModel != null) {
            pageTitleModel.detach();
        }
    }

    private List<DropdownMenuItem> getShareMenuOptions() {
        return Lists
                .newArrayList(new DropdownMenuItem("Bookmark this", "fa fa-bookmark fw") { //Bookmark
                    @Override
                    protected Link getLink(String id) {
                        return new AjaxFallbackLink(id) {

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                shareMenu.close();
                                modalBookmarkLinkPanel.setBookmarkMode();
                                inlineBookmarkLinkPanel.setBookmarkMode();
                                showLinkModal(target);
                            }
                        };
                    }
                }, new DropdownMenuItem("Copy link", "fa fa-clipboard fw") { //Clipboard
                    @Override
                    protected Link getLink(String id) {
                        return new AjaxFallbackLink(id) {

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                shareMenu.close();
                                modalBookmarkLinkPanel.setCopyMode();
                                inlineBookmarkLinkPanel.setCopyMode();
                                showLinkModal(target);
                            }
                        };
                    }
                }, new DropdownMenuItem("Send link by e-mail", "fa fa-envelope fw") { //E-mail
                    @Override
                    protected Link getLink(String id) {
                        return new Link(id) {
                            @Override
                            public void onClick() {
                                final String url
                                        = String.format("mailto:?subject=%s&body=%s",
                                                encodeMailtoParamValue(pageTitleModel.getObject()),
                                                encodeMailtoParamValue(linkModel.getObject()));
                                throw new RedirectToUrlException(url);
                            }
                        };
                    }
                }
                );
    }

    private static String encodeMailtoParamValue(String param) {
        //interestingly, for 'mailto' links it seems that the parameters need to be encoded using the path strategy...
        //see http://stackoverflow.com/a/1211256 and https://en.wikipedia.org/wiki/Mailto
        return UrlEncoder.PATH_INSTANCE.encode(param, "UTF-8")
                //encode reserved characters (see https://github.com/clarin-eric/VLO/issues/180)
                .replaceAll("\\?", UrlEncoder.QUERY_INSTANCE.encode("?", "UTF-8"))
                .replaceAll("\\+", UrlEncoder.QUERY_INSTANCE.encode("+", "UTF-8"))
                .replaceAll("\\:", UrlEncoder.QUERY_INSTANCE.encode(":", "UTF-8"))
                .replaceAll("&", UrlEncoder.QUERY_INSTANCE.encode("&", "UTF-8"));
    }

}
