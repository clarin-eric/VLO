package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.DataRoot;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

/**
 * The main metadataImporter class. Also contains the main function.
 *
 * The metadataimporter reads all the config files and then, for each
 * metadatafile in each defined directory structure parses and imports them as
 * defined in the configuration. The startImport function starts the importing
 * and so on.
 */
@SuppressWarnings({"serial"})
public class MetadataImporter {

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
    final static Map<String, PostProcessor> POST_PROCESSORS = new HashMap<String, PostProcessor>();
    
    static {
        POST_PROCESSORS.put(FacetConstants.FIELD_ID, new IdPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_CONTINENT, new ContinentNamePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_COUNTRY, new CountryNamePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_LANGUAGE_CODE, new LanguageCodePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_AVAILABILITY, new AvailabilityPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_ORGANISATION, new OrganisationPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_TEMPORAL_COVERAGE, new TemporalCoveragePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_NATIONAL_PROJECT, new NationalProjectPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_CLARIN_PROFILE, new CMDIComponentProfileNamePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_RESOURCE_CLASS, new ResourceClassPostProcessor());
    }

    /**
     * Constructor
     */
    public MetadataImporter() {}

    /**
     * Contains MDSelflinks (usually). Just to know what we have already done.
     */
    protected final Set<String> processedIds = new HashSet<String>();
    /**
     * Some caching for solr documents (we are more efficient if we ram a whole
     * bunch to the solr server at once.
     */
    protected List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

    // SOME STATS
    protected int nrOFDocumentsSend;
    protected int nrOfFilesAnalyzed = 0;
    protected int nrOfFilesWithoutId = 0;
    protected int nrOfIgnoredFiles = 0;
    protected int nrOfFilesWithError = 0;

    /**
     * Retrieve all files with VALID_CMDI_EXTENSIONS from all DataRoot entries
     * and starts processing for every single file
     *
     * @throws MalformedURLException
     */
    void startImport() throws MalformedURLException {
        
        initSolrServer();
        List<DataRoot> dataRoots = checkDataRoots();
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
                CMDIDataProcessor processor = new CMDIParserVTDXML(POST_PROCESSORS, false);
                List<File> files = getFilesFromDataRoot(dataRoot.getRootFile());
                for (File file : files) {
                    if (config.getMaxFileSize() > 0
                            && file.length() > config.getMaxFileSize()) {
                        LOG.info("Skipping " + file.getAbsolutePath() + " because it is too large.");
                    } else {
                        LOG.debug("PROCESSING FILE: {}", file.getAbsolutePath());
                        processCmdi(file, dataRoot, processor);
                    }
                }
                if (!docs.isEmpty()) {
                    sendDocs();
                }
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
            } catch (SolrServerException e) {
                LOG.error("cannot commit:\n", e);
            } catch (IOException e) {
                LOG.error("cannot commit:\n", e);
            }
        }
        long took = (System.currentTimeMillis() - start) / 1000;
        LOG.info("Found " + nrOfFilesWithoutId + " file(s) without an id. (id is generated based on fileName but that may not be unique)");
        LOG.info("Found " + nrOfFilesWithError + " file(s) with errors.");
        LOG.info("Found " + nrOfIgnoredFiles
                + " file(s) that where ignored (files without resources or any link to a search service or landing page are ignored).");
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
        for (DataRoot dataRoot : dataRoots) {
            if (!dataRoot.getRootFile().exists()) {
                LOG.error("Root file " + dataRoot.getRootFile() + " does not exist. Probable configuration error so stopping import.");
                System.exit(1);
            }
        }
        return dataRoots;
    }

    /**
     * Get the rootFile or all files with VALID_CMDI_EXTENSIONS if rootFile is a
     * directory
     *
     * @param rootFile
     * @return List with the rootFile or all contained files if rootFile is a
     * directory
     */
    protected List<File> getFilesFromDataRoot(File rootFile) {
        List<File> result = new ArrayList<File>();
        if (rootFile.isFile()) {
            result.add(rootFile);
        } else {
            Collection<File> listFiles = FileUtils.listFiles(rootFile, VALID_CMDI_EXTENSIONS, true);
            result.addAll(listFiles);
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
        LOG.info("Initializing Solr Server on " + solrUrl);

        /* Specify the number of documents in the queue that will trigger the
         * threads, two of them, emptying it.
         */
        solrServer = new ConcurrentUpdateSolrServer(solrUrl,
                config.getMinDocsInSolrQueue(), 2) {
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
                    if (!cmdiData.getDataResources().isEmpty() || !cmdiData.getLandingPageResources().isEmpty()
                            || !cmdiData.getSearchResources().isEmpty() || !cmdiData.getSearchPageResources().isEmpty()
                            || cmdiData.getMetadataResources().isEmpty()) {
                        // We only add metadata files that have
                        //  1) data resources or
                        //	2) a landing page or
                        //	3) a search service (like SRU/CQL) or
                        //	4) a search page or 
                        //  5) that have none of the above but also lack any metadata links (e.g. olac files describing a corpus with a link to the original archive).
                        // Other files will have only metadata resources and are considered 'collection' metadata files they
                        // are usually not very interesting (think imdi corpus files) and will not be included.
                        updateDocument(solrDocument, cmdiData, file, dataOrigin);
                    } else {
                        nrOfIgnoredFiles++;
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
        List<Object> fieldValues = solrDocument.containsKey(FacetConstants.FIELD_FORMAT) ? new ArrayList<Object>(solrDocument
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
        docs = new ArrayList<SolrInputDocument>();
    }

    /**
     * Builds suggester index for autocompletion
     *
     * @throws SolrServerException
     * @throws MalformedURLException
     */
    private void buildSuggesterIndex() throws SolrServerException, MalformedURLException {
        LOG.info("Building index for autocompletion.");
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("qt", "/suggest");
        paramMap.put("spellcheck.build", "true");
        SolrParams params = new MapSolrParams(paramMap);
        solrServer.query(params);
    }
    
    public static VloConfig config;

    public static LanguageCodeUtils languageCodeUtils;
    
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

        /**
         * Add a "c" option, the option indicating the specification of an XML
         * configuration file
         */
        options.addOption("c", true, "-c <file> : use parameters specified in <file>");
        
        CommandLineParser parser = new PosixParser();
        
        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("c")) {

                // the "c" option was specified, now get its value
                configFile = cmd.getOptionValue("c");
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
            final XmlVloConfigFactory configFactory = new XmlVloConfigFactory(configUrl);
            MetadataImporter.config = configFactory.newConfig();
            MetadataImporter.languageCodeUtils = new LanguageCodeUtils(MetadataImporter.config);

            // optionally, modify the configuration here
            // create and start the importer
            MetadataImporter importer = new MetadataImporter();
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
