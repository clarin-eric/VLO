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

import com.google.common.base.Strings;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.web.data.SearchRequest;
import eu.clarin.cmdi.vlo.web.repository.VloRecordRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Controller
@RequestMapping("/")
@Slf4j
public class SearchController {

    private final VloRecordRepository recordRepository;

    public SearchController(VloRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @GetMapping("/")
    public ModelAndView index(final Model model) {
        return new ModelAndView("redirect:/search", model.asMap());
    }

    @RequestMapping(path = "/search", method = {RequestMethod.GET, RequestMethod.POST})
    public String indexSubmit(final Model model, @ModelAttribute("searchRequest") SearchRequest searchRequest) {
        final List<VloRecord> records = recordRepository.search(
                Optional.ofNullable(Strings.emptyToNull(searchRequest.getQuery()))
                        .orElse("*"),
                PageRequest.of(0, 10));

        model.addAttribute("records", records);
        model.addAttribute("searchRequest", searchRequest);

        return "search";
    }

}
