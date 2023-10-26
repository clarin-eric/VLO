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
package eu.clarin.cmdi.vlo.wicket.model;

import eu.clarin.cmdi.vlo.ResourceInfo;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ResourceInfoObjectModel extends LoadableDetachableModel<ResourceInfo> {

    private final IModel<String> resourceInfoStringModel;
    private Jsonb jsonb;

    public ResourceInfoObjectModel(IModel<SolrDocument> documentModel, String fieldName) {
        this(new SolrFieldStringModel(documentModel, fieldName));
    }

    public ResourceInfoObjectModel(IModel<String> resourceInfoStringModel) {
        this.resourceInfoStringModel = resourceInfoStringModel;

    }

    @Override
    public void setObject(ResourceInfo t) {
        resourceInfoStringModel.setObject(t.toJson(jsonb));
        //require reload
        this.detach();
    }

    @Override
    protected ResourceInfo load() {
        jsonb = JsonbBuilder.create();
        return ResourceInfo.fromJson(jsonb, resourceInfoStringModel.getObject());
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        resourceInfoStringModel.detach();
        if (jsonb != null) {
            try {
                jsonb.close();
            } catch (Exception ex) {
                //not closing jsonb is acceptable
            }
            jsonb = null;
        }
    }

}
