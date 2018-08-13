/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo.importer.normalizer;

import org.slf4j.LoggerFactory;

import java.util.List;

import org.slf4j.Logger;


import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

/**
 *
 * @author twagoo
 */
public abstract class AbstractPostNormalizer{

    private final VloConfig config;
    
    protected final Logger LOG; 
    
    public AbstractPostNormalizer() {
    	this.config = null;
    	this.LOG = LoggerFactory.getLogger(this.getClass());
    }

    public AbstractPostNormalizer(VloConfig config) {
        this.config = config;
        this.LOG = LoggerFactory.getLogger(this.getClass());
    }
    
    /**
    *
    * @param value value to post-process; can be null
    * @param docFieldContainer processing context, can be null or incomplete
    * @return list of post-processed values
    */
   public abstract List<String> process(String value, DocFieldContainer docFieldContainer);

   /**
    *
    * @return whether the postprocessor should also be called in case no
    * matching value was found (with <pre>value == null</pre>)
    */
   public abstract boolean doesProcessNoValue();

    public VloConfig getConfig() {
        return config;
    }
}
