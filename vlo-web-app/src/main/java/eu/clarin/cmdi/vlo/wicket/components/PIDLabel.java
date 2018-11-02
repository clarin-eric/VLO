/*
 * Copyright (C) 2018 CLARIN
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

import eu.clarin.cmdi.vlo.PIDUtils;
import java.util.Locale;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PIDLabel extends GenericPanel<String> {

    private final IConverter<String> labelConverter;
    private final static IConverter<String> BADGE_CONVERTER = new PidBadgeLinkConverter();
    private boolean hideLabel = false;

    /**
     *
     * @param id component id
     * @param model link model
     */
    public PIDLabel(String id, IModel<String> model) {
        this(id, model, -1);
    }

    /**
     *
     * @param id component id
     * @param model link model
     * @param maxLinkLength maximum length before text inside label gets
     * truncated
     */
    public PIDLabel(String id, IModel<String> model, int maxLinkLength) {
        super(id, model);
        labelConverter = new PidLabelLinkConverter(maxLinkLength);
        add(new ExternalLink("link", model)
                .add(new Label("badge", model) {
                    @Override
                    public <C> IConverter<C> getConverter(Class<C> type) {
                        if (type.equals(String.class)) {
                            return (IConverter<C>) BADGE_CONVERTER;
                        } else {
                            return super.getConverter(type);
                        }
                    }

                })
                .add(new Label("label", model) {
                    @Override
                    public <C> IConverter<C> getConverter(Class<C> type) {
                        if (type.equals(String.class)) {
                            return (IConverter<C>) labelConverter;
                        } else {
                            return super.getConverter(type);
                        }
                    }

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(!hideLabel);
                    }

                })
                .add(new AttributeModifier("title", model))
        );
    }

    public PIDLabel setHideLabel(boolean hideLabel) {
        this.hideLabel = hideLabel;
        return this;
    }

    private static class PidBadgeLinkConverter implements IConverter<String> {

        @Override
        public String convertToString(String value, Locale locale) {
            if (PIDUtils.isHandle(value)) {
                return "HDL";
            } else {
                return "WWW";
            }

        }

        @Override
        public String convertToObject(String value, Locale locale) throws ConversionException {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    private static class PidLabelLinkConverter implements IConverter<String> {

        private final int maxLength;

        public PidLabelLinkConverter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public String convertToObject(String value, Locale locale) throws ConversionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String convertToString(String value, Locale locale) {
            if (value == null) {
                return null;
            } else {
                final String converted = convertToString(value);
                if (maxLength >= 0 && maxLength < converted.length()) {
                    return converted.substring(0, maxLength - 3) + "...";
                } else {
                    return converted;
                }
            }
        }

        private String convertToString(String value) {
            final String schemeSpecificId = PIDUtils.getSchemeSpecificId(value);
            if (schemeSpecificId == null) {
                return value;
            } else {
                return schemeSpecificId;
            }
        }

    }
}
