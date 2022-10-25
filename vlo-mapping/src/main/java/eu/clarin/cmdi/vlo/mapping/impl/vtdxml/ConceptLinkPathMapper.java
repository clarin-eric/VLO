package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import java.util.List;
import java.util.Map;

import com.ximpleware.NavException;

public interface ConceptLinkPathMapper {

    Map<String, List<Pattern>> createConceptLinkPathMapping(String profileId) throws NavException;

}
