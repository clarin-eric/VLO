/*
 * Copyright (C) 2022 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.model.ActionableLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.model.TruncatingStringModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class AddToVcrQueueButtonBehavior extends Behavior {

    private final IModel<SolrDocument> documentModel;

    public AddToVcrQueueButtonBehavior(IModel<SolrDocument> documentModel) {
        this.documentModel = documentModel;
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        final VloConfig config = VloWicketApplication.get().getVloConfig();
        final boolean vcrSubmissionEnabled = !Strings.isEmpty(config.getVcrSubmitEndpoint());
        final FieldNameService fieldNameService = VloWicketApplication.get().getFieldNameService();
        component.add(new AttributeModifier("data-vcr-uri", new NullFallbackModel(
                new ActionableLinkModel(new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(FieldKey.SELF_LINK))),
                new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(FieldKey.COMPLETE_METADATA)))))
                .add(new AttributeModifier("data-vcr-label", new NullFallbackModel(
                        new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(FieldKey.NAME)),
                        new StringResourceModel("searchpage.unnamedrecord", component))))
                .add(new AttributeModifier("data-vcr-description", new TruncatingStringModel(
                        new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(FieldKey.DESCRIPTION)),
                        1000,
                        990)))
                .add(BooleanVisibilityBehavior.visibleOnTrue(() -> vcrSubmissionEnabled));
    }

    @Override
    public void detach(Component component) {
        documentModel.detach();
    }

}
