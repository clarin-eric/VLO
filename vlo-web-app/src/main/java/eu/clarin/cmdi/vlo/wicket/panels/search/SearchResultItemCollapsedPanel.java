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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class SearchResultItemCollapsedPanel extends Panel {
    @SpringBean
    private FieldNameService fieldNameService;
    private static final int MAX_DESCRIPTION_LENGTH = 350;
    private static final int LONG_DESCRIPTION_TRUNCATE_POINT = 320;
    private static final int MAX_DESCRIPTION_LENGTH_SHORTER = 150;
    private static final int LONG_DESCRIPTION_TRUNCATE_POINT_SHORTER = 120;

    private final IModel<SearchContext> selectionModel;

    public SearchResultItemCollapsedPanel(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel, Ordering<String> availabilityOrdering) {
        super(id, documentModel);
        this.selectionModel = selectionModel;

        // description, truncated if too long
        add(new SolrFieldLabel("description", documentModel, fieldNameService.getFieldName(FieldKey.DESCRIPTION), "", MAX_DESCRIPTION_LENGTH, LONG_DESCRIPTION_TRUNCATE_POINT));
        // extra short description for smaller devices
        add(new SolrFieldLabel("description-shorter", documentModel, fieldNameService.getFieldName(FieldKey.DESCRIPTION), "", MAX_DESCRIPTION_LENGTH_SHORTER, LONG_DESCRIPTION_TRUNCATE_POINT_SHORTER));
    }

    @Override
    public void detachModels() {
        super.detachModels();
        // not passed to super
        selectionModel.detach();
    }

}
