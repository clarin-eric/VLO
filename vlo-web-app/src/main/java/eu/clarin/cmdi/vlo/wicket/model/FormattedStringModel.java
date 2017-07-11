/*
 * Copyright (C) 2016 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.model;

import org.apache.wicket.Session;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * Read-only model that returns a formatted string on basis of a value model and
 * a model that provides a formatting string
 *
 * @see String#format(java.lang.String, java.lang.Object...)
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class FormattedStringModel extends AbstractReadOnlyModel<String> {

    private final IModel<String> valueModel;
    private final IModel<String> formattingModel;

    /**
     *
     * @param formattingModel model that provides the formatting string
     * @param valueModel model that provides the value passed as an argument to
     * the formatting string
     */
    public FormattedStringModel(IModel<String> formattingModel, IModel<String> valueModel) {
        this.valueModel = valueModel;
        this.formattingModel = formattingModel;
    }

    @Override
    public String getObject() {
        final String value = valueModel.getObject();
        if (value == null) {
            return null;
        } else {
            return String.format(Session.get().getLocale(), formattingModel.getObject(), value);
        }
    }

    @Override
    public void detach() {
        valueModel.detach();
        formattingModel.detach();
    }

}
