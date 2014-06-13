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

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.AutoCompleteService;
import java.util.Iterator;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Form for textual query on the faceted search page
 *
 * @author twagoo
 */
public abstract class SearchForm extends Form<QueryFacetsSelection> {

    @SpringBean
    private AutoCompleteService autoCompleteDao;

    public SearchForm(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);

        // Bind search field to 'query' property of model
        add(new AutoCompleteTextField("query", new PropertyModel<String>(model, "query")) {

            @Override
            protected Iterator<String> getChoices(String input) {
                return autoCompleteDao.getChoices(input);
            }
        });

        // Button allows partial updates but can fall back to a full (non-JS) refresh
        add(new AjaxFallbackButton("searchSubmit", this) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                SearchForm.this.onSubmit(target);
            }
        }
        );
    }

    protected abstract void onSubmit(AjaxRequestTarget target);
}
