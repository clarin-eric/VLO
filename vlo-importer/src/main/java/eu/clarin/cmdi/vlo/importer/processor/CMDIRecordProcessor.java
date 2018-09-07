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
package eu.clarin.cmdi.vlo.importer.processor;

import com.google.common.collect.Sets;
import eu.clarin.cmdi.vlo.importer.ImportStatistics;
import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.DataRoot;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.EndpointDescription;
import eu.clarin.cmdi.vlo.importer.Resource;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;
import eu.clarin.cmdi.vlo.importer.normalizer.FormatPostNormalizer;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class CMDIRecordProcessor {

    protected final static Logger LOG = LoggerFactory.getLogger(CMDIRecordProcessor.class);
    private final FieldNameServiceImpl fieldNameService;
    private final CMDIDataProcessor processor;
    private final ImportStatistics importStatistics;

    /**
     * Contains MDSelflinks (usually). Just to know what we have already done.
     */
    private final Set<String> processedIds = Sets.newConcurrentHashSet();

    /**
     * Process single CMDI file with CMDIDataProcessor
     *
     * @param file CMDI input file
     * @param dataOrigin
     * @param resourceStructureGraph null to skip hierarchy processing
     * @param endpointDescription
     * @throws SolrServerException
     * @throws IOException
     */
    protected void processCmdi(File file, DataRoot dataOrigin, ResourceStructureGraph resourceStructureGraph, EndpointDescription endpointDescription) throws SolrServerException, IOException {
        importStatistics.nrOfFilesAnalyzed.incrementAndGet();
        CMDIData cmdiData = null;
        try {
            cmdiData = processor.process(file, resourceStructureGraph);
            if (!idOk(cmdiData.getId())) {
                cmdiData.setId(dataOrigin.getOriginName() + "/" + file.getName()); //No id found in the metadata file so making one up based on the file name. Not quaranteed to be unique, but we have to set something.
                importStatistics.nrOfFilesWithoutId.incrementAndGet();
            }
        } catch (Exception e) {
            LOG.error("error in file: {}", file, e);
            importStatistics.nrOfFilesWithError.incrementAndGet();
        }
        if (cmdiData != null) {
            if (!cmdiData.hasResources()) {
                importStatistics.nrOfFilesSkipped.incrementAndGet();
                LOG.warn("Skipping {}, no resource proxy found", file);
                return;
            }

            assert cmdiData.getId() != null; //idOk check guarantees this

            if (processedIds.add(cmdiData.getId())) {
                SolrInputDocument solrDocument = cmdiData.getSolrDocument();
                if (solrDocument != null) {
                    updateDocument(solrDocument, cmdiData, file, dataOrigin, endpointDescription);
                    if (resourceStructureGraph != null && resourceStructureGraph.getVertex(cmdiData.getId()) != null) {
                        resourceStructureGraph.getVertex(cmdiData.getId()).setWasImported(true);
                    }
                }
            } else {
                importStatistics.nrOfFilesSkipped.incrementAndGet();
                LOG.warn("Skipping {}, already processed id: {}", file, cmdiData.getId());
            }
        }
    }

    /**
     * Check id for validness
     *
     * @param id
     * @return true if id is acceptable, false otherwise
     */
    protected boolean idOk(String id) {
        return id != null && !id.trim().isEmpty();
    }

    /**
     * Adds some additional information from DataRoot to solrDocument, add
     * solrDocument to document list, submits list to SolrServer every 1000
     * files
     *
     * @param solrDocument
     * @param cmdiData
     * @param file
     * @param dataOrigin
     * @param endpointDescription
     * @throws SolrServerException
     * @throws IOException
     */
    protected void updateDocument(SolrInputDocument solrDocument, CMDIData cmdiData, File file, DataRoot dataOrigin, EndpointDescription endpointDescription) throws SolrServerException,
            IOException {
        solrDocument.addField(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER), dataOrigin.getOriginName());
        solrDocument.addField(fieldNameService.getFieldName(FieldKey.ID), cmdiData.getId());
        solrDocument.addField(fieldNameService.getFieldName(FieldKey.FILENAME), file.getAbsolutePath());

        // data provided by CLARIN's OAI-PMH harvester
        if (endpointDescription != null) {
            if (endpointDescription.getOaiEndpointUrl() != null) {
                solrDocument.addField(fieldNameService.getFieldName(FieldKey.OAI_ENDPOINT_URI), endpointDescription.getOaiEndpointUrl());
            }
            if (endpointDescription.getNationalProject() != null) {
                solrDocument.addField(fieldNameService.getFieldName(FieldKey.NATIONAL_PROJECT), endpointDescription.getNationalProject());
            }
            if (endpointDescription.getCentreName() != null) {
                solrDocument.addField(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER_NAME), endpointDescription.getCentreName());
            }
        }

        String metadataSourceUrl = dataOrigin.getPrefix();
        metadataSourceUrl += file.getAbsolutePath().substring(dataOrigin.getToStrip().length());

        solrDocument.addField(fieldNameService.getFieldName(FieldKey.COMPLETE_METADATA), metadataSourceUrl);

        // add SearchServices (should be CQL endpoint)
        for (Resource resource : cmdiData.getSearchResources()) {
            solrDocument.addField(fieldNameService.getFieldName(FieldKey.SEARCH_SERVICE), resource.getResourceName());
        }

        // add landing page resource
        for (Resource resource : cmdiData.getLandingPageResources()) {
            solrDocument.addField(fieldNameService.getFieldName(FieldKey.LANDINGPAGE), resource.getResourceName());
        }

        // add search page resource
        for (Resource resource : cmdiData.getSearchPageResources()) {
            solrDocument.addField(fieldNameService.getFieldName(FieldKey.SEARCHPAGE), resource.getResourceName());
        }

        // add timestamp
        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        solrDocument.addField(fieldNameService.getFieldName(FieldKey.LAST_SEEN), df.format(dt));

        // set number of days since last import to '0'
        solrDocument.addField(fieldNameService.getFieldName(FieldKey.DAYS_SINCE_LAST_SEEN), 0);

        // add resource proxys      
        addResourceData(solrDocument, cmdiData);

        LOG.debug("Adding document for submission to SOLR: {}", file);

        solrBridge.addDocument(solrDocument);
        if (importStatistics.nrOFDocumentsSent.incrementAndGet() % 250 == 0) {
            LOG.info("Number of documents sent thus far: {}", importStatistics.nrOFDocumentsSent);
        }
    }

    /**
     * Adds two fields FIELD_FORMAT and FIELD_RESOURCE. The Type can be
     * specified in the "ResourceType" element of an imdi file or possibly
     * overwritten by some more specific xpath (as in the LRT cmdi files). So if
     * a type is overwritten and already in the solrDocument we take that type.
     *
     * @param solrDocument
     * @param cmdiData
     */
    protected void addResourceData(SolrInputDocument solrDocument, CMDIData cmdiData) {
        List<Object> fieldValues = solrDocument.containsKey(fieldNameService.getFieldName(FieldKey.FORMAT)) ? new ArrayList<>(solrDocument
                .getFieldValues(fieldNameService.getFieldName(FieldKey.FORMAT))) : null;
        solrDocument.removeField(fieldNameService.getFieldName(FieldKey.FORMAT)); //Remove old values they might be overwritten.
        List<Resource> resources = cmdiData.getDataResources();
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            String mimeType = resource.getMimeType();
            if (mimeType == null) {
                if (fieldValues != null && i < fieldValues.size()) {
                    mimeType = CommonUtils.normalizeMimeType(fieldValues.get(i).toString());
                } else {
                    mimeType = CommonUtils.normalizeMimeType("");
                }
            }

            FormatPostNormalizer processor = new FormatPostNormalizer();
            mimeType = processor.process(mimeType, null).get(0);

            // TODO check should probably be moved into Solr (by using some minimum length filter)
            if (!mimeType.equals("")) {
                solrDocument.addField(fieldNameService.getFieldName(FieldKey.FORMAT), mimeType);
            }
            solrDocument.addField(fieldNameService.getFieldName(FieldKey.RESOURCE), mimeType + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR
                    + resource.getResourceName());
        }
        solrDocument.addField(fieldNameService.getFieldName(FieldKey.RESOURCE_COUNT), resources.size());
    }
}
