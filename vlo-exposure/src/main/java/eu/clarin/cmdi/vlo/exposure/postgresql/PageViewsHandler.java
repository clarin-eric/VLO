package eu.clarin.cmdi.vlo.exposure.postgresql;

import java.util.HashMap;
import java.util.List;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.PageView;
import eu.clarin.cmdi.vlo.exposure.models.Record;

public interface PageViewsHandler {

    boolean addPageView(VloConfig vloConfig, PageView pv);
    List<Record> getStats(VloConfig vloConfig, QueryParameters qp);
    HashMap<String,String> getStatByRecordId(VloConfig vloConfig, QueryParameters qp);
    HashMap<String,Integer> getKeyWordsByRecordId(VloConfig vloConfig, QueryParameters qp);

}
