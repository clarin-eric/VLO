package eu.clarin.cmdi.vlo.importer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
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
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph.CmdiVertex;
import java.time.LocalDate;
import java.time.ZoneId;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    private final VloConfig config;
    private ExecutorService fileProcessingPool;

    //data roots passed from command line    
    private final String clDatarootsList;

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
     * interface to the solr server
     */
    private final SolrBridge solrBridge;
    
    private final CMDIDataProcessor processor;

    private static class DefaultSolrBridgeFactory {

        static SolrBridge createDefaultSolrBridge(VloConfig config) {
            final SolrBridgeImpl solrBridge = new BufferingSolrBridgeImpl(config);
            solrBridge.setCommit(true);
            return solrBridge;
        }
    }

    /**
     * Defines the post-processor associations. At import, for each facet value,
     * this map is checked and all postprocessors associated with the facet
     * _type_ are applied to the value before storing the new value in the solr
     * document.
     */
    protected final Map<String, PostProcessor> postProcessors;

    /**
     * Contains MDSelflinks (usually). Just to know what we have already done.
     */
    protected final Set<String> processedIds = new HashSet<>();
    /**
     * Some caching for solr documents (we are more efficient if we ram a whole
     * bunch to the solr server at once.
     */
    //protected List<SolrInputDocument> docs = new ArrayList<>();

    // SOME STATS
    protected final AtomicInteger nrOFDocumentsSent = new AtomicInteger();
    protected final AtomicInteger nrOfFilesAnalyzed = new AtomicInteger();
    protected final AtomicInteger nrOfFilesSkipped = new AtomicInteger();
    protected final AtomicInteger nrOfFilesWithoutId = new AtomicInteger();
    protected final AtomicInteger nrOfFilesWithError = new AtomicInteger();
    protected final AtomicInteger nrOfFilesTooLarge = new AtomicInteger();
    private Long time;

    public MetadataImporter(VloConfig config, LanguageCodeUtils languageCodeUtils, FacetMappingFactory mappingFactory, VLOMarshaller marshaller, String clDatarootsList) {
        this(config, languageCodeUtils, mappingFactory, marshaller, clDatarootsList, DefaultSolrBridgeFactory.createDefaultSolrBridge(config));
    }

    public MetadataImporter(VloConfig config, LanguageCodeUtils languageCodeUtils, FacetMappingFactory mappingFactory, VLOMarshaller marshaller, String clDatarootsList, SolrBridge solrBrdige) {
        this.config = config;
        this.clDatarootsList = clDatarootsList;
        this.postProcessors = registerPostProcessors(config, languageCodeUtils);
        this.solrBridge = solrBrdige;
        this.processor = new CMDIParserVTDXML(postProcessors, config, mappingFactory, marshaller, false);

    }

    protected static Map<String, PostProcessor> registerPostProcessors(VloConfig config, LanguageCodeUtils languageCodeUtils) {
        return ImmutableMap.<String, PostProcessor>builder()
                .put(FacetConstants.FIELD_ID, new IdPostProcessor())
                .put(FacetConstants.FIELD_CONTINENT, new ContinentNamePostProcessor())
                .put(FacetConstants.FIELD_COUNTRY, new CountryNamePostProcessor(config))
                .put(FacetConstants.FIELD_LANGUAGE_CODE, new LanguageCodePostProcessor(config, languageCodeUtils))
                .put(FacetConstants.FIELD_LANGUAGE_NAME, new LanguageNamePostProcessor(languageCodeUtils))
                .put(FacetConstants.FIELD_AVAILABILITY, new AvailabilityPostProcessor(config))
                .put(FacetConstants.FIELD_LICENSE_TYPE, new LicenseTypePostProcessor(config))
                .put(FacetConstants.FIELD_ORGANISATION, new OrganisationPostProcessor(config))
                .put(FacetConstants.FIELD_TEMPORAL_COVERAGE, new TemporalCoveragePostProcessor())
                .put(FacetConstants.FIELD_NATIONAL_PROJECT, new NationalProjectPostProcessor(config))
                .put(FacetConstants.FIELD_CLARIN_PROFILE, new CMDIComponentProfileNamePostProcessor(config))
                .put(FacetConstants.FIELD_RESOURCE_CLASS, new ResourceClassPostProcessor())
                .put(FacetConstants.FIELD_LICENSE, new LicensePostProcessor(config))
                .put(FacetConstants.FIELD_NAME, new NamePostProcessor())
                .build();
    }

    /**
     * Retrieve all files with VALID_CMDI_EXTENSIONS from all DataRoot entries
     * and starts processing for every single file
     *
     * @throws MalformedURLException
     */
    void startImport() throws MalformedURLException {
        solrBridge.init();

        final long start = System.currentTimeMillis();
        try {
            final List<DataRoot> dataRoots = filterDataRootsWithCLArgs(checkDataRoots());

            // Delete the whole Solr db
            if (config.getDeleteAllFirst()) {
                deleteAll();
            }

            final int nProcessingThreads = config.getFileProcessingThreads();
            if (nProcessingThreads > 0) {
                LOG.info("Initiating processing pool with {} threads", nProcessingThreads);
                fileProcessingPool = Executors.newFixedThreadPool(config.getFileProcessingThreads());
            } else {
                LOG.info("Initiating work stealing pool (0 >= file processing threads in configuration)");
                fileProcessingPool = Executors.newWorkStealingPool();
                LOG.info("Pool was created with parallelism level {}", ((ForkJoinPool) fileProcessingPool).getParallelism());
            }

            // Import the specified data roots
            for (DataRoot dataRoot : dataRoots) {
                processDataRoot(dataRoot);
            }
            // Delete outdated entries (based on maxDaysInSolr parameter)
            if (config.getMaxDaysInSolr() > 0 && config.getDeleteAllFirst() == false) {
                purgeOldDocs();
            }
        } catch (SolrServerException e) {
            LOG.error("error updating files:\n", e);
            LOG.error("Also see vlo_solr server logs for more information");
        } catch (IOException e) {
            LOG.error("error updating files:\n", e);
        } catch (InterruptedException ex) {
            LOG.error("Interrupted while importing", ex);
        } finally {
            try {
                solrBridge.commit();
                buildSuggesterIndex();
            } catch (SolrServerException | IOException e) {
                LOG.error("cannot commit:\n", e);
            }
            shutdown();
        }
        time = (System.currentTimeMillis() - start);
        logStatistics();
    }

    protected void deleteAll() throws IOException, SolrServerException {
        LOG.info("Deleting original data...");
        solrBridge.getServer().deleteByQuery("*:*");
        solrBridge.commit();
        LOG.info("Deleting original data done.");
    }

    protected void processDataRoot(DataRoot dataRoot) throws SolrServerException, IOException, InterruptedException {
        LOG.info("Start of processing: " + dataRoot.getOriginName());
        if (dataRoot.deleteFirst()) {
            LOG.info("Deleting data for data provider: " + dataRoot.getOriginName());
            solrBridge.getServer().deleteByQuery(FacetConstants.FIELD_DATA_PROVIDER + ":" + ClientUtils.escapeQueryChars(dataRoot.getOriginName()));
            LOG.info("Deleting data of provider done.");
        }
        // import files from every centre/endpoint within the data root
        for (List<File> centreFiles : getFilesFromDataRoot(dataRoot.getRootFile())) {
            processCentreFiles(centreFiles, dataRoot);
        }
        updateDaysSinceLastImport(dataRoot);
        LOG.info("End of processing: " + dataRoot.getOriginName());
    }

    protected void processCentreFiles(final List<File> centreFiles, final DataRoot dataRoot) throws IOException, SolrServerException, InterruptedException {
        LOG.info("Processing directory: {}", centreFiles.get(0).getParent());
        String centerDirName = centreFiles.get(0).getParentFile().getName();

        final ResourceStructureGraph resourceStructureGraph = instantiateResourceStructureGraph(dataRoot, centerDirName);

        // pre-process: identify mdSelfLinks and remove too large files from center file list
        LOG.info("Checking file list...");
        final Set<String> mdSelfLinkSet = Sets.newConcurrentHashSet();
        final Set<File> ignoredFileSet = Sets.newConcurrentHashSet();

        //(perform in thread pool)
        final Stream<Callable<Void>> preProcessors = centreFiles.stream().map((File file) -> {
            return (Callable) () -> {
                final boolean createHierarchyGraph = resourceStructureGraph != null;
                preProcessFile(file, createHierarchyGraph, ignoredFileSet, mdSelfLinkSet);
                return null;
            };
        });
        final Set<Callable<Void>> preProcessorsCollection = preProcessors.collect(Collectors.toSet());
        fileProcessingPool.invokeAll(preProcessorsCollection);

        //remove ignored files
        centreFiles.removeAll(ignoredFileSet);

        // inform structure graph about MdSelfLinks of all files in this collection
        if (resourceStructureGraph != null) {
            resourceStructureGraph.setOccurringMdSelfLinks(mdSelfLinkSet);
            LOG.info("...extracted {} mdSelfLinks", mdSelfLinkSet.size());
        }

        // process every file in this collection
        final Stream<Callable<Void>> processors = centreFiles.stream().map((File file) -> {
            return (Callable) () -> {
                LOG.debug("PROCESSING FILE: {}", file.getAbsolutePath());
                processCmdi(file, dataRoot, resourceStructureGraph);
                return null;
            };
        });
        final Set<Callable<Void>> processorsCollection = processors.collect(Collectors.toSet());
        fileProcessingPool.invokeAll(processorsCollection);

        LOG.info("Number of documents sent thus far: {}", nrOFDocumentsSent);
        solrBridge.commit();
        if (resourceStructureGraph != null) {
            fileProcessingPool.submit(() -> {
                try {
                    updateDocumentHierarchy(resourceStructureGraph);
                } catch (IOException | SolrServerException ex) {
                    throw new RuntimeException("An exception occurred while updating a document hierarchy for a centre in the '" + dataRoot.getOriginName() + "' data root", ex);
                }
            });
        }
    }

    /**
     * decide if hierarchy graph will be created for this centre
     *
     * @param dataRoot
     * @param centerDirName
     * @return null if no graph should be kept, otherwise a fresh resource
     * structure graph object
     */
    protected ResourceStructureGraph instantiateResourceStructureGraph(final DataRoot dataRoot, String centerDirName) {
        final boolean createHierarchyGraph;
        if (!config.isProcessHierarchies()) {
            createHierarchyGraph = false;
        } else if (dataRoot.getProcessHierarchyDirList().contains(centerDirName)) {
            createHierarchyGraph = true;
        } else if (dataRoot.getProcessHierarchyDirList().contains("*") & !dataRoot.getIgnoreHierarchyDirList().contains(centerDirName)) {
            createHierarchyGraph = true;
        } else {
            createHierarchyGraph = false;
        }
        LOG.info("Create structure graph: {}", createHierarchyGraph);
        if (createHierarchyGraph) {
            return new ResourceStructureGraph();
        } else {
            return null;
        }
    }

    protected void preProcessFile(File file, boolean createHierarchyGraph, Set<File> ignoredFileSet, Set<String> mdSelfLinkSet) {
        if (config.getMaxFileSize() > 0
                && file.length() > config.getMaxFileSize()) {
            LOG.info("Skipping {} because it is too large.", file.getAbsolutePath());
            nrOfFilesTooLarge.incrementAndGet();
            ignoredFileSet.add(file);
        } else if (createHierarchyGraph) {
            String mdSelfLink = null;
            try {
                mdSelfLink = processor.extractMdSelfLink(file);
            } catch (Exception e) {
                LOG.error("error in file: {}", file, e);
                nrOfFilesWithError.incrementAndGet();
            }
            if (mdSelfLink != null) {
                mdSelfLinkSet.add(StringUtils.normalizeIdString(mdSelfLink));
            }
        }
    }

    protected void purgeOldDocs() throws SolrServerException, IOException {
        LOG.info("Deleting old files that were not seen for more than " + config.getMaxDaysInSolr() + " days...");
        solrBridge.getServer().deleteByQuery(FacetConstants.FIELD_LAST_SEEN + ":[* TO NOW-" + config.getMaxDaysInSolr() + "DAYS]");
        LOG.info("Deleting old files done.");
    }

    protected void logStatistics() {
        LOG.info("Found {} file(s) without an id. (id is generated based on fileName but that may not be unique)", nrOfFilesWithoutId);
        LOG.info("Found {} file(s) with errors.", nrOfFilesWithError);
        LOG.info("Found {} file(s) too large.", nrOfFilesTooLarge);
        LOG.info("Skipped {} file(s) due to duplicate id.", nrOfFilesSkipped);
        LOG.info("Update of {} took {} secs. Total nr of files analyzed {}", nrOFDocumentsSent, time / 1000, nrOfFilesAnalyzed);
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
     * Process single CMDI file with CMDIDataProcessor
     *
     * @param file CMDI input file
     * @param dataOrigin
     * @param processor
     * @param resourceStructureGraph null to skip hierarchy processing
     * @throws SolrServerException
     * @throws IOException
     */
    protected void processCmdi(File file, DataRoot dataOrigin, ResourceStructureGraph resourceStructureGraph) throws SolrServerException, IOException {
        nrOfFilesAnalyzed.incrementAndGet();
        CMDIData cmdiData = null;
        try {
            cmdiData = processor.process(file, resourceStructureGraph);
            if (!idOk(cmdiData.getId())) {
                cmdiData.setId(dataOrigin.getOriginName() + "/" + file.getName()); //No id found in the metadata file so making one up based on the file name. Not quaranteed to be unique, but we have to set something.
                nrOfFilesWithoutId.incrementAndGet();
            }
        } catch (Exception e) {
            LOG.error("error in file: {}", file, e);
            nrOfFilesWithError.incrementAndGet();
        }
        if (cmdiData != null) {
            if (processedIds.add(cmdiData.getId())) {
                SolrInputDocument solrDocument = cmdiData.getSolrDocument();
                if (solrDocument != null) {
                    updateDocument(solrDocument, cmdiData, file, dataOrigin);
                    if (resourceStructureGraph != null && resourceStructureGraph.getVertex(cmdiData.getId()) != null) {
                        resourceStructureGraph.getVertex(cmdiData.getId()).setWasImported(true);
                    }
                }
            } else {
                nrOfFilesSkipped.incrementAndGet();
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

        solrBridge.addDocument(solrDocument);
        if (nrOFDocumentsSent.incrementAndGet() % 250 == 0) {
            LOG.info("Number of documents sent thus far: {}", nrOFDocumentsSent);
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
            mimeType = processor.process(mimeType, null).get(0);

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
        solrBridge.getServer().query(params);
    }

    /**
     * Updates documents in Solr with their hierarchy weight and lists of
     * related resources (hasPart & isPartOf)
     *
     * @throws SolrServerException
     * @throws MalformedURLException
     */
    private synchronized void updateDocumentHierarchy(ResourceStructureGraph resourceStructureGraph) throws SolrServerException, MalformedURLException, IOException {
        LOG.info(resourceStructureGraph.printStatistics(0));
        final AtomicInteger updateCount = new AtomicInteger();
        final Iterator<CmdiVertex> vertexIter = resourceStructureGraph.getFoundVertices().iterator();
        while (vertexIter.hasNext()) {
            final CmdiVertex vertex = vertexIter.next();
            final List<String> incomingVertexNames = resourceStructureGraph.getIncomingVertexNames(vertex);
            final List<String> outgoingVertexNames = resourceStructureGraph.getOutgoingVertexNames(vertex);

            // update vertex if changes are necessary (necessary if non-default weight or edges to other resources)
            if (vertex.getHierarchyWeight() != 0 || !incomingVertexNames.isEmpty() || !outgoingVertexNames.isEmpty()) {
                updateCount.incrementAndGet();
                final SolrInputDocument doc = new SolrInputDocument();
                doc.setField(FacetConstants.FIELD_ID, Arrays.asList(vertex.getId()));

                if (vertex.getHierarchyWeight() != 0) {
                    final Map<String, Integer> partialUpdateMap = new HashMap<>();
                    partialUpdateMap.put("set", Math.abs(vertex.getHierarchyWeight()));
                    doc.setField(FacetConstants.FIELD_HIERARCHY_WEIGHT, partialUpdateMap);
                }

                // remove vertices that were not imported
                final Iterator<String> incomingVertexIter = incomingVertexNames.iterator();
                while (incomingVertexIter.hasNext()) {
                    String vertexId = incomingVertexIter.next();
                    if (resourceStructureGraph.getVertex(vertexId) == null || !resourceStructureGraph.getVertex(vertexId).getWasImported()) {
                        incomingVertexIter.remove();
                    }
                }
                final Iterator<String> outgoingVertexIter = outgoingVertexNames.iterator();
                while (outgoingVertexIter.hasNext()) {
                    final String vertexId = outgoingVertexIter.next();
                    if (resourceStructureGraph.getVertex(vertexId) == null || !resourceStructureGraph.getVertex(vertexId).getWasImported()) {
                        outgoingVertexIter.remove();
                    }
                }

                if (!incomingVertexNames.isEmpty()) {
                    doc.setField(FacetConstants.FIELD_HAS_PART, ImmutableMap.of("set", incomingVertexNames));
                    doc.setField(FacetConstants.FIELD_HAS_PART_COUNT, ImmutableMap.of("set", incomingVertexNames.size()));

                    // add hasPartCount weight
                    final Double hasPartCountWeight = Math.log10(1 + Math.min(50, incomingVertexNames.size()));
                    doc.setField(FacetConstants.FIELD_HAS_PART_COUNT_WEIGHT, ImmutableMap.of("set", hasPartCountWeight));
                }

                if (!outgoingVertexNames.isEmpty()) {
                    doc.setField(FacetConstants.FIELD_IS_PART_OF, ImmutableMap.of("set", outgoingVertexNames));
                }
                solrBridge.addDocument(doc);
            }
        }
        LOG.info("Updated {} documents in graph", updateCount.get());
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
        QueryResponse rsp = solrBridge.getServer().query(query);

        final long totalResults = rsp.getResults().getNumFound();
        final LocalDate nowDate = LocalDate.now();

        final int docsListSize = config.getMaxDocsInList();

        Boolean updatedDocs = false;
        int offset = 0;

        while (offset < totalResults) {
            query.setStart(offset);
            query.setRows(fetchSize);

            for (SolrDocument doc : solrBridge.getServer().query(query).getResults()) {
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

                solrBridge.addDocument(updateDoc);
                final Throwable error = solrBridge.popError();
                if (error != null) {
                    throw new SolrServerException(error);
                }
            }
            offset += fetchSize;
            LOG.info("Updating \"days since last import\": {} out of {} records updated", offset, totalResults);
        }

        if (updatedDocs) {
            solrBridge.commit();
        }

        LOG.info("Updating \"days since last import\" done.");
    }

    private void shutdown() {
        //wait for processing pool to finish
        if (fileProcessingPool != null) {
            fileProcessingPool.shutdown();
            try {
                while (!fileProcessingPool.isTerminated() && !fileProcessingPool.awaitTermination(20, TimeUnit.SECONDS)) {
                    LOG.info("Waiting for processing pool to terminate...");
                }
            } catch (InterruptedException ex) {
                LOG.warn("Interrupted while waiting for termination in processing pool");
            }
        }
        //shut down Solr client
        try {
            solrBridge.shutdownServer();
        } catch (SolrServerException | IOException ex) {
            LOG.error("Failed to shutdown Solr server", ex);
        }
    }

    /**
     *
     * @return time last completed import took; may be null
     */
    public Long getTime() {
        return time;
    }

    /**
     * test constructor
     *
     * @param config
     * @param languageCodeUtils
     */
    protected MetadataImporter(VloConfig config, LanguageCodeUtils languageCodeUtils) {
        this(config, languageCodeUtils, new VLOMarshaller(), DefaultSolrBridgeFactory.createDefaultSolrBridge(config));
    }

    /**
     * test constructor
     *
     * @param config
     * @param languageCodeUtils
     * @param solrBridge
     */
    protected MetadataImporter(VloConfig config, LanguageCodeUtils languageCodeUtils, SolrBridge solrBridge) {
        this(config, languageCodeUtils, new VLOMarshaller(), solrBridge);
    }

    private MetadataImporter(VloConfig config, LanguageCodeUtils languageCodeUtils, VLOMarshaller marshaller, SolrBridge solrBridge) {
        this(config, languageCodeUtils, new FacetMappingFactory(config, marshaller), marshaller, null, solrBridge);
    }

}
