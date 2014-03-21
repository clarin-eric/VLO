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
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel with a link to toggle a text input which shows a bookmarkable link to
 * the current page with parameters representing the current model
 *
 * @author twagoo
 */
public class PermaLinkPanel extends GenericPanel<QueryFacetsSelection> {

    @SpringBean
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;

    private final IModel<SolrDocument> documentModel;
    private final Model<Boolean> linkVisibilityModel;

    public PermaLinkPanel(String id, final IModel<QueryFacetsSelection> selectionmodel) {
        this(id, selectionmodel, null);
    }

    public PermaLinkPanel(String id, final IModel<QueryFacetsSelection> selectionmodel, final IModel<SolrDocument> documentModel) {
        super(id, selectionmodel);
        this.documentModel = documentModel;
        this.linkVisibilityModel = new Model<Boolean>(false);

        // create a model that provides a link to the current page
        final IModel<String> linkModel = new PermaLinkModel(selectionmodel, documentModel);

        // action to link to request the permalink
        add(new IndicatingAjaxFallbackLink<String>("linkrequest", linkModel) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                // toggle
                linkVisibilityModel.setObject(!linkVisibilityModel.getObject());

                if (target != null && linkVisibilityModel.getObject()) {
                    target.appendJavaScript("permalinkShown();");
                }

                // callback to react to change
                onChange(target);
            }
        });

        // field that holds the actual link
        final TextField<String> linkField = new TextField<String>("linkfield", linkModel) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(linkVisibilityModel.getObject());
            }

        };

        add(linkField);
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

    private class PermaLinkModel extends AbstractReadOnlyModel<String> {

        private final IModel<QueryFacetsSelection> selectionmodel;
        private final IModel<SolrDocument> documentModel;

        public PermaLinkModel(IModel<QueryFacetsSelection> selectionmodel, IModel<SolrDocument> documentModel) {
            this.selectionmodel = selectionmodel;
            this.documentModel = documentModel;
        }

        @Override
        public String getObject() {
            final PageParameters params = paramsConverter.toParameters(selectionmodel.getObject());
            if (documentModel != null) {
                params.add("docId", documentModel.getObject().getFirstValue(FacetConstants.FIELD_ID));
            }
            final CharSequence url = urlFor(getPage().getClass(), params);
            final String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
            return absoluteUrl;
        }
    }

}
