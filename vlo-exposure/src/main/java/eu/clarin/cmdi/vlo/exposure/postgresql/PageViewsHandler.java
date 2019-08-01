package eu.clarin.cmdi.vlo.exposure.postgresql;

import java.util.HashMap;
import java.util.List;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.PageView;

public interface PageViewsHandler {

	public boolean addPageView(VloConfig vloConfig, PageView pv);
}
