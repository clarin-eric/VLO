/*
 * Copyright (C) 2024 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.wicket;

import static eu.clarin.cmdi.vlo.CmdConstants.CMDI_MEDIA_TYPE;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import java.io.File;
import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.request.RequestHandlerExecutor;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.handler.ErrorCodeRequestHandler;
import org.apache.wicket.util.resource.FileSystemResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * Request handlers that serves the CMDI content for a document (from the file system)
 * @author twagoo
 */
public class CmdiContentRequestHandler extends ResourceStreamRequestHandler {

    public CmdiContentRequestHandler(SolrDocument document, FieldNameService fieldNameService) {
        super(createResourceStream(document, fieldNameService));
    }

    private static IResourceStream createResourceStream(SolrDocument document, FieldNameService fieldNameService) {
        final Collection<Object> filenameValues = document.getFieldValues(fieldNameService.getFieldName(FieldKey.FILENAME));
        if (!filenameValues.isEmpty()) {
            final File cmdiFile = new File(filenameValues.iterator().next().toString());
            if (cmdiFile.exists()) {
                return new FileSystemResourceStream(cmdiFile) {
                    @Override
                    public String getContentType() {
                        return CMDI_MEDIA_TYPE;
                    }

                };
            }
        }
        throw new RequestHandlerExecutor.ReplaceHandlerException(new ErrorCodeRequestHandler(HttpServletResponse.SC_NOT_FOUND, "CMDI file not found"), true);
    }

}
