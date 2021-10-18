/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.api.processing;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.Optional;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public interface MappingResultStore<T> {

    void storeResult(T identifier, VloRecord record);
    
    Optional<VloRecord> getMappingResult(T identifier);

}
