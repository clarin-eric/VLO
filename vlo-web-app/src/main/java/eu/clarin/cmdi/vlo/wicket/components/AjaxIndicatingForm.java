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
package eu.clarin.cmdi.vlo.wicket.components;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

/**
 * A form with an {@link AjaxIndicatorAppender} that shows an indicator while
 * waiting for an AJAX response
 *
 * @author twagoo
 */
public class AjaxIndicatingForm extends Form implements IAjaxIndicatorAware {

    private final AjaxIndicatorAppender ajaxIndicatorAppender;

    public AjaxIndicatingForm(String id) {
        super(id);
        add(ajaxIndicatorAppender = new AjaxIndicatorAppender());
    }

    public AjaxIndicatingForm(String id, IModel model) {
        super(id, model);
        add(ajaxIndicatorAppender = new AjaxIndicatorAppender());
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return ajaxIndicatorAppender.getMarkupId();
    }

}
