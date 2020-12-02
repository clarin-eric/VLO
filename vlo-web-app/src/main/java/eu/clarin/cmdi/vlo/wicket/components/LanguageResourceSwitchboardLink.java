/*
 * Copyright (C) 2017 CLARIN
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

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Optional;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class LanguageResourceSwitchboardLink extends AjaxFallbackLink<String> {

    private final static boolean POPUP_ENABLED = true;

    private final static Logger logger = LoggerFactory.getLogger(LanguageResourceSwitchboardLink.class);

    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private LanguageCodeUtils languageCodeUtils;

    private final IModel<String> linkModel;
    private final IModel<ResourceInfo> resourceInfoModel;
    private final IModel<Collection<Object>> languagesModel;

    public LanguageResourceSwitchboardLink(String id, IModel<String> linkModel, IModel<Collection<Object>> languagesModel, IModel<ResourceInfo> resourceInfoModel) {
        super(id);
        this.linkModel = linkModel;
        this.resourceInfoModel = resourceInfoModel;
        this.languagesModel = languagesModel;

        if (POPUP_ENABLED) {
            add(new AttributeModifier("onclick", () -> {
                final CharSequence alignId = LanguageResourceSwitchboardLink.this.getMarkupId();
                final CharSequence resourceLink = JavaScriptUtils.escapeQuotes(resourceInfoModel.getObject().getHref());
                return String.format(
                        "var dropDownId='#' + $('#%s').parents('.dropdown').attr('id');"
                        + "showSwitchboardPopup({'alignSelector': dropDownId, 'alignRight': true}, {'url': '%s'});"
                        + "return false;",
                        alignId,
                        resourceLink);
            }));
        }

        setOutputMarkupId(true);
    }

    @Override
    public void onClick(Optional<AjaxRequestTarget> target) {
        if (!target.isPresent() || !POPUP_ENABLED) {
            //redirect browser to Switchboard website
            throw new RedirectToUrlException(getLanguageSwitchboardUrl(linkModel, resourceInfoModel.getObject()));
        }
    }

    private String getLanguageSwitchboardUrl(IModel<String> linkModel, ResourceInfo resourceInfo) {
        try {
            //create link for this resource to the language resource switchboard
            final String href = linkModel.getObject();
            final String mimeType = resourceInfo.getMimeType();
            final String languageCode = getResourceLanguageCode();
            return String.format("%s#/vlo/%s/%s/%s",
                    vloConfig.getLrSwitchboardBaseUrl(),
                    URLEncoder.encode(href, "UTF-8"),
                    URLEncoder.encode(mimeType, "UTF-8"), languageCode);
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error while creating switchboard link", ex);
            return null;
        }
    }

    private String getResourceLanguageCode() {
        final Collection<Object> languageValues = languagesModel.getObject();
        if (languageValues != null && languageValues.size() == 1) {
            //if not exactly one language, so cannot be determined for this resource
            final String languageFieldValue = languageValues.iterator().next().toString();
            final LanguageCodeUtils.LanguageInfo languageInfo = languageCodeUtils.decodeLanguageCodeString(languageFieldValue);
            if (languageInfo.getType() == LanguageCodeUtils.LanguageInfo.Type.CODE) {
                //LRS only accepts language codes
                return languageInfo.getValue().toLowerCase();
            }
        }
        //all other cases: no info
        return "";
    }

    @Override
    public void detachModels() {
        super.detachModels();
        linkModel.detach();
        resourceInfoModel.detach();
        languagesModel.detach();
    }

}
