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
package eu.clarin.cmdi.vlo.wicket.model;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.Application;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * Model that uses a {@link StringResourceModel} to produce a human friendly
 * and/or localised name instead of the internal field name. This depends on the
 * presence of a globally registered resource bundle that contains a property
 * "field.{fieldname}".
 *
 * @author twagoo
 * @see Application#getResourceSettings()
 * @see ResourceSettings#getStringResourceLoaders()
 */
public class SolrFieldNameModel extends StringResourceModel {
    
    public SolrFieldNameModel(IModel<FacetField> fieldModel, String fieldName){
        this(new PropertyModel<String>(fieldModel, fieldName));
    }
    
    public SolrFieldNameModel(IModel<String> model) {
        super("field.${}", model);
        setDefaultValue(Model.of(model.getObject())); //TODO: check if this is necessary, added on migration to Wicket 7
    }

}
