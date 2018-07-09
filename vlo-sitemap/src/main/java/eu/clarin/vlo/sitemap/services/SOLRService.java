package eu.clarin.vlo.sitemap.services;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.vlo.sitemap.gen.Config;
import eu.clarin.vlo.sitemap.pojo.Sitemap.URL;
import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.commons.io.FileUtils;

public class SOLRService {

    public static final Logger LOGGER = LoggerFactory.getLogger(SOLRService.class);

    //to increase performances, should be increased in future
    static final int MAX_NUM_OF_RECORDS = 1500000; //1M
    static final String GET_IDS = "wt=xml&fl=id&rows=";

    private VTDGen vg;

    public SOLRService() {
        vg = new VTDGen();
    }

    public List<URL> getRecordURLS() throws VTDException, MalformedURLException, IOException {
        final java.net.URL url = new java.net.URL(Config.SOLR_QUERY_URL + GET_IDS + MAX_NUM_OF_RECORDS);

        //TODO: paginate
        // download result into temp file, because parseHttpUrl() doesn't support HTTP Basic Auth
        File tmpFile = File.createTempFile("vlo_sitemap-", ".tmp");
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Config.SOLR_USER, Config.SOLR_PASS.toCharArray());
            }
        });
        FileUtils.copyURLToFile(url, tmpFile);
        final boolean parseSuccess = vg.parseFile(tmpFile.getAbsolutePath(), false);
        tmpFile.delete();

        if (!parseSuccess) {
            throw new RuntimeException("Error retrieving or parsing result from: " + url);
        }

        final VTDNav nav = vg.getNav();
        final AutoPilot ap = new AutoPilot(nav);

        ap.selectXPath("//response/result/doc/str");

        final List<URL> ids = new ArrayList<URL>(MAX_NUM_OF_RECORDS);
        int i = -1;
        while ((i = ap.evalXPath()) != -1) {
            String id = nav.toNormalizedString(nav.getText());
            ids.add(new URL(Config.RECORD_URL_TEMPLATE + id));
        }

        return ids;

    }

}
