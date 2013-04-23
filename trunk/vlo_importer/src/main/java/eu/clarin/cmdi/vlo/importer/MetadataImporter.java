package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.DataRoot;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
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
     * Defines which files to try and parse.
     * In this case all files ending in "xml" or "cmdi".
     */
    private static final String[] VALID_CMDI_EXTENSIONS = new String[] { "xml", "cmdi" };

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
    private StreamingUpdateSolrServer solrServer;
    /**
     * Defines the post-processor associations. At import, for each facet value,
     * this map is checked and all postprocessors associated with the facet
     * _type_ are applied to the value before storing the new value in the solr
     * document.
     */
    final static Map<String, PostProcessor> POST_PROCESSORS = new HashMap<String, PostProcessor>();
    static {
        POST_PROCESSORS.put(FacetConstants.FIELD_COUNTRY, new CountryNamePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_LANGUAGE, new LanguageCodePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_RESOURCE_TYPE, new ResourceTypePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_LANGUAGES, new LanguageLinkPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_NATIONAL_PROJECT, new NationalProjectPostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_CLARIN_PROFILE, new CMDIComponentProfileNamePostProcessor());
    }
    
    /**
     * Constructor
     * 
     * @param
     */
    public MetadataImporter (){
    }

    /**
     * Contains MDSelflinks (usually).
     * Just to know what we have already done.
     */
    protected final Set<String> processedIds = new HashSet<String>();
    /**
     * Some caching for solr documents (we are more efficient if we ram a whole
     * bunch to the solr server at once.
     */
    protected List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

    // SOME STATS
    protected int nrOFDocumentsUpdated;
    protected int nrOfFilesAnalyzed = 0;
    protected int nrOfFilesWithoutId = 0;
    protected int nrOfFilesWithoutDataResources = 0;
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
            if (VloConfig.deleteAllFirst()) {
                LOG.info("Deleting original data...");
                solrServer.deleteByQuery("*:*");
                solrServer.commit();
                LOG.info("Deleting original data done.");
            }
            for (DataRoot dataRoot : dataRoots) {
                LOG.info("Start of processing: " + dataRoot.getOriginName());
                if (dataRoot.deleteFirst()) {
                    LOG.info("Deleting data for data provider: " + dataRoot.getOriginName());
                    solrServer.deleteByQuery(FacetConstants.FIELD_DATA_PROVIDER + ":" + ClientUtils.escapeQueryChars(dataRoot.getOriginName()));
                    LOG.info("Deleting data of provider done.");
                }
                CMDIDataProcessor processor = new CMDIParserVTDXML(POST_PROCESSORS);
                List<File> files = getFilesFromDataRoot(dataRoot.getRootFile());
                for (File file : files) {
                    if (VloConfig.getUseMaxFileSize() && 
                            file.length() > VloConfig.getMaxFileSize()) {
                        LOG.info("Skipping " + file.getAbsolutePath() + " because it is too large.");
                    } else {
                        LOG.debug("PROCESSING FILE: " + file.getAbsolutePath());
                        processCmdi(file, dataRoot, processor);
                    }
                }
                if (!docs.isEmpty()) {
                    sendDocs();
                }
                LOG.info("End of processing: " + dataRoot.getOriginName());
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
        LOG.info("Found " + nrOfFilesWithoutDataResources
                + " file(s) without data resources (metadata descriptions without resources are ignored).");
        LOG.info("Update of " + nrOFDocumentsUpdated + " took " + took + " secs. Total nr of files analyzed " + nrOfFilesAnalyzed);
    }

    /**
     * Check a List of DataRoots for existence of RootFile (typically parent
     * directory of metadata files)
     *
     * @return
     */
    protected List<DataRoot> checkDataRoots() {
        List<DataRoot> dataRoots = VloConfig.getDataRoots();
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
     * Initialize SolrServer as specified in configuration file
     *
     * @throws MalformedURLException
     */
    protected void initSolrServer() throws MalformedURLException {
        String solrUrl = VloConfig.getSolrUrl();
        LOG.info("Initializing Solr Server on " + solrUrl);
        solrServer = new StreamingUpdateSolrServer(solrUrl, 1000, 2) {
            @Override
            public void handleError(Throwable ex) {
                super.handleError(ex);
                serverError = ex;
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
            LOG.error("error in file: " + file + " Exception", e);
            nrOfFilesWithError++;
        }
        if (cmdiData != null && processedIds.add(cmdiData.getId())) {
            SolrInputDocument solrDocument = cmdiData.getSolrDocument();
            if (solrDocument != null) {
                if (!cmdiData.getDataResources().isEmpty() || cmdiData.getMetadataResources().isEmpty()) {
                    // We only add metadata files that have data resources (1) or files that don't link to other metadata files (2):
                    //  1) files with data resources are obviously interesting
                    //  2) files without metadata links and without dataResource can be interesting e.g. olac files describing a corpus with a link to the original archive.
                    // Other files will have only metadata resources and are considered 'collection' metadata files they
                    // are usually not very interesting (think imdi corpus files) and will not be included.
                    updateDocument(solrDocument, cmdiData, file, dataOrigin);
                } else {
                    nrOfFilesWithoutDataResources++;
                }
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
        //System.out.println(dataOrigin.getTostrip());
        //System.out.println(dataOrigin.getTostrip().length());
        //System.out.println(file.getAbsolutePath());
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
        
        addResourceData(solrDocument, cmdiData);
        docs.add(solrDocument);
        if (docs.size() == VloConfig.getMaxOnHeap()) {
            sendDocs();
        }
    }

    /**
     * Adds two fields FIELD_RESOURCE_TYPE and FIELD_RESOURCE. The Type can be
     * specified in the "ResourceType" element of an imdi file or possibly
     * overwritten by some more specific xpath (as in the LRT cmdi files). So if
     * a type is overwritten and already in the solrDocument we take that type.
     */
    protected void addResourceData(SolrInputDocument solrDocument, CMDIData cmdiData) {
        List<Object> fieldValues = solrDocument.containsKey(FacetConstants.FIELD_RESOURCE_TYPE) ? new ArrayList<Object>(solrDocument
                .getFieldValues(FacetConstants.FIELD_RESOURCE_TYPE)) : null;
        solrDocument.removeField(FacetConstants.FIELD_RESOURCE_TYPE); //Remove old values they might be overwritten.
        List<Resource> resources = cmdiData.getDataResources();
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            String mimeType = resource.getMimeType();
            String resourceType = mimeType;
            if (mimeType == null) {
                if (fieldValues != null && i < fieldValues.size()) {
                    resourceType = fieldValues.get(i).toString(); //assuming there will be as many resource types overwritten as there are specified
                    mimeType = CommonUtils.normalizeMimeType(resourceType);
                } else {
                    mimeType = CommonUtils.normalizeMimeType("");
                    resourceType = mimeType;
                }
            } else {
                resourceType = CommonUtils.normalizeMimeType(mimeType);
            }
            solrDocument.addField(FacetConstants.FIELD_RESOURCE_TYPE, resourceType);
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
        LOG.info("Sending " + docs.size() + " docs to solr server. Total number of docs updated till now: " + nrOFDocumentsUpdated);
        nrOFDocumentsUpdated += docs.size();
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
    	HashMap<String,String> paramMap = new HashMap<String, String>();
    	paramMap.put("qt", "/suggest");
    	paramMap.put("spellcheck.build", "true");
    	SolrParams params = new MapSolrParams(paramMap);
    	solrServer.query(params);
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws MalformedURLException, IOException { 

        // application configuration
        VloConfig config;
        
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
        
        if (configFile == null){

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
            VloConfig.readConfig(configFile);

            // optionally, modify the configuration here

            // create and start the importer
            MetadataImporter importer = new MetadataImporter();
            importer.startImport();

            // finished importing

            if (VloConfig.printMapping()) {
                File file = new File("xsdMapping.txt");
                FacetMappingFactory.printMapping(file);
                LOG.info("Printed facetMapping in " + file);
            }
        }
    }
}