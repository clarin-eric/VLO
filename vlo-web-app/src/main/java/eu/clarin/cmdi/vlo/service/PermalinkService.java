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
package eu.clarin.cmdi.vlo.service;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Page;

/**
 * Interface for a service that produces a URL given a page class and optionally
 * a facet selection and document object
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public interface PermalinkService {

    String getUrlString(Class<? extends Page> pageClass, QueryFacetsSelection selection, SolrDocument document);

}
