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
package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.panels.BootstrapFeedbackPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.ImmutableNavbarComponent;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar.ComponentPosition;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarExternalLink;
import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.config.SnippetConfig;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.HideJavascriptFallbackControlsBehavior;
import eu.clarin.cmdi.vlo.wicket.InvisibleIfNullBehaviour;
import eu.clarin.cmdi.vlo.wicket.model.EnvironmentVariableModel;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.include.Include;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base page for all VLO pages; has common header and footer markup and takes
 * care of the following:
 *
 * <ul>
 * <li>setting the theme if provided in query parameters;</li>
 * <li>including CSS</li>
 * <li>adding a {@link FeedbackPanel} for info/warning/error messages</li>
 * <li>hiding all JavaScript fallback controls (see
 * {@link HideJavascriptFallbackControlsBehavior});</li>
 * </ul>
 *
 * @author twagoo
 * @param <T> the type of the page's model object (see {@link GenericWebPage})
 */
public class VloBasePage<T> extends GenericWebPage<T> {

    private final static Logger logger = LoggerFactory.getLogger(VloBasePage.class);
    public static final String DEFAULT_APP_TITLE = "Virtual Language Observatory";

    public static final String VLO_APPLICATION_TITLE_ENV_VAR = "VLO_APPLICATION_TITLE";
    public static final String VLO_PAGE_TITLE_ENV_VAR = "VLO_PAGE_TITLE";
    public static final String VLO_INSTANCE_INFO_ENV_VAR = "VLO_INSTANCE_INFO";

    @SpringBean
    private VloConfig vloConfig;

    @SpringBean
    private JavaScriptResources javaScriptResources;

    @SpringBean
    private PiwikConfig piwikConfig;

    @SpringBean
    private SnippetConfig snippetConfig;
    private String bottomSnippet;

    private IModel<String> appTitleModel;
    private IModel<String> pageTitleModel;

    public VloBasePage() {
        addComponents();
    }

    public VloBasePage(IModel<T> model) {
        super(model);
        addComponents();
    }

    public VloBasePage(PageParameters parameters) {
        super(parameters);

        processTheme(parameters);
        addComponents();
    }

    /**
     * Sets the theme from the page parameters if applicable. An present but
     * empty theme value will reset the theme (by unsetting the style).
     *
     * @param parameters page parameters to process
     * @see VloWebAppParameters#THEME
     * @see Session#setStyle(java.lang.String)
     */
    private void processTheme(PageParameters parameters) {
        final StringValue themeValue = parameters.get(VloWebAppParameters.THEME);
        if (!themeValue.isNull()) {
            if (themeValue.isEmpty()) {
                // empty string resets theme
                logger.debug("Resetting theme");
                Session.get().setStyle(null);
            } else {
                // theme found, set it as style in the session
                final String theme = themeValue.toString().toLowerCase();
                logger.debug("Setting theme to {}", theme);
                Session.get().setStyle(theme);
            }

            /*
             * Remove theme parameter to prevent it from interfering with 
             * further processing, specifically the parameters check in 
             * the simple page search
             */
            parameters.remove(VloWebAppParameters.THEME, themeValue.toString());
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        // page title label is added here because it uses an overridable method
        add(new Label("title", getTitleModel()));

        // same for page description (this populates the <meta name="description" /> element)
        add(new WebComponent("pageDescription") {
            {
                add(new AttributeAppender("content", getPageDescriptionModel()));
            }
        });

        add(new WebComponent("canonicalUrl") {

            @Override
            protected void onRender() {
                final IModel<String> canonicalUrlModel = getCanonicalUrlModel();
                if (canonicalUrlModel != null) {
                    getResponse().write("<link rel=\"canonical\" href=\"" + canonicalUrlModel.getObject() + "\"/>");
                }
            }

        });

        // Add bottom snippets content if a file URL is configured to retrieve content from
        if (Strings.isEmpty(snippetConfig.getBottomSnippetFileUrl())) {
            add(new WebMarkupContainer("bottomSnippet"));
        } else {
            add(new Include("bottomSnippet", snippetConfig.getBottomSnippetFileUrl()));
        }

    }

    /**
     * Override to give the page a custom or dynamic page title
     *
     * @return string model that provides the page title
     */
    public IModel<String> getTitleModel() {
        return pageTitleModel;
    }

    /**
     * Override to give a custom or dynamic description for the page via the
     * description "meta" tag in the page's header
     *
     * @return string model that provides a description for the page, null for
     * no description
     */
    public IModel<String> getPageDescriptionModel() {
        return new StringResourceModel("vloDescription", (IModel) null).setParameters((Object[]) null);
    }

    /**
     *
     * @return URL to include as a canonical HREF in the page header (null to
     * omit such a reference)
     */
    public IModel<String> getCanonicalUrlModel() {
        return null;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        //render jQuery first, it is the most common dependency
        response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference(), true));
        // Include other JavaScript for header (e.g. permalink animation)
        response.render(JavaScriptHeaderItem.forReference(javaScriptResources.getVloHeaderJS(), true));
        response.render(JavaScriptHeaderItem.forReference(javaScriptResources.getHistoryApiJS(), true));
        response.render(JavaScriptHeaderItem.forReference(javaScriptResources.getClipBoardJS(), true));
        response.render(JavaScriptHeaderItem.forReference(javaScriptResources.getVloClipboardJS(), true));

        if (!Strings.isEmpty(vloConfig.getVcrSubmitEndpoint())) {
            response.render(JavaScriptHeaderItem.forReference(javaScriptResources.getVcrPluginJS(), true));
            response.render(JavaScriptHeaderItem.forReference(javaScriptResources.getVcrPluginConfigJS(), true));
        }

        if (bottomSnippet != null) {
            response.render(JavaScriptHeaderItem.forScript(bottomSnippet, "bottomSnippet"));
        }

        response.render(JavaScriptHeaderItem.forScript("$(document).ready(function() { $('body').removeClass('non-js'); });", "js-only-elements"));
    }

    private void addComponents() {

        appTitleModel = new NullFallbackModel<>(new EnvironmentVariableModel(VLO_APPLICATION_TITLE_ENV_VAR), DEFAULT_APP_TITLE);
        pageTitleModel = new NullFallbackModel<>(new EnvironmentVariableModel(VLO_PAGE_TITLE_ENV_VAR), new StringResourceModel("pageTitle.default", this));
        final IModel<String> instanceInfoModel = new NullFallbackModel<>(new EnvironmentVariableModel(VLO_INSTANCE_INFO_ENV_VAR), "Unnamed application instance");

        add(new BootstrapFeedbackPanel("feedback"));

        add(new WebMarkupContainer("header")
                // navbar in header
                .add(createHeaderMenu("menu"))
                // add 'class' attribute to header indicating version qualifier (e.g. 'beta')
                .add(new AttributeAppender("class", VloWicketApplication.get().getAppVersionQualifier(), " ")));

        add(new Label("instanceInfo", instanceInfoModel).add(new InvisibleIfNullBehaviour(instanceInfoModel)));

        add(new HideJavascriptFallbackControlsBehavior());

        // add Piwik tracker (if enabled)
        if (piwikConfig.isEnabled()) {
            add(new PiwikTracker("piwik", piwikConfig.getSiteId(), piwikConfig.getPiwikHost(), piwikConfig.getDomains()));
        } else {
            //empty placeholder
            add(new WebMarkupContainer("piwik"));
        }
    }

    private Component createHeaderMenu(String id) {
        final Navbar navbar = new Navbar(id) {
            @Override
            protected Label newBrandLabel(String markupId) {
                //set label to not escape model strings to allow HTML
                return (Label) super.newBrandLabel(markupId).setEscapeModelStrings(false);
            }

        };
        navbar.setBrandName(new StringResourceModel("vloMenuTitle", this).setModel(appTitleModel));

        // link to CLARIN website
        final Component clarinLink = new NavbarExternalLink(new StringResourceModel("vloNavigation.projectPageUrl", this)) {
            @Override
            protected Component newLabel(String markupId) {
                return super.newLabel(markupId).setEscapeModelStrings(false);
            }

        }
                .setLabel(Model.of("<span>CLARIN</span>"))
                .add(new AttributeModifier("class", "clarin-logo hidden-xs"));

        //add all menu compoennts
        navbar.addComponents(
                new ImmutableNavbarComponent(new NavbarButton(FacetedSearchPage.class, new StringResourceModel("vloNavigation.search", this))
                        .add(new AttributeModifier("class", "search-link")), ComponentPosition.LEFT),
                new ImmutableNavbarComponent(new NavbarButton(ContributorsPage.class, new StringResourceModel("vloNavigation.contributors", this))
                        .add(new AttributeModifier("class", "contributors-link")), ComponentPosition.LEFT),
                new ImmutableNavbarComponent(new NavbarButton(HelpPage.class, new StringResourceModel("vloNavigation.help", this))
                        .add(new AttributeModifier("class", "help-link")), ComponentPosition.LEFT),
                new ImmutableNavbarComponent(clarinLink, ComponentPosition.RIGHT)
        );
        return navbar;
    }

    public final VloConfig getVloConfig() {
        return vloConfig;
    }

}
