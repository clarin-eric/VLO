/*
 * Copyright (C) 2024 twagoo
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
package eu.clarin.cmdi.vlo.api.controller;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author twagoo
 */
@Slf4j
@ControllerAdvice(basePackageClasses = VloRecordController.class)
public class VloApiControllerAdvice {

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(VloApiProcessingException.class)
    public ResponseEntity<?> handleControllerException(HttpServletRequest request, Throwable ex) {
        log.info("Handling exception in request handling for {}", request.getRequestURI(), ex);

        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        if (ex != null) {
            problemDetail.setDetail(ex.getMessage());
            problemDetail.getProperties();
            if (ex instanceof VloApiProcessingException vloApiProcessingException) {
                final VloRecord recordContext = vloApiProcessingException.getRecordContext();
                if (recordContext != null) {
                    log.debug("Record context of exception at {}: {}", request.getRequestURI(), recordContext);
                }
                problemDetail.setProperty("recordContext", recordContext);
            }
        }

        return ResponseEntity.of(problemDetail).build();

    }

}
