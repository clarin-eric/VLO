/*
 * Copyright (C) 2020 CLARIN
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
package eu.clarin.cmdi.vlo.config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class DataSetStructuredData {

    private boolean enabled;

    private List<DataSetStructuredDataFilter> include = new ArrayList<>();

    private List<DataSetStructuredDataFilter> exclude = new ArrayList<>();

    public DataSetStructuredData() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<DataSetStructuredDataFilter> getInclude() {
        return include;
    }

    public void setInclude(List<DataSetStructuredDataFilter> include) {
        this.include = include;
    }

    public List<DataSetStructuredDataFilter> getExclude() {
        return exclude;
    }

    public void setExclude(List<DataSetStructuredDataFilter> exclude) {
        this.exclude = exclude;
    }

}
