/*
 * Copyright (C) 2015 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.components;


import eu.clarin.cmdi.vlo.config.VloConfig;
import static eu.clarin.cmdi.vlo.config.VloConfig.LANGUAGE_LINK_TEMPLATE_LANGUAGE_CODE_PLACEHOLDER;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel that has a link and a label for displaying language names with a link
 * to the language information page. If the value model object matches the
 * 'language code' syntax ("<code>code:xxx</code>"), it will show a link to the
 * configured language information page, wrapping around a
 * {@link FieldValueLabel} to show the language name. In other cases, it falls
 * back to a plain FieldValueLabel without a link.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 * @see FacetConstants#FIELD_LANGUAGE_CODE
 * @see VloConfig#getLanguageLinkTemplate()
 */
public class LanguageInfoLink extends GenericPanel<String> {

    @SpringBean
    private VloConfig vloConfig;

    private final static Pattern LANGUAGE_CODE_REGEX = Pattern.compile("code:(.*)");
    private final MarkupContainer link;
    private final FieldValueLabel fallbackLabel;

    public LanguageInfoLink(String id, IModel<String> originalValueModel, IModel<String> facetNameModel) {
        super(id, originalValueModel);

        // link to language info page
        link = new WebMarkupContainer("link");
        // make it an external link to the language info page
        link.add(new AttributeModifier("href", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                final String languageCode = getLanguageCode();
                if (languageCode == null) {
                    return null;
                } else {
                    return vloConfig.getLanguageLinkTemplate().replace(LANGUAGE_LINK_TEMPLATE_LANGUAGE_CODE_PLACEHOLDER, languageCode);
                }
            }
        }));
        
        // normal field value label inside (which should convert the language code to a name)
        link.add(new FieldValueLabel("label", originalValueModel, facetNameModel));
        add(link);

        // add a standalone label to be shown when the link is not available
        fallbackLabel = new FieldValueLabel("label", originalValueModel, facetNameModel);
        add(fallbackLabel);
    }

    @Override
    protected void onConfigure() {
        // when a link can be generated (i.e. wehen a language code is 
        // available) show the link; otherwise show the fallback label
        final boolean linkAvailable = newLanguageCodeMatcher().matches();
        link.setVisible(linkAvailable);
        fallbackLabel.setVisible(!linkAvailable);
    }

    private String getLanguageCode() {
        final Matcher matcher = newLanguageCodeMatcher();
        if (matcher.matches() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private Matcher newLanguageCodeMatcher() {
        return LANGUAGE_CODE_REGEX.matcher(getModelObject());
    }
}
