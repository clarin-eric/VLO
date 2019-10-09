/*
 * Copyright (C) 2019 CLARIN
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

import java.io.Serializable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * Label which does not escape its model strings, allowing the use of HTML tags
 * @author teckart
 */
public class UnescapedLabel extends Label {

    public UnescapedLabel(String id) {
        super(id);
        setEscapeModelStrings(false);
    }

    public UnescapedLabel(String id, IModel<?> model) {
        super(id, model);
        setEscapeModelStrings(false);
    }

    public UnescapedLabel(String id, Serializable label) {
        super(id, label);
        setEscapeModelStrings(false);
    }

    public UnescapedLabel(String id, String label) {
        super(id, label);
        setEscapeModelStrings(false);
    }
}
