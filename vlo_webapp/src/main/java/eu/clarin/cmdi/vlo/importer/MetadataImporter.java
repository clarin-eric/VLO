package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private int nrOfNonExistendResourceFiles = 0;
    private int nrOfFilesAnalyzed = 0;
    private int nrOfFilesWithoutId = 0;

    public MetadataImporter(ImporterConfig config) {
        this.config = config;
    }

    void startImport() throws MalformedURLException {
        initSolrServer();
        List<DataRoot> dataRoots = config.getDataRoots();
        for (DataRoot dataRoot : dataRoots) {
            if (!dataRoot.getRootFile().exists()) {
                LOG.error("Root file " + dataRoot.getRootFile() + " does not exist. Probable configuration error so stopping import.");
                System.exit(1);
            }
        }

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
                    LOG.info("Deleting data for origin: " + dataRoot.getOriginName());
                    solrServer.deleteByQuery(FacetConstants.FIELD_ORIGIN + ":" + dataRoot.getOriginName());
                    LOG.info("Deleting data for origin done.");
                }
                CMDIDataProcessor processor = new CMDIParserVTDXML(dataRoot.getFacetMapping(), POST_PROCESSORS);
                processCmdi(dataRoot.getRootFile(), dataRoot.getOriginName(), processor);
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
        LOG.info("Found " + nrOfNonExistendResourceFiles + " non existing resources files.");
        LOG.info("Found " + nrOfFilesWithoutId + " file(s) without an id.");
        LOG.info("Update of " + nrOFDocumentsUpdated + " took " + took + " secs. Total nr of files analyzed " + nrOfFilesAnalyzed);
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

    private void processCmdi(File file, String origin, CMDIDataProcessor processor) throws SolrServerException, IOException {
        nrOfFilesAnalyzed++;
        CMDIData cmdiData = null;
        try {
            cmdiData = processor.process(file);
        } catch (Exception e) {
            LOG.error("error in file: " + file + " Exception", e);
        }
        if (cmdiData != null && processedIds.add(cmdiData.getId())) {
            SolrInputDocument solrDocument = cmdiData.getSolrDocument();
            if (solrDocument != null) {
                updateDocument(solrDocument, cmdiData, file, origin);
            }
            List<Resource> resources = cmdiData.getMetadataResources();
            for (Resource cmdiResource : resources) {
                File resourceFile = new File(file.getParentFile(), cmdiResource.getResourceName());
                if (resourceFile.exists()) {
                    processCmdi(resourceFile, origin, processor);
                } else {
                    nrOfNonExistendResourceFiles++;
                    LOG.error("Found nonexistent resource file (" + cmdiResource + ") in cmdi: " + file);
                }
            }
        }
    }

    private void updateDocument(SolrInputDocument solrDocument, CMDIData cmdiData, File file, String origin) throws SolrServerException,
            IOException {
        if (cmdiData.getId() == null || cmdiData.getId().isEmpty()) {
            nrOfFilesWithoutId++;
            LOG.info("Ignoring document without id, fileName: " + file);
        } else {
            solrDocument.addField(FacetConstants.FIELD_ORIGIN, origin);
            solrDocument.addField(FacetConstants.FIELD_DATA_ROOT, origin);
            solrDocument.addField(FacetConstants.FIELD_ID, cmdiData.getId());
            solrDocument.addField(FacetConstants.FIELD_FILENAME, file.getAbsolutePath());
            List<Resource> resources = cmdiData.getDataResources();
            for (Resource resource : resources) {
                String mimeType = resource.getMimeType();
                if (mimeType == null) {
                    mimeType = "unknownType";
                }
                solrDocument.addField(FacetConstants.FIELD_RESOURCE_TYPE, CommonUtils.normalizeMimeType(mimeType));
                solrDocument.addField(FacetConstants.FIELD_RESOURCE, mimeType + "," + resource.getResourceName());
            }
            docs.add(solrDocument);
            if (docs.size() == 1000) {
                sendDocs();
            }
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
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws MalformedURLException {
        BeanFactory factory = new ClassPathXmlApplicationContext(new String[] { Configuration.CONFIG_FILE, ImporterConfig.CONFIG_FILE });
        factory.getBean("configuration");
        ImporterConfig config = (ImporterConfig) factory.getBean("importerConfig", ImporterConfig.class);
        MetadataImporter importer = new MetadataImporter(config);
        importer.startImport();
    }

}
