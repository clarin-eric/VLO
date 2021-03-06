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

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.PIDUtils;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.pojo.ResourceInfo;

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
            if (resolveAgainst == null || !resolveAgainst.toLowerCase().startsWith("http") || PIDUtils.isPid(resolveAgainst)) {
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

    /**
     * Creates a resolving link model that takes the href out of a resource info
     * model and wraps it in a {@link PIDLinkModel} so that 'hdl' links and
     * relative URLs are resolved to actionable links (if possible)
     *
     * @param resourceInfoModel model for resource to create link for
     * @param documentModel model of document to resolve any relative links to
     * @return
     */
    public static ResolvingLinkModel modelFor(IModel<ResourceInfo>  resourceInfoModel, IModel<SolrDocument> documentModel) {
        return new ResolvingLinkModel(
                //URI to resolve against
                new SolrFieldStringModel(documentModel, VloWicketApplication.get().getFieldNameService().getFieldName(FieldKey.SELF_LINK)),
                //URI of link to resolve (potentially)
                new PIDLinkModel(new PropertyModel<>(resourceInfoModel, "href")));
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
