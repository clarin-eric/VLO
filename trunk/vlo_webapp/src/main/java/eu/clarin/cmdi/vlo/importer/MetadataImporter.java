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
import org.xml.sax.SAXException;

import eu.clarin.cmdi.vlo.Configuration;

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

    private void startImport() {
        Map<String, File> originRootMap = new HashMap<String, File>(); //TODO PD can have multiple origins
        originRootMap.put("MPI corpora", new File("/Users/patdui/data/data/corpora/qfs1/media-archive/Corpusstructure/MPI.imdi.cmdi"));
        originRootMap.put("CORP-ORAL", new File(
                "/Users/patdui/data/data/corpora/qfs1/media-archive/CORP_ORAL/Corpusstructure/CORP_ORAL.imdi.cmdi"));
        originRootMap.put("DoBeS archive", new File(
                "/Users/patdui/data/data/corpora/qfs1/media-archive/dobes_data/Corpusstructure/dobes.imdi.cmdi"));
        originRootMap.put("ECHO", new File("/Users/patdui/data/data/corpora/qfs1/media-archive/echo_data/Corpusstructure/echo.imdi.cmdi"));
        originRootMap.put("DBD", new File("/Users/patdui/data/data/corpora/qfs1/media-archive/dbd_data/Corpusstructure/dbd.imdi.cmdi"));
        originRootMap.put("Sign Language", new File(
                "/Users/patdui/data/data/corpora/qfs1/media-archive/Corpusstructure/sign_language.imdi.cmdi"));
        originRootMap.put("Endagered Languages", new File(
                "/Users/patdui/data/data/corpora/qfs1/media-archive/Corpusstructure/endangered_languages.imdi.cmdi"));
        originRootMap.put("ANDES",
                new File("/Users/patdui/data/data/corpora/qfs1/media-archive/ANDES_data/Corpusstructure/ANDES.imdi.cmdi"));
        originRootMap.put("Leiden Archives", new File(
                "/Users/patdui/data/data/corpora/qfs1/media-archive/LeidenArchives/Corpusstructure/LeidenArchives.imdi.cmdi"));
        originRootMap.put("ILSP INTERA Contribution", new File(
                "/Users/patdui/data/data/corpora/qfs1/media-archive/ilsp_data/ILSP_INTERA.imdi.cmdi"));
        originRootMap.put("MPI für Bildungsforschung", new File(
                "/Users/patdui/data/data/corpora/qfs1/media-archive/Bildungsforschung/Corpusstructure/MPI_Bildungsforschung.imdi.cmdi"));
        originRootMap.put("Humanethologisches Filmarchiv", new File(
                "/Users/patdui/data/data/corpora/qfs1/media-archive/humanethology/Corpusstructure/humanethology.imdi.cmdi"));
        originRootMap.put("SUCA", new File("/Users/patdui/data/data/corpora/qfs1/media-archive/suca_data/Corpusstructure/suca.imdi.cmdi"));
        originRootMap.put("Nijmegen corpora of casual speech", new File(
                "/Users/patdui/data/data/corpora/qfs1/media-archive/casual_speech/Corpusstructure/casual_speech.imdi.cmdi"));

        //TODO This file is already added in the above list originRootMap.put("ESF corpus", new File("/Users/patdui/data/data/corpora/qfs1/media-archive/acqui_data/ac-ESF/Corpusstructure/esf.imdi.cmdi"));
//TODO PD these two do not exist in the dataset and ESF is different then what I find in the root cmdi file.
        //originRootMap.put("IFA corpus", new File("/Users/patdui/data/data/corpora/IFAcorpus/IMDI/IFAcorpus.imdi.cmdi"));
//        originRootMap
//        .put("CGN corpus", new File("/Users/patdui/data/data/corpora/qfs1/media-archive/NCGN/Corpusstructure/cgn.imdi.cmdi"));

        //        originRootMap.put("OLAC Metadata Providers", new File("/Users/patdui/data/olac/olac-cmdi-20101011/collection_root.cmdi")); 

        
        for (File file : originRootMap.values()) {
            if (!file.exists()) {
                LOG.error("Root file " + file + " does not exist. Probable configuration error so stopping import.");
                System.exit(1);
            }
        }


        // root file       File file = new File("/Users/patdui/data/data/corpora/qfs1/media-archive/Corpusstructure/MPI.imdi.cmdi");
        long start = System.currentTimeMillis();
        try {
            solrServer.deleteByQuery("*:*");//Delete the whole solr db.
            CMDIDigester digester = new CMDIDigester(config.getFacetMapping());
            for (String origin : originRootMap.keySet()) {
                processCmdi(originRootMap.get(origin), origin, digester);
            }
            if (!docs.isEmpty()) {
                sendDocs();
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
            solrDocument.addField("origin", origin);
            solrDocument.addField("id", cmdiData.getId());
            docs.add(solrDocument);
            if (docs.size() == 1000) {
                sendDocs();
            }
        }
    }

    private void sendDocs() throws SolrServerException, IOException {
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
