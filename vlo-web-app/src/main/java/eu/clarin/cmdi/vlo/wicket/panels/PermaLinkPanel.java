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
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class PermaLinkPanel extends GenericPanel<QueryFacetsSelection> {

    @SpringBean
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;

    private final IModel<SolrDocument> documentModel;
    private final Model<String> linkModel;

    public PermaLinkPanel(String id, final IModel<QueryFacetsSelection> selectionmodel) {
        this(id, selectionmodel, null);
    }

    public PermaLinkPanel(String id, final IModel<QueryFacetsSelection> selectionmodel, final IModel<SolrDocument> documentModel) {
        super(id, selectionmodel);
        this.documentModel = documentModel;
        this.linkModel = new Model<String>();
        add(new AjaxFallbackLink("linkrequest") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                final String link = linkModel.getObject();
                if (link == null) {
                    // create link and set
                    final PageParameters params = paramsConverter.toParameters(selectionmodel.getObject());
                    if (documentModel != null) {
                        params.add("docId", documentModel.getObject().getFirstValue(FacetConstants.FIELD_ID));
                    }
                    final CharSequence url = urlFor(getPage().getClass(), params);
                    final String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
                    linkModel.setObject(absoluteUrl);
                } else {
                    linkModel.setObject(null);
                }

                onChange(target);
            }
        });
        add(new TextField<String>("linkfield", linkModel) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(linkModel.getObject() != null);
            }

        });
    }

    @Override
    public void detachModels() {
        super.detachModels();
        if (documentModel != null) {
            documentModel.detach();
        }
    }

    protected void onChange(AjaxRequestTarget target) {
        if (target != null) {
            target.add(getPage());
        }
    }

}
