/*
 * Copyright (C) 2015 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.components;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.FieldNameService;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class NamedRecordPageLink extends GenericPanel<SolrDocument> {
    @SpringBean
    private FieldNameService fieldNameService;
    

    public NamedRecordPageLink(String id, IModel<SolrDocument> model) {
        this(id, model, null);
    }

    public NamedRecordPageLink(String id, IModel<SolrDocument> model, String initialTab) {
        super(id, model);
        final RecordPageLink link = new RecordPageLink("link", model, null, initialTab);
        link.add(new SingleValueSolrFieldLabel(
                "name", 
                model, 
                fieldNameService.getFieldName(FieldKey.NAME),
                new StringResourceModel("recordpage.unnamedrecord", this, null)));
        add(link);
    }

}
