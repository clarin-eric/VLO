package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;
import org.apache.solr.common.SolrInputDocument;

public class MetadataMapper {

    /**
     * Log log log log
     */
    protected final static Logger LOG = LoggerFactory.getLogger(MetadataImporter.class);

    /**
     * @param args
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void main(String[] args) throws MalformedURLException, IOException {

        try {

            // path to the configuration file
            String configFile = null;

            // path to the record file
            String recordFile = null;

            // use the Apache cli framework for getting command line parameters
            Options options = new Options();
            options.addOption("c", true, "-c <file> : use parameters specified in <file>");
            options.addOption("r", true, "-r <file> : process CMD record in <file>.");

            CommandLineParser parser = new PosixParser();

            try {
                // parse the command line arguments
                CommandLine cmd = parser.parse(options, args);
                if (cmd.hasOption("c")) {

                    // the "c" option was specified, now get its value
                    configFile = cmd.getOptionValue("c");
                }

                if (cmd.hasOption("r")) {
                    recordFile = cmd.getOptionValue("r");
                }

            } catch (org.apache.commons.cli.ParseException ex) {
                /**
                 * Caught an exception caused by command line parsing. Try to
                 * get the name of the configuration file by querying the system
                 * property.
                 */
                String message = "Command line parsing failed. " + ex.getMessage();
                LOG.error(message);
                System.err.println(message);
            }

            if (configFile == null) {
                String message = "Could not get config file name via the command line, trying the system properties.";
                LOG.info(message);
                String key;
                key = "configFile";
                configFile = System.getProperty(key);
            }

            if (configFile == null) {
                String message = "Could not get filename as system property either - stopping.";
                LOG.error(message);
                System.exit(1);
            }

            // read the configuration from the externally supplied file
            final URL configUrl;
            if (configFile.startsWith("file:")) {
                configUrl = new URL(configFile);
            } else {
                configUrl = new File(configFile).toURI().toURL();
            }
            LOG.info("Reading configuration from " + configUrl.toString());

            final XmlVloConfigFactory configFactory = new XmlVloConfigFactory(configUrl);
            final VloConfig config = configFactory.newConfig();
            config.setPrintMapping(true);
            final LanguageCodeUtils languageCodeUtils = new LanguageCodeUtils(config);
            final VLOMarshaller marshaller = new VLOMarshaller();
            final FacetMappingFactory facetMappingFactory = new FacetMappingFactory(config, marshaller);

            final FieldNameService fieldNameService = new FieldNameServiceImpl(config);
            final CMDIDataSolrImplFactory cmdiDataFactory = new CMDIDataSolrImplFactory(fieldNameService);
            CMDIDataProcessor<SolrInputDocument> processor = new CMDIParserVTDXML(
                    MetadataImporter.registerPostProcessors(config, fieldNameService, languageCodeUtils),
                    MetadataImporter.registerPostMappingFilters(fieldNameService),
                    config, facetMappingFactory, marshaller, cmdiDataFactory, fieldNameService, false);

            if (recordFile == null) {
                String message = "Could not get record filename - stopping.";
                LOG.error(message);
                System.exit(1);
            }

            File record = new File(recordFile);

            CMDIData<SolrInputDocument> cmdiData = processor.process(record, new ResourceStructureGraph());

            for (String field : cmdiData.getDocument().getFieldNames()) {
                System.out.println(cmdiData.getDocument().getField(field));
            }

            // finished importing
            if (config.printMapping()) {
                File file = new File("xsdMapping.txt");
                facetMappingFactory.printMapping(file);
                LOG.info("Printed facetMapping in " + file);
            }
        } catch (Exception ex) {
            LOG.error("FATAL!", ex);
            System.exit(1);
        }
    }
}
