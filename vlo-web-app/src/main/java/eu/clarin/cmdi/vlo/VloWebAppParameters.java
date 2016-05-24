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
package eu.clarin.cmdi.vlo;

/**
 *
 * @author twagoo
 */
public class VloWebAppParameters {

    public final static String THEME = "theme";
    public final static String DOCUMENT_ID = "docId";
    public static final String QUERY = "q";
    public static final String FILTER_QUERY = "fq";
    public static final String FILTER_QUERY_TYPE = "fqType";
    public static final String SEARCH_INDEX = "index";
    public static final String SEARCH_COUNT = "count";
    
    /**
     * Optional page parameter that determines the initial tab
     */
    public static final String RECORD_PAGE_TAB = "tab";
}
