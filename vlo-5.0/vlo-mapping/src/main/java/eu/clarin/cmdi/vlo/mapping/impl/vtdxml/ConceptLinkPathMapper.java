package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import java.util.Map;

import com.ximpleware.NavException;
import eu.clarin.cmdi.vlo.mapping.model.Context;

public interface ConceptLinkPathMapper {

    Map<String, Context> createConceptLinkPathMapping(String profileId) throws NavException;

}
