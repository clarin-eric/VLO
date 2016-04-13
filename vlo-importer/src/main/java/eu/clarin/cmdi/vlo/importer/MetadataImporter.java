package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.StringUtils;
import eu.clarin.cmdi.vlo.config.DataRoot;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import java.time.LocalDate;
import java.time.ZoneId;
import static java.time.temporal.ChronoUnit.DAYS;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

/**
 * The main metadataImporter class. Also contains the main function.
 *
 * The metadataimporter reads all the config files and then, for each
 * metadatafile in each defined directory structure parses and imports them as
 * defined in the configuration. The startImport function starts the importing
 * and so on.
 */
public class MetadataImporter {

    private static final int SOLR_SERVER_THREAD_COUNT = 2;
    /**
     * Defines which files to try and parse. In this case all files ending in
     * "xml" or "cmdi".
     */
    private static final String[] VALID_CMDI_EXTENSIONS = new String[]{"xml", "cmdi"};

    /**
     * Log log log log
     */
    protected final static Logger LOG = LoggerFactory.getLogger(MetadataImporter.class);
    /**
     * Some place to store errors.
     */
    private static Throwable serverError;
    /**
     * the solr server.
     */
    private ConcurrentUpdateSolrServer solrServer;
    /**
     * Defines the post-processor associations. At import, for each facet value,
     * this map is checked and all postprocessors associated with the facet
     * _type_ are applied to the value before storing the new value in the solr
     * document.
     */
    final static Map<String, PostProcessor> POST_PROCESSORS = new HashMap<>();

    static {
        POST_PROCESSORS.put(FacetConstants.FIELD_ID, new IdPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_CONTINENT, new ContinentNamePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_COUNTRY, new CountryNamePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_LANGUAGE_CODE, new LanguageCodePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_LANGUAGE_NAME, new LanguageNamePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_AVAILABILITY, new AvailabilityPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_ORGANISATION, new OrganisationPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_TEMPORAL_COVERAGE, new TemporalCoveragePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_NATIONAL_PROJECT, new NationalProjectPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_CLARIN_PROFILE, new CMDIComponentProfileNamePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_RESOURCE_CLASS, new ResourceClassPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_LICENSE, new LicensePostProcessor());
    }

    /**
     * Constructor
     */
    public MetadataImporter() {
    }

    public MetadataImporter(String clDatarootsList) {
        this.clDatarootsList = clDatarootsList;
    }

    /**
     * Contains MDSelflinks (usually). Just to know what we have already done.
     */
    protected final Set<String> processedIds = new HashSet<>();
    /**
     * Some caching for solr documents (we are more efficient if we ram a whole
     * bunch to the solr server at once.
     */
    protected List<SolrInputDocument> docs = new ArrayList<>();

    // SOME STATS
    protected int nrOFDocumentsSend;
    protected int nrOfFilesAnalyzed = 0;
    protected int nrOfFilesWithoutId = 0;
    protected int nrOfFilesWithError = 0;
    protected int nrOfFilesTooLarge = 0;

    /**
     * Retrieve all files with VALID_CMDI_EXTENSIONS from all DataRoot entries
     * and starts processing for every single file
     *
     * @throws MalformedURLException
     */
    void startImport() throws MalformedURLException {

        initSolrServer();
        List<DataRoot> dataRoots = checkDataRoots();
        dataRoots = filterDataRootsWithCLArgs(dataRoots);

        long start = System.currentTimeMillis();
        try {
            // Delete the whole Solr db
            if (config.getDeleteAllFirst()) {
                LOG.info("Deleting original data...");
                solrServer.deleteByQuery("*:*");
                solrServer.commit();
                LOG.info("Deleting original data done.");
            }

            // Import the specified data roots
            for (DataRoot dataRoot : dataRoots) {
                LOG.info("Start of processing: " + dataRoot.getOriginName());
                if (dataRoot.deleteFirst()) {
                    LOG.info("Deleting data for data provider: " + dataRoot.getOriginName());
                    solrServer.deleteByQuery(FacetConstants.FIELD_DATA_PROVIDER + ":" + ClientUtils.escapeQueryChars(dataRoot.getOriginName()));
                    LOG.info("Deleting data of provider done.");
                }
                CMDIDataProcessor processor = new CMDIParserVTDXML(POST_PROCESSORS, config, false);
                List<List<File>> centreFilesList = getFilesFromDataRoot(dataRoot.getRootFile());
                // import files from every endpoint
                for (List<File> centreFiles : centreFilesList) {
                    LOG.info("Processing directory: {}", centreFiles.get(0).getParent());

                    // identify mdSelfLinks and remove too large files from center file list
                    LOG.info("Extracting mdSelfLinks");
                    Set<String> mdSelfLinkSet = new HashSet<>();
                    Set<File> ignoredFileSet = new HashSet<>();
                    for (File file : centreFiles) {
                        if (config.getMaxFileSize() > 0
                                && file.length() > config.getMaxFileSize()) {
                            LOG.info("Skipping " + file.getAbsolutePath() + " because it is too large.");
                            nrOfFilesTooLarge++;
                            ignoredFileSet.add(file);
                        } else {
                            String mdSelfLink = null;
                            try {
                                mdSelfLink = processor.extractMdSelfLink(file);
                            } catch (Exception e) {
                                LOG.error("error in file: {}", file, e);
                                nrOfFilesWithError++;
                            }
                            if (mdSelfLink != null) {
                                mdSelfLinkSet.add(StringUtils.normalizeIdString(mdSelfLink));
                            }
                        }
                    }
                    centreFiles.removeAll(ignoredFileSet);

                    // inform structure graph about MdSelfLinks of all files in this collection
                    ResourceStructureGraph.setOccurringMdSelfLinks(mdSelfLinkSet);
                    LOG.info("...extracted {} mdSelfLinks", mdSelfLinkSet.size());

                    // process every file in this collection
                    for (File file : centreFiles) {
                        LOG.debug("PROCESSING FILE: {}", file.getAbsolutePath());
                        processCmdi(file, dataRoot, processor);
                    }
                    if (!docs.isEmpty()) {
                        sendDocs();
                    }
                    solrServer.commit();
                    if (config.isProcessHierarchies()) {
                        updateDocumentHierarchy();
                    }
                }
                updateDaysSinceLastImport(dataRoot);
                LOG.info("End of processing: " + dataRoot.getOriginName());
            }

            // delete outdated entries (based on maxDaysInSolr parameter)
            if (config.getMaxDaysInSolr() > 0 && config.getDeleteAllFirst() == false) {
                LOG.info("Deleting old files that were not seen for more than " + config.getMaxDaysInSolr() + " days...");
                solrServer.deleteByQuery(FacetConstants.FIELD_LAST_SEEN + ":[* TO NOW-" + config.getMaxDaysInSolr() + "DAYS]");
                LOG.info("Deleting old files done.");
            }
        } catch (SolrServerException e) {
            LOG.error("error updating files:\n", e);
            LOG.error("Also see vlo_solr server logs for more information");
        } catch (IOException e) {
            LOG.error("error updating files:\n", e);
        } finally {
            try {
                if (solrServer != null) {
                    solrServer.commit();
                    buildSuggesterIndex();
                }
            } catch (SolrServerException | IOException e) {
                LOG.error("cannot commit:\n", e);
            }
        }
        long took = (System.currentTimeMillis() - start) / 1000;
        LOG.info("Found " + nrOfFilesWithoutId + " file(s) without an id. (id is generated based on fileName but that may not be unique)");
        LOG.info("Found " + nrOfFilesWithError + " file(s) with errors.");
        LOG.info("Found " + nrOfFilesTooLarge + " file(s) too large.");
        LOG.info("Update of " + nrOFDocumentsSend + " took " + took + " secs. Total nr of files analyzed " + nrOfFilesAnalyzed);
        solrServer.shutdown();
    }

    /**
     * Check a List of DataRoots for existence of RootFile (typically parent
     * directory of metadata files)
     *
     * @return
     */
    protected List<DataRoot> checkDataRoots() {
        List<DataRoot> dataRoots = config.getDataRoots();
        List<DataRoot> existingDataRoots = new LinkedList<>();
        for (DataRoot dataRoot : dataRoots) {
            if (!dataRoot.getRootFile().exists()) {
                LOG.warn("Root file " + dataRoot.getRootFile() + " does not exist. It could be configuration error! Proceeding with next ...");
            } else {
                existingDataRoots.add(dataRoot);
            }

        }
        return existingDataRoots;
    }

    /**
     * if user specified which data roots should be imported, list of existing
     * data roots will be filtered with the list from user
     *
     * @param dataRoots complete list of DataRoots
     * @return list of DataRoots without DataRoots excluded by the user
     */
    protected List<DataRoot> filterDataRootsWithCLArgs(List<DataRoot> dataRoots) {
        if (clDatarootsList == null) {
            return dataRoots;
        }

        LOG.info("Filtering configured data root files with command line arguments: \"" + clDatarootsList + "\"");

        LinkedList<File> fsDataRoots = new LinkedList<>();

        List<String> paths = Arrays.asList((clDatarootsList.split("\\s+")));

        //Convert String paths to File objects for comparison
        for (String path : paths) {
            fsDataRoots.add(new File(path));
        }

        List<DataRoot> filteredDataRoots = new LinkedList<>();
        try {
            //filter data
            dr:
            for (DataRoot dataRoot : dataRoots) {
                for (File fsDataRoot : fsDataRoots) {
                    if (fsDataRoot.getCanonicalPath().equals(dataRoot.getRootFile().getCanonicalPath())) {
                        filteredDataRoots.add(dataRoot);
                        fsDataRoots.remove(fsDataRoot);
                        continue dr;
                    }
                }
                LOG.info("Root file " + dataRoot.getRootFile() + " will be omitted from processing");
            }
        } catch (IOException e) {
            filteredDataRoots = dataRoots;
        }

        return filteredDataRoots;
    }

    /**
     * Get all files with VALID_CMDI_EXTENSIONS if rootFile is a directory that
     * contains center directories or rootFile if it is a file
     *
     * @param rootFile
     * @return List with centre Lists of all contained CMDI files if rootFile is
     * a directory or rootFile if it is a File
     */
    protected List<List<File>> getFilesFromDataRoot(File rootFile) {
        List<List<File>> result = new ArrayList<>();
        if (rootFile.isFile()) {
            List<File> singleFileList = new ArrayList<>();
            singleFileList.add(rootFile);
            result.add(singleFileList);
        } else {
            File[] centerDirs = rootFile.listFiles();
            for (File centerDir : centerDirs) {
                List<File> centerFileList = new ArrayList<>();
                if (centerDir.isDirectory()) {
                    centerFileList.addAll(FileUtils.listFiles(centerDir, VALID_CMDI_EXTENSIONS, true));
                }

                if (!centerFileList.isEmpty()) {
                    result.add(centerFileList);
                }
            }
        }
        return result;
    }

    /**
     * Create an interface to the SOLR server.
     *
     * After the interface has been created the importer can send documents to
     * the server. Sending documents involves a queue. The importer adds
     * documents to a queue, and dedicated threads will empty it, and
     * effectively store store the documents.
     *
     * @throws MalformedURLException
     */
    protected void initSolrServer() throws MalformedURLException {
        String solrUrl = config.getSolrUrl();
        LOG.info("Initializing concurrent Solr Server on {} with {} threads", solrUrl, SOLR_SERVER_THREAD_COUNT);

        /* Specify the number of documents in the queue that will trigger the
         * threads, two of them, emptying it.
         */
        solrServer = new ConcurrentUpdateSolrServer(solrUrl,
                config.getMinDocsInSolrQueue(), SOLR_SERVER_THREAD_COUNT) {
            /*
                     * Let the super class method handle exceptions. Make the
                     * exception available to the importer in the form of the
                     * serverError variable.
             */
            @Override
            public void handleError(Throwable exception) {
                super.handleError(exception);
                serverError = exception;
            }
        };
    }

    /**
     * Process single CMDI file with CMDIDataProcessor
     *
     * @param file CMDI input file
     * @param dataOrigin
     * @param processor
     * @throws SolrServerException
     * @throws IOException
     */
    protected void processCmdi(File file, DataRoot dataOrigin, CMDIDataProcessor processor) throws SolrServerException, IOException {
        nrOfFilesAnalyzed++;
        CMDIData cmdiData = null;
        try {
            cmdiData = processor.process(file);
            if (!idOk(cmdiData.getId())) {
                cmdiData.setId(dataOrigin.getOriginName() + "/" + file.getName()); //No id found in the metadata file so making one up based on the file name. Not quaranteed to be unique, but we have to set something.
                nrOfFilesWithoutId++;
            }
        } catch (Exception e) {
            LOG.error("error in file: {}", file, e);
            nrOfFilesWithError++;
        }
        if (cmdiData != null) {
            if (processedIds.add(cmdiData.getId())) {
                SolrInputDocument solrDocument = cmdiData.getSolrDocument();
                if (solrDocument != null) {
                    updateDocument(solrDocument, cmdiData, file, dataOrigin);
                    if (config.isProcessHierarchies() && ResourceStructureGraph.getVertex(cmdiData.getId()) != null) {
                        ResourceStructureGraph.getVertex(cmdiData.getId()).setWasImported(true);
                    }
                }
            } else {
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
        return id != null && !id.isEmpty();
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
     * @throws SolrServerException
     * @throws IOException
     */
    protected void updateDocument(SolrInputDocument solrDocument, CMDIData cmdiData, File file, DataRoot dataOrigin) throws SolrServerException,
            IOException {
        if (!solrDocument.containsKey(FacetConstants.FIELD_COLLECTION)) {
            solrDocument.addField(FacetConstants.FIELD_COLLECTION, dataOrigin.getOriginName());
        }
        solrDocument.addField(FacetConstants.FIELD_DATA_PROVIDER, dataOrigin.getOriginName());
        solrDocument.addField(FacetConstants.FIELD_ID, cmdiData.getId());
        solrDocument.addField(FacetConstants.FIELD_FILENAME, file.getAbsolutePath());

        String metadataSourceUrl = dataOrigin.getPrefix();
        metadataSourceUrl += file.getAbsolutePath().substring(dataOrigin.getToStrip().length());

        solrDocument.addField(FacetConstants.FIELD_COMPLETE_METADATA, metadataSourceUrl);

        // add SearchServices (should be CQL endpoint)
        for (Resource resource : cmdiData.getSearchResources()) {
            solrDocument.addField(FacetConstants.FIELD_SEARCH_SERVICE, resource.getResourceName());
        }

        // add landing page resource
        for (Resource resource : cmdiData.getLandingPageResources()) {
            solrDocument.addField(FacetConstants.FIELD_LANDINGPAGE, resource.getResourceName());
        }

        // add search page resource
        for (Resource resource : cmdiData.getSearchPageResources()) {
            solrDocument.addField(FacetConstants.FIELD_SEARCHPAGE, resource.getResourceName());
        }

        // add timestamp
        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        solrDocument.addField(FacetConstants.FIELD_LAST_SEEN, df.format(dt));

        // set number of days since last import to '0'
        solrDocument.addField(FacetConstants.FIELD_DAYS_SINCE_LAST_SEEN, 0);

        // add resource proxys      
        addResourceData(solrDocument, cmdiData);

        LOG.debug("Adding document for submission to SOLR: {}", file);
        docs.add(solrDocument);
        if (docs.size() == config.getMaxDocsInList()) {
            sendDocs();
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
        List<Object> fieldValues = solrDocument.containsKey(FacetConstants.FIELD_FORMAT) ? new ArrayList<>(solrDocument
                .getFieldValues(FacetConstants.FIELD_FORMAT)) : null;
        solrDocument.removeField(FacetConstants.FIELD_FORMAT); //Remove old values they might be overwritten.
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

            FormatPostProcessor processor = new FormatPostProcessor();
            mimeType = processor.process(mimeType).get(0);

            // TODO check should probably be moved into Solr (by using some minimum length filter)
            if (!mimeType.equals("")) {
                solrDocument.addField(FacetConstants.FIELD_FORMAT, mimeType);
            }
            solrDocument.addField(FacetConstants.FIELD_RESOURCE, mimeType + FacetConstants.FIELD_RESOURCE_SPLIT_CHAR
                    + resource.getResourceName());
        }
        solrDocument.addField(FacetConstants.FIELD_RESOURCE_COUNT, resources.size());
    }

    /**
     * Send current list of SolrImputDocuments to SolrServer and clears list
     * afterwards
     *
     * @throws SolrServerException
     * @throws IOException
     */
    protected void sendDocs() throws SolrServerException, IOException {
        LOG.info("Sending " + docs.size() + " docs to solr server. Total number of docs updated till now: " + nrOFDocumentsSend);
        nrOFDocumentsSend += docs.size();
        solrServer.add(docs);
        if (serverError != null) {
            throw new SolrServerException(serverError);
        }
        docs = new ArrayList<>();
    }

    /**
     * Builds suggester index for autocompletion
     *
     * @throws SolrServerException
     * @throws MalformedURLException
     */
    private void buildSuggesterIndex() throws SolrServerException, MalformedURLException {
        LOG.info("Building index for autocompletion.");
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("qt", "/suggest");
        paramMap.put("spellcheck.build", "true");
        SolrParams params = new MapSolrParams(paramMap);
        solrServer.query(params);
    }

    /**
     * Updates documents in Solr with their hierarchy weight and lists of
     * related resources (hasPart & isPartOf)
     *
     * @throws SolrServerException
     * @throws MalformedURLException
     */
    private void updateDocumentHierarchy() throws SolrServerException, MalformedURLException, IOException {
        LOG.info(ResourceStructureGraph.printStatistics(0));
        Boolean updatedDocs = false;
        List<SolrInputDocument> updateDocs = new ArrayList<>();
        Iterator<CmdiVertex> vertexIter = ResourceStructureGraph.getFoundVertices().iterator();
        while (vertexIter.hasNext()) {
            CmdiVertex vertex = vertexIter.next();
            List<String> incomingVertexNames = ResourceStructureGraph.getIncomingVertexNames(vertex);
            List<String> outgoingVertexNames = ResourceStructureGraph.getOutgoingVertexNames(vertex);

            // update vertex if changes are necessary (necessary if non-default weight or edges to other resources)
            if (vertex.getHierarchyWeight() != 0 || !incomingVertexNames.isEmpty() || !outgoingVertexNames.isEmpty()) {
                updatedDocs = true;
                SolrInputDocument doc = new SolrInputDocument();
                doc.setField(FacetConstants.FIELD_ID, Arrays.asList(vertex.getId()));

                if (vertex.getHierarchyWeight() != 0) {
                    Map<String, Integer> partialUpdateMap = new HashMap<>();
                    partialUpdateMap.put("set", Math.abs(vertex.getHierarchyWeight()));
                    doc.setField(FacetConstants.FIELD_HIERARCHY_WEIGHT, partialUpdateMap);
                }

                // remove vertices that were not imported
                Iterator<String> incomingVertexIter = incomingVertexNames.iterator();
                while (incomingVertexIter.hasNext()) {
                    String vertexId = incomingVertexIter.next();
                    if (ResourceStructureGraph.getVertex(vertexId) == null || !ResourceStructureGraph.getVertex(vertexId).getWasImported()) {
                        incomingVertexIter.remove();
                    }
                }
                Iterator<String> outgoingVertexIter = outgoingVertexNames.iterator();
                while (outgoingVertexIter.hasNext()) {
                    String vertexId = outgoingVertexIter.next();
                    if (ResourceStructureGraph.getVertex(vertexId) == null || !ResourceStructureGraph.getVertex(vertexId).getWasImported()) {
                        outgoingVertexIter.remove();
                    }
                }

                if (!incomingVertexNames.isEmpty()) {
                    Map<String, List<String>> partialUpdateMap = new HashMap<>();
                    partialUpdateMap.put("set", incomingVertexNames);
                    doc.setField(FacetConstants.FIELD_HAS_PART, partialUpdateMap);

                    Map<String, Integer> partialUpdateMapCount = new HashMap<>();
                    partialUpdateMapCount.put("set", incomingVertexNames.size());
                    doc.setField(FacetConstants.FIELD_HAS_PART_COUNT, partialUpdateMapCount);

                    // add hasPartCount weight
                    Double hasPartCountWeight = Math.log10(1 + Math.min(50, incomingVertexNames.size()));
                    Map<String, Double> partialUpdateMapCountWeight = new HashMap<>();
                    partialUpdateMapCountWeight.put("set", hasPartCountWeight);
                    doc.setField(FacetConstants.FIELD_HAS_PART_COUNT_WEIGHT, partialUpdateMapCountWeight);
                }

                if (!outgoingVertexNames.isEmpty()) {
                    Map<String, List<String>> partialUpdateMap = new HashMap<>();
                    partialUpdateMap.put("set", outgoingVertexNames);
                    doc.setField(FacetConstants.FIELD_IS_PART_OF, partialUpdateMap);
                }
                updateDocs.add(doc);
            }

            if (updateDocs.size() == config.getMaxDocsInList()) {
                solrServer.add(updateDocs);
                if (serverError != null) {
                    throw new SolrServerException(serverError);
                }
                updateDocs = new ArrayList<>();
            }
        }
        if (!updateDocs.isEmpty()) {
            solrServer.add(updateDocs);
            if (serverError != null) {
                throw new SolrServerException(serverError);
            }
        }

        if (updatedDocs) {
            solrServer.commit();
        }

        ResourceStructureGraph.clearResourceGraph();
    }

    /**
     * Update "days since last import" field for all Solr records of dataRoot.
     * Notice that it will not touch records that have a "last seen" value newer
     * than today. Therefore this should be called <em>after</em> normal 
     * processing of data root!
     *
     * @param dataRoot
     * @throws SolrServerException
     * @throws IOException
     */
    private void updateDaysSinceLastImport(DataRoot dataRoot) throws SolrServerException, IOException {
        LOG.info("Updating \"days since last import\" in Solr for: {}", dataRoot.getOriginName());

        SolrQuery query = new SolrQuery();
        query.setQuery(
                //we're going to process all records in the current data root...
                FacetConstants.FIELD_DATA_PROVIDER + ":" + ClientUtils.escapeQueryChars(dataRoot.getOriginName())
                + " AND "
                // ...that have a "last seen" value _older_ than today (on update/initialisation all records get 0 so we can skip the rest)
                + FacetConstants.FIELD_LAST_SEEN + ":[* TO NOW-1DAY]"
        );
        query.setFields(FacetConstants.FIELD_ID, FacetConstants.FIELD_LAST_SEEN);
        int fetchSize = 1000;
        query.setRows(fetchSize);
        QueryResponse rsp = solrServer.query(query);

        final long totalResults = rsp.getResults().getNumFound();
        final LocalDate nowDate = LocalDate.now();

        final int docsListSize = config.getMaxDocsInList();
        List<SolrInputDocument> updateDocs = new ArrayList<>(docsListSize);

        Boolean updatedDocs = false;
        int offset = 0;

        while (offset < totalResults) {
            query.setStart(offset);
            query.setRows(fetchSize);

            for (SolrDocument doc : solrServer.query(query).getResults()) {
                updatedDocs = true;

                String recordId = (String) doc.getFieldValue(FacetConstants.FIELD_ID);
                Date lastImportDate = (Date) doc.getFieldValue(FacetConstants.FIELD_LAST_SEEN);
                LocalDate oldDate = lastImportDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                long daysSinceLastSeen = DAYS.between(oldDate, nowDate);

                SolrInputDocument updateDoc = new SolrInputDocument();
                updateDoc.setField(FacetConstants.FIELD_ID, recordId);

                Map<String, Long> partialUpdateMap = new HashMap<>();
                partialUpdateMap.put("set", daysSinceLastSeen);
                updateDoc.setField(FacetConstants.FIELD_DAYS_SINCE_LAST_SEEN, partialUpdateMap);

                updateDocs.add(updateDoc);

                if (updateDocs.size() == docsListSize) {
                    solrServer.add(updateDocs);
                    if (serverError != null) {
                        throw new SolrServerException(serverError);
                    }
                    updateDocs = new ArrayList<>(docsListSize);
                }
            }
            offset += fetchSize;
            LOG.info("Updating \"days since last import\": {} out of {} records updated", offset, totalResults);
        }

        if (!updateDocs.isEmpty()) {
            solrServer.add(updateDocs);
            if (serverError != null) {
                throw new SolrServerException(serverError);
            }
        }

        if (updatedDocs) {
            solrServer.commit();
        }

        LOG.info("Updating \"days since last import\" done.");
    }

    public static VloConfig config;

    public static LanguageCodeUtils languageCodeUtils;

    //data roots passed from command line    
    private String clDatarootsList = null;

    /**
     * @param args
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void main(String[] args) throws MalformedURLException, IOException {

        // path to the configuration file
        String configFile = null;

        // use the Apache cli framework for getting command line parameters
        Options options = new Options();

        // Data root list passed from command line with -l option
        String cldrList = null;

        /**
         * Add a "c" option, the option indicating the specification of an XML
         * configuration file
         *
         * "l" option - to specify which data roots (from config file) to import
         * imports all by default
         */
        options.addOption("c", true, "-c <file> : use parameters specified in <file>");
        options.addOption("l", true, "-l <dataroot> [ ' ' <dataroot> ]* :  space separated list of dataroots to be processed.\n"
                + "If dataroot is not specified in config file it will be ignored.");
        options.getOption("l").setOptionalArg(true);

        CommandLineParser parser = new PosixParser();

        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("c")) {

                // the "c" option was specified, now get its value
                configFile = cmd.getOptionValue("c");
            }

            if (cmd.hasOption("l")) {
                cldrList = cmd.getOptionValue("l");
            }

        } catch (org.apache.commons.cli.ParseException ex) {

            /**
             * Caught an exception caused by command line parsing. Try to get
             * the name of the configuration file by querying the system
             * property.
             */
            String message = "Command line parsing failed. " + ex.getMessage();
            LOG.error(message);
            System.err.println(message);
        }

        if (configFile == null) {

            String message;

            message = "Could not get config file name via the command line, trying the system properties.";
            LOG.info(message);

            String key;

            key = "configFile";
            configFile = System.getProperty(key);
        }

        if (configFile == null) {

            String message;

            message = "Could not get filename as system property either - stopping.";
            LOG.error(message);
        } else {
            // read the configuration from the externally supplied file
            final URL configUrl;
            if (configFile.startsWith("file:")) {
                configUrl = new URL(configFile);
            } else {
                configUrl = new File(configFile).toURI().toURL();
            }
            System.out.println("Reading configuration from " + configUrl.toString());
            LOG.info("Reading configuration from " + configUrl.toString());
            final XmlVloConfigFactory configFactory = new XmlVloConfigFactory(configUrl);
            MetadataImporter.config = configFactory.newConfig();
            MetadataImporter.languageCodeUtils = new LanguageCodeUtils(MetadataImporter.config);

            // optionally, modify the configuration here
            // create and start the importer
            MetadataImporter importer = new MetadataImporter(cldrList);
            importer.startImport();

            // finished importing
            if (MetadataImporter.config.printMapping()) {
                File file = new File("xsdMapping.txt");
                FacetMappingFactory.printMapping(file);
                LOG.info("Printed facetMapping in " + file);
            }
        }
    }
}
