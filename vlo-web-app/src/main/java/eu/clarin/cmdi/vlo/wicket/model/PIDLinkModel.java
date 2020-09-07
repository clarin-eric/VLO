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

import static eu.clarin.cmdi.vlo.FacetConstants.DOI_RESOLVER_URL;
import static eu.clarin.cmdi.vlo.FacetConstants.HANDLE_PROXY;
import static eu.clarin.cmdi.vlo.FacetConstants.URN_NBN_RESOLVER_URL;
import eu.clarin.cmdi.vlo.PIDUtils;
import java.util.regex.Pattern;
import org.apache.wicket.model.IModel;

/**
 * Model that takes a link from an inner model and in case of a pid , will
 * replace the scheme with the resolver URL
 *
 * @author twagoo
 * @see PIDUtils
 */
public class PIDLinkModel implements IModel<String> {


    private final IModel<String> linkModel;

    public PIDLinkModel(IModel<String> linkModel) {
        this.linkModel = linkModel;
    }

    @Override
    public String getObject() {
        return PIDUtils.getActionableLinkForPid(linkModel.getObject());
    }

    @Override
    public void setObject(String object) {
        linkModel.setObject(object);
    }

    @Override
    public void detach() {
        linkModel.detach();
    }

    /**
     *
     * @param model
     * @return model if it is a PIDLinkModel, otherwise a new PIDLinkModel that
     * wraps provided model
     */
    public final static PIDLinkModel wrapLinkModel(IModel<String> model) {
        if (model instanceof PIDLinkModel) {
            return (PIDLinkModel) model;
        } else {
            return new PIDLinkModel(model);
        }
    }
}
