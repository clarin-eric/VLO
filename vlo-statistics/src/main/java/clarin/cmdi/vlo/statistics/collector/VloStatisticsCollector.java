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
package clarin.cmdi.vlo.statistics.collector;

import java.io.IOException;
import clarin.cmdi.vlo.statistics.model.VloReport;
import eu.clarin.cmdi.vlo.config.VloConfig;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public interface VloStatisticsCollector {

    void collect(VloReport report, VloConfig config, SolrClient solrClient) throws SolrServerException, IOException;
}
