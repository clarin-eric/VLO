/*
 * Copyright (C) 2022 CLARIN
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
package eu.clarin.cmdi.vlo.importer.linkcheck;

import java.time.LocalDateTime;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class BasicLinkStatus implements LinkStatus {

    private final String url;
    private final Integer status;
    private final LocalDateTime checkingDate;
    private final String contentType;

    public BasicLinkStatus(String url, Integer status, LocalDateTime checkingDate, String contentType) {
        this.url = url;
        this.status = status;
        this.checkingDate = checkingDate;
        this.contentType = contentType;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public LocalDateTime getCheckingDate() {
        return checkingDate;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

}
