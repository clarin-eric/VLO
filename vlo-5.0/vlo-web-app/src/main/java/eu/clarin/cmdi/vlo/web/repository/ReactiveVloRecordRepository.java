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
package eu.clarin.cmdi.vlo.web.repository;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Repository
public class ReactiveVloRecordRepository implements VloRecordRepository {

    @Override
    public Flux<VloRecord> findAll() {
        return Flux.fromIterable(ImmutableList.of(
                VloRecord.builder()
                        .id("record1")
                        .selflink("https://records/record1")
                        .build(),
                VloRecord.builder()
                        .id("record2")
                        .selflink("https://records/record2")
                        .build()
        ));
    }

}
