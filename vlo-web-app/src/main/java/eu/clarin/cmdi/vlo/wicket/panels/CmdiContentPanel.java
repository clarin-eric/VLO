/*
 * Copyright (C) 2016 CLARIN
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
import eu.clarin.cmdi.vlo.wicket.model.CompoundListModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.model.UrlFromStringModel;
import eu.clarin.cmdi.vlo.wicket.model.XsltModel;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class CmdiContentPanel extends GenericPanel<SolrDocument> {
    @SpringBean
    private FieldNameService fieldNameService;

    private final CompoundListModel cmdiUrlsModel;

    public CmdiContentPanel(String id, IModel<SolrDocument> model) {
        super(id, model);

        final List<IModel<URL>> locationModels = Arrays.<IModel<URL>>asList(
                //local (harvested) copy of the record
                new UrlFromStringModel(new SolrFieldStringModel(model, fieldNameService.getFieldName(FieldKey.FILENAME))),
                //self link as fallback
                new UrlFromStringModel(new HandleLinkModel(new SolrFieldStringModel(model, fieldNameService.getFieldName(FieldKey.SELF_LINK)))));

        this.cmdiUrlsModel = new CompoundListModel(locationModels);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("content",
                new XsltModel(cmdiUrlsModel))
                .setEscapeModelStrings(false));
    }

    @Override
    public void detachModels() {
        super.detachModels();
        cmdiUrlsModel.detach();
    }

}
