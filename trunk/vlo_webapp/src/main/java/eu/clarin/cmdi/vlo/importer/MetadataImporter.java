package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.dao.FacetConstants;

@SuppressWarnings("serial")
public final class MetadataImporter {

    private final static Logger LOG = LoggerFactory.getLogger(MetadataImporter.class);
    private static Throwable serverError;
    private final StreamingUpdateSolrServer solrServer;

    private Set<String> processedIds = new HashSet<String>();
    private List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
    private final ImporterConfig config;

    private int nrOFDocumentsUpdated;
    private int nrOfNonExistendResourceFiles = 0;
    private int nrOfFilesAnalyzed = 0;
    private int nrOfFilesWithoutId = 0;

    public MetadataImporter(ImporterConfig config) throws MalformedURLException {
        this.config = config;
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

    //TODO PD can have multiple origins
    private void startImport() {
        List<DataRoot> dataRoots = config.getDataRoots();
        for (DataRoot dataRoot : dataRoots) {
            if (!dataRoot.getRootFile().exists()) {
                LOG.error("Root file " + dataRoot.getRootFile() + " does not exist. Probable configuration error so stopping import.");
                System.exit(1);
            }
        }

        long start = System.currentTimeMillis();
        try {
            if (config.isDeleteFirst()) {
                LOG.info("Deleting original data...");
                solrServer.deleteByQuery("*:*");//Delete the whole solr db.
                LOG.info("Deleting original data done.");
            }
            for (DataRoot dataRoot : dataRoots) {
                LOG.info("Start of processing: "+dataRoot.getOriginName());
                CMDIDigester digester = new CMDIDigester(dataRoot.getFacetMapping());
                processCmdi(dataRoot.getRootFile(), dataRoot.getOriginName(), digester);
                if (!docs.isEmpty()) {
                    sendDocs();
                }
                LOG.info("End of processing: "+dataRoot.getOriginName());
            }
        } catch (SolrServerException e) {
            LOG.error("error updating files:\n", e);
            LOG.error("Also see vlo_solr server logs for more information");
        } catch (IOException e) {
            LOG.error("error updating files:\n", e);
        } finally {
            try {
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

    private void processCmdi(File file, String origin, CMDIDigester digester) throws SolrServerException, IOException {
        nrOfFilesAnalyzed++;
        CMDIData cmdiData = null;
        try {
            cmdiData = digester.process(file);
        } catch (IOException e) {
            LOG.error("error in file: " + file + " Exception", e);
        } catch (SAXException e) {
            LOG.error("error in file: " + file + " Exception", e);
        } catch (XPathExpressionException e) {
            LOG.error("error in file: " + file + " Exception", e);
        }
        if (cmdiData != null && processedIds.add(cmdiData.getId())) {
            SolrInputDocument solrDocument = cmdiData.getSolrDocument();
            if (solrDocument != null) {
                updateDocument(solrDocument, cmdiData, file, origin);
            }
            List<String> resources = cmdiData.getResources();
            for (String cmdiResource : resources) {
                File resourceFile = new File(file.getParentFile(), cmdiResource);
                if (resourceFile.exists()) {
                    processCmdi(resourceFile, origin, digester);
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
            solrDocument.addField(FacetConstants.FIELD_ID, cmdiData.getId());
            solrDocument.addField(FacetConstants.FIELD_FILENAME, file.toString());
            docs.add(solrDocument);
            if (docs.size() == 1000) {
                sendDocs();
            }
        }
    }

    private void sendDocs() throws SolrServerException, IOException {
        LOG.info("Sending "+docs.size()+" docs to solr server. Total number of docs updated till now: "+nrOFDocumentsUpdated);
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
        BeanFactory factory = new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml", "importerConfig.xml" });
        factory.getBean("configuration");
        ImporterConfig config = (ImporterConfig) factory.getBean("importerConfig", ImporterConfig.class);
        MetadataImporter importer = new MetadataImporter(config);
        importer.startImport();
    }

}
