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
package eu.clarin.cmdi.vlo.util;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class VloApiConstants {
    
    public final static String RECORDS_PATH = "/records";
    public final static String RECORD_MAPPING_PATH = "/recordMapping";
    public final static String RECORD_MAPPING_REQUEST_PATH = RECORD_MAPPING_PATH + "/request";
    public final static String RECORD_MAPPING_RESULT_PATH = RECORD_MAPPING_PATH + "/result";
    
    public final static String QUERY_PARAMETER = "q";
    public final static String ROWS_PARAMETER = "rows";
    public final static String START_PARAMETER = "start";
    
}
