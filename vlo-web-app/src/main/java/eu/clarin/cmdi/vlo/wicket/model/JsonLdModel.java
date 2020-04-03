/*
 * Copyright (C) 2020 CLARIN
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

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class JsonLdModel implements IModel<String> {

    private final static GsonBuilder GSON_BUILDER = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting();

    private final IModel<JsonLdObject> jsonLdObjectModel;

    public JsonLdModel(IModel<JsonLdObject> jsonLdObjectModel) {
        this.jsonLdObjectModel = jsonLdObjectModel;
    }
    
    @Override
    public String getObject() {
        return GSON_BUILDER.create().toJson(jsonLdObjectModel.getObject());
    }
    
    @Override
    public void detach() {
        jsonLdObjectModel.detach();
    }

    public static class JsonLdObject implements Serializable {

        @SerializedName("@context")
        private final String context;

        @SerializedName("@type")
        private final String type;

        public JsonLdObject(String context, String type) {
            this.context = context;
            this.type = type;
        }

        public String getContext() {
            return context;
        }

        public String getType() {
            return type;
        }

    }

}
