/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.importer;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small helper class that stores metadata provided by the CLARIN OAI-PMH harvester
 *
 * @author Thomas Eckart
 */
public class HarvesterMap {
    protected final static Logger LOG = LoggerFactory.getLogger(HarvesterMap.class);
    
    /**
     * Parses mapping file of OAI-PMH harvester and returns mapping file
     * (harvester directory name -> EndpointDescription)
     * @param mappingFile
     * @return Map (key: harvester directory name, value: EndpointDescription object)
     */
    public static Map<String, EndpointDescription> loadEndpointMap(File mappingFile) {
        LOG.info("Loading harvester mapping data");
        HashMap<String, EndpointDescription> endpointDescriptionMap = new HashMap<>();
        try {
            CSVParser parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(false).build();
            CSVReader reader = new CSVReaderBuilder(new FileReader(mappingFile)).withSkipLines(1).withCSVParser(parser).build();
            String[] lineArray;
            while((lineArray = reader.readNext()) != null) {
                if(lineArray.length == 4) {
                    EndpointDescription endpoint = new EndpointDescription(lineArray[0], lineArray[2], lineArray[3]);
                    endpointDescriptionMap.put(lineArray[1], endpoint);
                } else {
                    LOG.info("Ignoring mapping line {}", Arrays.toString(lineArray));
                }
            }
        } catch (IOException ioe) {
            LOG.error("Error when trying to read from harvester mapping file: {}.", ioe.getMessage());
        }

        LOG.info("...found {} mappings.", endpointDescriptionMap.keySet().size());
        return endpointDescriptionMap;
    }
    
    
}
