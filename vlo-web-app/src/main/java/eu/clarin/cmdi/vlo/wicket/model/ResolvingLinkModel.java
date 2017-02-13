/*
 * Copyright (C) 2017 CLARIN
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

import static eu.clarin.cmdi.vlo.FacetConstants.HANDLE_PROXY;
import static eu.clarin.cmdi.vlo.FacetConstants.HANDLE_PROXY_HTTPS;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.wicket.model.IModel;

/**
 * Model that resolves a URI string (object) against another URI string
 * (subject). Will return 'null' as an object if resolving is impossible, for
 * example if the resolve subject URI is invalid or a handle URI.
 *
 * @author twagoo
 */
public class ResolvingLinkModel implements IModel<String> {

    private final IModel<String> resolveObjectModel;
    private final IModel<String> resolveSubjectModel;

    /**
     *
     * @param resolveSubjectModel model of URL string to resolve against
     * @param resolveObjectModel model of URL string to resolve
     */
    public ResolvingLinkModel(IModel<String> resolveSubjectModel, IModel<String> resolveObjectModel) {
        this.resolveSubjectModel = resolveSubjectModel;
        this.resolveObjectModel = resolveObjectModel;
    }

    @Override
    public String getObject() {
        final String toResolve = resolveObjectModel.getObject();
        final String toResolveLower = (toResolve == null) ? null : toResolve.toLowerCase();
        if (toResolveLower == null || toResolveLower.startsWith("http") || toResolveLower.startsWith("https")) {
            //no need to resolve
            return toResolve;
        } else {
            //some resolving is desirable...
            final String resolveAgainst = resolveSubjectModel.getObject();
            final String resolveAgainstLower = (resolveAgainst == null) ? null : resolveAgainst.toLowerCase();
            if (resolveAgainstLower == null
                    || !resolveAgainstLower.startsWith("http")
                    || resolveAgainstLower.startsWith(HANDLE_PROXY)
                    || resolveAgainstLower.startsWith(HANDLE_PROXY_HTTPS)) {
                //can only resolve against http(s) URI (excluding handle proxy)
                return null;
            } else {
                try {
                    final URI resolveAgainstURI = new URI(resolveAgainst);
                    if (!resolveAgainstURI.isAbsolute()) {
                        //only resolve against an absolute URI
                        return null;
                    } else {
                        return resolveAgainstURI
                                .resolve(toResolve)
                                .toString();
                    }
                } catch (IllegalArgumentException | URISyntaxException ex) {
                    //one of the URIs is invalid
                    return null;
                }
            }
        }
    }

    @Override
    public void setObject(String object) {
        resolveObjectModel.setObject(object);
    }

    @Override
    public void detach() {
        resolveSubjectModel.detach();
        resolveObjectModel.detach();
    }

}
