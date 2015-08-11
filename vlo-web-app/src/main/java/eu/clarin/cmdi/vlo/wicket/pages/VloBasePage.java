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

import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.HideJavascriptFallbackControlsBehavior;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
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
    public final static String DEFAULT_PAGE_TITLE = "CLARIN VLO";

    @SpringBean
    private VloConfig vloConfig;

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
    }

    /**
     * Override to give the page a custom or dynamic page title
     *
     * @return string model that provides the page title
     */
    public IModel<String> getTitleModel() {
        return Model.of(DEFAULT_PAGE_TITLE);
    }

    /**
     * Override to give a custom or dynamic description for the page via the
     * description "meta" tag in the page's header
     *
     * @return string model that provides a description for the page, null for
     * no description
     */
    public IModel<String> getPageDescriptionModel() {
        return StringResourceModelMigration.of("vloDescription", null, (Object[]) null);
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
        // Include CSS. Exact file will be chosen on basis of current locale and style (theme)
        response.render(CssHeaderItem.forReference(new CssResourceReference(VloBasePage.class, "vlo.css", getLocale(), getStyle(), getVariation())));
        // Include JavaScript for header (e.g. permalink animation)
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getVloHeaderJS()));
    }

    private void addComponents() {
        add(new FeedbackPanel("feedback"));
        add(new ExternalLink("help", vloConfig.getHelpUrl()));

        // add 'class' attribute to header indicating version qualifier (e.g. 'beta')
        add(new WebMarkupContainer("header").add(new AttributeAppender("class", VloWicketApplication.get().getAppVersionQualifier())));

        add(new HideJavascriptFallbackControlsBehavior());
    }

}
