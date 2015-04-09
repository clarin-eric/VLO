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

import org.apache.wicket.extensions.markup.html.basic.DefaultLinkParser;
import org.apache.wicket.extensions.markup.html.basic.ILinkParser;
import org.apache.wicket.extensions.markup.html.basic.LinkParser;
import org.apache.wicket.extensions.markup.html.basic.SmartLinkLabel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;

import static org.apache.wicket.extensions.markup.html.basic.DefaultLinkParser.URL_RENDER_STRATEGY;

/**
 * Clone of {@link SmartLinkLabel} from wicket-extensions by Juergen Donnerstag
 * that extends FieldValueLabel so that it undergoes the same conversion
 * processing as its super class
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @see SmartLinkLabel
 */
public class SmartLinkFieldValueLabel extends FieldValueLabel {

    public SmartLinkFieldValueLabel(String id, IModel<String> fieldModel) {
        super(id, fieldModel);
    }

    public SmartLinkFieldValueLabel(String id, IModel<?> model, IModel<String> fieldModel) {
        super(id, model, fieldModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        replaceComponentTagBody(markupStream, openTag,
                getSmartLink(getDefaultModelObjectAsString()));
    }

    /**
     *
     * @return link parser
     */
    protected ILinkParser getLinkParser() {
        return new VloLinkParser();
    }

    /**
     * Replace all email and URL addresses
     *
     * @param text Text to be modified
     * @return Modified Text
     */
    protected final CharSequence getSmartLink(final CharSequence text) {
        return getLinkParser().parse(text.toString());
    }

    /**
     * Adapted version of {@link DefaultLinkParser} that does not parse e-mail
     * addresses (to prevent false classifications of handle URIs containing an
     * {@code @})
     */
    private static class VloLinkParser extends LinkParser {

        /**
         * URL pattern adapted from {@link DefaultLinkParser}; added the @ in
         * the second part of the URI for cases like
         * {@code hdl.handle.net/abc-123@format=cmdi}.
         */
        private static final String urlPattern = "([a-zA-Z]+://[\\w\\.\\-\\:\\/~]+)[\\w\\.:\\-/?&=%@]*";

        public VloLinkParser() {
            addLinkRenderStrategy(urlPattern, URL_RENDER_STRATEGY);
        }

    }

}
