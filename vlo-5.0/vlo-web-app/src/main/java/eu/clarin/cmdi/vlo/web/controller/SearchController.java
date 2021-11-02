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
package eu.clarin.cmdi.vlo.web.controller;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.web.repository.VloRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Controller
public class SearchController {

    private final VloRecordRepository recordRepository;

    public SearchController(VloRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @RequestMapping("/")
    public String index(final Model model) {
        final Flux<VloRecord> recordsFlux
                = recordRepository.findAll(PageRequest.of(0, 10));
        
        final IReactiveDataDriverContextVariable reactiveDataDrivenMode
                = new ReactiveDataDriverContextVariable(recordsFlux, 2);

        model.addAttribute("records", reactiveDataDrivenMode);

        return "index";
    }

}
