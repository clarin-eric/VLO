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
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.StringUtils;
import eu.clarin.cmdi.vlo.config.DataRoot;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph.CmdiVertex;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.AvailabilityPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.CMDIComponentProfileNamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.ContinentNamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.CountryNamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.FormatPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.IdPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.LanguageCodePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.LanguageNamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.LicensePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.LicenseTypePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.NamePostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.OrganisationPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.ResourceClassPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.TemporalCoveragePostNormalizer;
import eu.clarin.cmdi.vlo.importer.processor.*;
import eu.clarin.cmdi.vlo.importer.solr.BufferingSolrBridgeImpl;
import eu.clarin.cmdi.vlo.importer.solr.SolrBridge;
import eu.clarin.cmdi.vlo.importer.solr.SolrBridgeImpl;
import java.net.SocketTimeoutException;

import java.time.LocalDate;
import java.time.ZoneId;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Collection;
import java.util.Collections;
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
    protected final Map<String, AbstractPostNormalizer> postProcessors;

    /**
     * Contains MDSelflinks (usually). Just to know what we have already done.
     */
    protected final Set<String> processedIds = Sets.newConcurrentHashSet();
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
    private final FieldNameServiceImpl fieldNameService;

    public MetadataImporter(VloConfig config, LanguageCodeUtils languageCodeUtils, FacetMappingFactory mappingFactory, VLOMarshaller marshaller, String clDatarootsList) {
        this(config, languageCodeUtils, mappingFactory, marshaller, clDatarootsList, DefaultSolrBridgeFactory.createDefaultSolrBridge(config));
    }

    public MetadataImporter(VloConfig config, LanguageCodeUtils languageCodeUtils, FacetMappingFactory mappingFactory, VLOMarshaller marshaller, String clDatarootsList, SolrBridge solrBrdige) {
        this.config = config;
        this.fieldNameService = new FieldNameServiceImpl(config);
        this.clDatarootsList = clDatarootsList;
        this.postProcessors = registerPostProcessors(config, this.fieldNameService, languageCodeUtils);
        this.solrBridge = solrBrdige;
        this.processor = new CMDIParserVTDXML(postProcessors, config, mappingFactory, marshaller, false);

    }

    protected static Map<String, AbstractPostNormalizer> registerPostProcessors(VloConfig config, FieldNameService fieldNameService, LanguageCodeUtils languageCodeUtils) {
        ImmutableMap.Builder<String, AbstractPostNormalizer> imb = ImmutableMap.builder();
        
        imb.put(fieldNameService.getFieldName(FieldKey.ID), new IdPostNormalizer());
        
        if(fieldNameService.getFieldName(FieldKey.CONTINENT) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.CONTINENT), new ContinentNamePostNormalizer());
        if(fieldNameService.getFieldName(FieldKey.COUNTRY) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.COUNTRY), new CountryNamePostNormalizer(config));
        if(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE), new LanguageCodePostNormalizer(config, languageCodeUtils));
        if(fieldNameService.getFieldName(FieldKey.LANGUAGE_NAME) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.LANGUAGE_NAME), new LanguageNamePostNormalizer(languageCodeUtils));
        if(fieldNameService.getFieldName(FieldKey.AVAILABILITY) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.AVAILABILITY), new AvailabilityPostNormalizer(config));
        if(fieldNameService.getFieldName(FieldKey.LICENSE_TYPE) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.LICENSE_TYPE), new LicenseTypePostNormalizer(config));
        if(fieldNameService.getFieldName(FieldKey.ORGANISATION) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.ORGANISATION), new OrganisationPostNormalizer(config));
        if(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE), new TemporalCoveragePostNormalizer());
        if(fieldNameService.getFieldName(FieldKey.CLARIN_PROFILE) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.CLARIN_PROFILE), new CMDIComponentProfileNamePostNormalizer(config));
        if(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.RESOURCE_CLASS), new ResourceClassPostNormalizer());
        if(fieldNameService.getFieldName(FieldKey.LICENSE) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.LICENSE), new LicensePostNormalizer(config));
        if(fieldNameService.getFieldName(FieldKey.NAME) != null)
        	imb.put(fieldNameService.getFieldName(FieldKey.NAME), new NamePostNormalizer());
    	
    	return imb.build();

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
                solrBridge.commit();
            } catch (SolrServerException | IOException e) {
                if (e instanceof SocketTimeoutException || e.getCause() instanceof SocketTimeoutException) {
                    LOG.warn("Retrieved a timeout while waiting for building of autocompletion index to complete.", e);
                } else {
                    LOG.error("cannot commit:\n", e);
                }
            }
            shutdown();
        }
        time = (System.currentTimeMillis() - start);
        logStatistics();
    }

    protected void deleteAll() throws IOException, SolrServerException {
        LOG.info("Deleting original data...");
        solrBridge.getClient().deleteByQuery("*:*");
        solrBridge.commit();
        LOG.info("Deleting original data done.");
    }

    protected void processDataRoot(DataRoot dataRoot) throws SolrServerException, IOException, InterruptedException {
        LOG.info("Start of processing: " + dataRoot.getOriginName());
        if (dataRoot.deleteFirst()) {
            LOG.info("Deleting data for data provider: " + dataRoot.getOriginName());
            solrBridge.getClient().deleteByQuery(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER) + ":" + ClientUtils.escapeQueryChars(dataRoot.getOriginName()));
            LOG.info("Deleting data of provider done.");
        }

        // load mapping data provided by CLARIN's OAI-PMH harvester
        Map<String, EndpointDescription> directoryEndpointMap = HarvesterMap.loadEndpointMap(new File(dataRoot.getRootFile().getParent(), "map.csv"));

        // import files from every centre/endpoint within the data root
        for (List<File> centreFiles : getFilesFromDataRoot(dataRoot.getRootFile())) {
            processCentreFiles(centreFiles, dataRoot, directoryEndpointMap);
        }
        updateDaysSinceLastImport(dataRoot);
        LOG.info("End of processing: " + dataRoot.getOriginName());
    }

    protected void processCentreFiles(final List<File> centreFiles, final DataRoot dataRoot, final Map<String,EndpointDescription> directoryEndpointMap) throws IOException, SolrServerException, InterruptedException {
        LOG.info("Processing directory: {}", centreFiles.get(0).getParent());
        String centerDirName = centreFiles.get(0).getParentFile().getName();

        final ResourceStructureGraph resourceStructureGraph = instantiateResourceStructureGraph(dataRoot, centerDirName);
        final boolean createHierarchyGraph = resourceStructureGraph != null;

        // pre-process: identify mdSelfLinks and remove too large files from center file list
        LOG.info("Checking file list...");
        final Set<String> mdSelfLinkSet = Sets.newConcurrentHashSet();
        final Set<File> ignoredFileSet = Sets.newConcurrentHashSet();

        //(perform in thread pool)
        final AtomicInteger preProcessCount = new AtomicInteger();
        final Stream<Callable<Void>> preProcessors = centreFiles.stream().map((File file) -> {
            return (Callable) () -> {
                preProcessFile(file, createHierarchyGraph, ignoredFileSet, mdSelfLinkSet, preProcessCount);
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
                processCmdi(file, dataRoot, resourceStructureGraph, directoryEndpointMap.get(file.getParentFile().getName()));
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

    protected void preProcessFile(File file, boolean createHierarchyGraph, Set<File> ignoredFileSet, Set<String> mdSelfLinkSet, AtomicInteger progress) {
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

        //some counting and logging to indicate progress, especially helpful for large sets
        final int progressNow = progress.incrementAndGet();
        if (progressNow % 10000 == 0) {
            LOG.info("Pre-processed {} files in set...", progressNow);
        }
    }

    protected void purgeOldDocs() throws SolrServerException, IOException {
        LOG.info("Deleting old files that were not seen for more than " + config.getMaxDaysInSolr() + " days...");
        solrBridge.getClient().deleteByQuery(fieldNameService.getFieldName(FieldKey.LAST_SEEN) + ":[* TO NOW-" + config.getMaxDaysInSolr() + "DAYS]");
        LOG.info("Deleting old files done.");
    }

    protected void logStatistics() {
        LOG.info("Found {} file(s) without an id. (id is generated based on fileName but that may not be unique)", nrOfFilesWithoutId);
        LOG.info("Found {} file(s) with errors.", nrOfFilesWithError);
        LOG.info("Found {} file(s) too large.", nrOfFilesTooLarge);
        LOG.info("Skipped {} file(s) due to duplicate or problematic id.", nrOfFilesSkipped);
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
        LOG.info("Collecting files in data root {}", rootFile);
        List<List<File>> result = new ArrayList<>();
        if (rootFile.isFile()) {
            LOG.info("Data root {} is a singleton dataroot", rootFile);
            List<File> singleFileList = new ArrayList<>();
            singleFileList.add(rootFile);
            result.add(singleFileList);
        } else {
            File[] centerDirs = rootFile.listFiles();
            for (File centerDir : centerDirs) {
                LOG.info("Collecting files from centre directory {}", centerDir);
                List<File> centerFileList = new ArrayList<>();
                if (centerDir.isDirectory()) {
                    centerFileList.addAll(FileUtils.listFiles(centerDir, VALID_CMDI_EXTENSIONS, true));
                }

                if (!centerFileList.isEmpty()) {
                    LOG.info("Found {} candidates for import in {}", centerFileList.size(), centerDir);
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
     * @param resourceStructureGraph null to skip hierarchy processing
     * @param endpointDescription
     * @throws SolrServerException
     * @throws IOException
     */
    protected void processCmdi(File file, DataRoot dataOrigin, ResourceStructureGraph resourceStructureGraph, EndpointDescription endpointDescription) throws SolrServerException, IOException {
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
            if (!cmdiData.hasResources()) {
                nrOfFilesSkipped.incrementAndGet();
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
        if(endpointDescription != null) {
            if(endpointDescription.getOaiEndpointUrl() != null)
                solrDocument.addField(fieldNameService.getFieldName(FieldKey.OAI_ENDPOINT_URI), endpointDescription.getOaiEndpointUrl());
            if(endpointDescription.getNationalProject() != null)
                solrDocument.addField(fieldNameService.getFieldName(FieldKey.NATIONAL_PROJECT), endpointDescription.getNationalProject());
            if(endpointDescription.getCentreName() != null)
                solrDocument.addField(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER_NAME), endpointDescription.getCentreName());
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

    /**
     * Builds suggester index for autocompletion
     *
     * @throws SolrServerException
     * @throws MalformedURLException
     */
    private void buildSuggesterIndex() throws SolrServerException, MalformedURLException, IOException {
        LOG.info("Building index for autocompletion.");
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("qt", "/suggest");
        paramMap.put("suggest.build", "true");
        SolrParams params = new MapSolrParams(paramMap);
        solrBridge.getClient().query(params);
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
                doc.setField(fieldNameService.getFieldName(FieldKey.ID), Arrays.asList(vertex.getId()));

                if (vertex.getHierarchyWeight() != 0) {
                    final Map<String, Integer> partialUpdateMap = new HashMap<>();
                    partialUpdateMap.put("set", Math.abs(vertex.getHierarchyWeight()));
                    doc.setField(fieldNameService.getFieldName(FieldKey.HIERARCHY_WEIGHT), partialUpdateMap);
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
                    doc.setField(fieldNameService.getFieldName(FieldKey.HAS_PART), ImmutableMap.of("set", incomingVertexNames));
                    doc.setField(fieldNameService.getFieldName(FieldKey.HAS_PART_COUNT), ImmutableMap.of("set", incomingVertexNames.size()));

                    // add hasPartCount weight
                    final Double hasPartCountWeight = Math.log10(1 + Math.min(50, incomingVertexNames.size()));
                    doc.setField(fieldNameService.getFieldName(FieldKey.HAS_PART_COUNT_WEIGHT), ImmutableMap.of("set", hasPartCountWeight));
                }

                if (!outgoingVertexNames.isEmpty()) {
                    doc.setField(fieldNameService.getFieldName(FieldKey.IS_PART_OF), ImmutableMap.of("set", outgoingVertexNames));
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
    private void updateDaysSinceLastImport(final DataRoot dataRoot) throws SolrServerException, IOException {
        LOG.info("Updating \"days since last seen\" in Solr for: {}", dataRoot.getOriginName());
        final int fetchSize = 1000;

        final SolrQuery countQuery = createOldRecordsQuery(dataRoot);
        countQuery.setRows(0);

        final QueryResponse rsp = solrBridge.getClient().query(countQuery);
        final long totalResults = rsp.getResults().getNumFound();
        final LocalDate nowDate = LocalDate.now();

        final AtomicInteger updatedDocs = new AtomicInteger();
        int offset = 0;

        //create processors for updating days since last seen
        final Collection<Callable<Void>> processors = new ArrayList<>((int) Math.ceil(totalResults / fetchSize));
        while (offset < totalResults) {
            final int batchOffset = offset;
            processors.add(() -> {
                try {
                    performUpdateDaysSinceLastImportBatch(dataRoot, fetchSize, batchOffset, updatedDocs, nowDate);
                } catch (SolrServerException | IOException ex) {
                    LOG.error("Error while updating 'days since last seen' property for old records", ex);
                }
                return null;
            });
            offset += fetchSize;
        }

        //wait for processing to finish
        try {
            fileProcessingPool.invokeAll(processors);
        } catch (InterruptedException ex) {
            LOG.warn("Interrupted while waiting for termination of updating 'days since last seen' properties");
        }

        if (updatedDocs.get() > 0) {
            solrBridge.commit();
        }

        LOG.info("Updated \"days since last seen\" value in {} records.", updatedDocs.get());
    }

    private void performUpdateDaysSinceLastImportBatch(DataRoot dataRoot, final int fetchSize, int offset, AtomicInteger updatedDocs, final LocalDate nowDate) throws SolrServerException, IOException {
        int updatedInBatch = 0;
        final SolrQuery query = createOldRecordsQuery(dataRoot);
        query.setStart(offset);
        query.setRows(fetchSize);
        for (SolrDocument doc : solrBridge.getClient().query(query).getResults()) {
            updatedInBatch++;

            String recordId = (String) doc.getFieldValue(fieldNameService.getFieldName(FieldKey.ID));
            Date lastImportDate = (Date) doc.getFieldValue(fieldNameService.getFieldName(FieldKey.LAST_SEEN));
            LocalDate oldDate = lastImportDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long daysSinceLastSeen = DAYS.between(oldDate, nowDate);

            SolrInputDocument updateDoc = new SolrInputDocument();
            updateDoc.setField(fieldNameService.getFieldName(FieldKey.ID), recordId);

            final Map<String, Long> partialUpdateMap = Collections.singletonMap("set", daysSinceLastSeen);
            updateDoc.setField(fieldNameService.getFieldName(FieldKey.DAYS_SINCE_LAST_SEEN), partialUpdateMap);

            solrBridge.addDocument(updateDoc);
            final Throwable error = solrBridge.popError();
            if (error != null) {
                throw new SolrServerException(error);
            }
        }
        final int totalUpdated = updatedDocs.addAndGet(updatedInBatch);
        LOG.info("Updating \"days since last seen\": {} updated in batch - {} records updated thus far", updatedInBatch, totalUpdated);
    }

    private SolrQuery createOldRecordsQuery(DataRoot dataRoot) {
        final SolrQuery query = new SolrQuery();
        query.setQuery(
                //we're going to process all records in the current data root...
                fieldNameService.getFieldName(FieldKey.DATA_PROVIDER) + ":" + ClientUtils.escapeQueryChars(dataRoot.getOriginName())
                + " AND "
                // ...that have a "last seen" value _older_ than today (on update/initialisation all records get 0 so we can skip the rest)
                + fieldNameService.getFieldName(FieldKey.LAST_SEEN) + ":[* TO NOW-1DAY]"
        );
        query.setFields(fieldNameService.getFieldName(FieldKey.ID), fieldNameService.getFieldName(FieldKey.LAST_SEEN));
        return query;
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
            solrBridge.shutdown();
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
