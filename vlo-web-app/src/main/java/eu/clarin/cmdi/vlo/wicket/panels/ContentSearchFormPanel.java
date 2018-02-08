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

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 * A panel with an HTML form (not a Wicket form!) with a submit link that posts
 * a CQL endpoint and document ID to the configured (see null {@link VloConfig#getFederatedContentSearchUrl()
 * }) federated content search endpoint so that the user can perform a free text
 * search in its user interface.
 *
 * @author twagoo
 */
public class ContentSearchFormPanel extends GenericPanel<String> {

    private final static Logger logger = LoggerFactory.getLogger(ContentSearchFormPanel.class);

    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private FieldNameService fieldNameService;

    public ContentSearchFormPanel(String id, final IModel<SolrDocument> documentModel, final IModel<String> endpointModel) {
        super(id);

        // Create a model that returns a JSON representation of the endpoint and document id
        final IModel<String> jsonModel = createJsonModel(documentModel, endpointModel);
        setModel(jsonModel);

        // Populate attributes in form elements...
        // Outer <form> element
        final WebMarkupContainer fcsForm = new WebMarkupContainer("fcsForm");
        // The action of the form should be the aggregator endpoint
        fcsForm.add(new AttributeModifier("action", vloConfig.getFederatedContentSearchUrl()));
        add(fcsForm);

        // Hidden form field for aggregation context
        final WebMarkupContainer aggregationContext = new WebMarkupContainer("aggregationContent");
        // The value should be the JSON object
        aggregationContext.add(new AttributeModifier("value", jsonModel));
        fcsForm.add(aggregationContext);
    }

    private IModel<String> createJsonModel(final IModel<SolrDocument> model, final IModel<String> endpointModel) {
        // Prepare a JSON object that holds the CQL endpoint and the document self link
        return new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                final String endPoint = endpointModel.getObject();
                final Object selfLink = model.getObject().getFirstValue(fieldNameService.getFieldName(FieldKey.SELF_LINK));
                try {
                    final JSONObject json = new JSONObject();
                    json.put(endPoint, new JSONArray(new Object[]{selfLink}));
                    return json.toString(2);
                } catch (JSONException ex) {
                    logger.warn("Could not create JSON for aggregation context with endpoint '{}' and docId '{}'", endPoint, selfLink, ex);
                    return null;
                }
            }

            @Override
            public void detach() {
                super.detach();
                endpointModel.detach();
            }

        };
    }

}
