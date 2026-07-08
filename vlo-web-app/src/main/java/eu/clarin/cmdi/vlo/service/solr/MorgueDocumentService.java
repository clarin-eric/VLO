/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.vlo.service.solr;

import java.util.Optional;
import org.apache.solr.common.SolrDocument;

/**
 * Provides read access to the "morgue" index, which holds minimal information
 * about records that were removed from the main index. Used to render tombstone
 * pages for records that can no longer be found.
 */
public interface MorgueDocumentService {

    /**
     * Looks up a removed record by its identifier.
     *
     * @param docId record identifier
     * @return the morgue document if present, otherwise an empty optional (also
     * empty when the morgue is not configured)
     */
    Optional<SolrDocument> getById(String docId);

}
