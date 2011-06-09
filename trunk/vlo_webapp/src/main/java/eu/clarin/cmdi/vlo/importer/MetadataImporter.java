package eu.clarin.cmdi.vlo.importer;

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

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.FacetConstants;

@SuppressWarnings("serial")
public class MetadataImporter {

    private static final String[] VALID_CMDI_EXTENSIONS = new String[] { "xml", "cmdi" };
    private final static Logger LOG = LoggerFactory.getLogger(MetadataImporter.class);
    private static Throwable serverError;
    private StreamingUpdateSolrServer solrServer;

    final static Map<String, PostProcessor> POST_PROCESSORS = new HashMap<String, PostProcessor>();
    static {
        POST_PROCESSORS.put(FacetConstants.FIELD_COUNTRY, new CountryNamePostProcessor());
        POST_PROCESSORS.put(FacetConstants.FIELD_LANGUAGE, new LanguageCodePostProcessor());
    }

    private Set<String> processedIds = new HashSet<String>();
    protected List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
    private final ImporterConfig config;

    private int nrOFDocumentsUpdated;
    private int nrOfFilesAnalyzed = 0;
    private int nrOfFilesWithoutId = 0;
    private int nrOfFilesWithoutDataResources = 0;
    private int nrOfFilesWithError = 0;

    public MetadataImporter(ImporterConfig config) {
        this.config = config;
    }

    void startImport() throws MalformedURLException {
        initSolrServer();
        List<DataRoot> dataRoots = checkDataRoots();
        long start = System.currentTimeMillis();
        try {
            if (config.isDeleteAllFirst()) {
                LOG.info("Deleting original data...");
                solrServer.deleteByQuery("*:*");//Delete the whole solr db.
                LOG.info("Deleting original data done.");
            }
            for (DataRoot dataRoot : dataRoots) {
                LOG.info("Start of processing: " + dataRoot.getOriginName());
                if (dataRoot.isDeleteFirst()) {
                    LOG.info("Deleting data for data provider: " + dataRoot.getOriginName());
                    solrServer.deleteByQuery(FacetConstants.FIELD_DATA_PROVIDER + ":" + dataRoot.getOriginName());
                    LOG.info("Deleting data of provider done.");
                }
                CMDIDataProcessor processor = new CMDIParserVTDXML(POST_PROCESSORS);
                List<File> files = getFilesFromDataRoot(dataRoot.getRootFile());
                for (File file : files) {
                    processCmdi(file, dataRoot.getOriginName(), processor);
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
                if (solrServer != null)
                    solrServer.commit();
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

    private List<DataRoot> checkDataRoots() {
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
     * 
     * @param rootFile
     * @return The rootFile if it is a file or when it is a directory the files in that directory
     */
    private List<File> getFilesFromDataRoot(File rootFile) {
        List<File> result = new ArrayList<File>();
        if (rootFile.isFile()) {
            result.add(rootFile);
        } else {
            Collection listFiles = FileUtils.listFiles(rootFile, VALID_CMDI_EXTENSIONS, true);
            result.addAll(listFiles);
        }
        return result;
    }

    protected void initSolrServer() throws MalformedURLException {
        String solrUrl = Configuration.getInstance().getSolrUrl();
        LOG.info("Initializing Solr Server on " + solrUrl);
        solrServer = new StreamingUpdateSolrServer(solrUrl, 1000, 2) {
            @Override
            public void handleError(Throwable ex) {
                super.handleError(ex);
                serverError = ex;
            }
        };
    }

    private void processCmdi(File file, String dataOrigin, CMDIDataProcessor processor) throws SolrServerException, IOException {
        nrOfFilesAnalyzed++;
        CMDIData cmdiData = null;
        try {
            cmdiData = processor.process(file);
            if (!idOk(cmdiData.getId())) {
                cmdiData.setId(dataOrigin + "/" + file.getName()); //No id found in the metadata file so making one up based on the file name. Not quaranteed to be unique, but we have to set something.
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

    private boolean idOk(String id) {
        return id != null && !id.isEmpty();
    }

    private void updateDocument(SolrInputDocument solrDocument, CMDIData cmdiData, File file, String dataOrigin) throws SolrServerException,
            IOException {
        if (!solrDocument.containsKey(FacetConstants.FIELD_ORIGIN)) {
            solrDocument.addField(FacetConstants.FIELD_ORIGIN, dataOrigin);
        }
        solrDocument.addField(FacetConstants.FIELD_DATA_PROVIDER, dataOrigin);
        solrDocument.addField(FacetConstants.FIELD_ID, cmdiData.getId());
        solrDocument.addField(FacetConstants.FIELD_FILENAME, file.getAbsolutePath());
        solrDocument.addField(FacetConstants.FIELD_COMPLETE_METADATA, "test"); // TODO: add the contents of the metadata file here

        addResourceData(solrDocument, cmdiData);
        docs.add(solrDocument);
        if (docs.size() == 1000) {
            sendDocs();
        }
    }

    /**
     * Adds two fields FIELD_RESOURCE_TYPE and FIELD_RESOURCE. The Type can be specified in the "ResourceType" element of an imdi file or
     * possibly overwritten by some more specific xpath (as in the LRT cmdi files). So if a type is overwritten and already in the
     * solrDocument we take that type.
     */
    private void addResourceData(SolrInputDocument solrDocument, CMDIData cmdiData) {
        List<Object> fieldValues = solrDocument.containsKey(FacetConstants.FIELD_RESOURCE_TYPE) ? new ArrayList(solrDocument
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
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BeanFactory factory = new ClassPathXmlApplicationContext(new String[] { Configuration.CONFIG_FILE, ImporterConfig.CONFIG_FILE });
        factory.getBean("configuration");
        ImporterConfig config = (ImporterConfig) factory.getBean("importerConfig", ImporterConfig.class);
        MetadataImporter importer = new MetadataImporter(config);
        importer.startImport();
        if (config.isPrintMapping()) {
            File file = new File("xsdMapping.txt");
            FacetMappingFactory.printMapping(file);
            LOG.info("Printed facetMapping in " + file);
        }
    }

}
