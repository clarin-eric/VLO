/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.batchimporter.parsing;

import com.ximpleware.ParseException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.batchimporter.InputProcessingException;
import eu.clarin.cmdi.vlo.batchimporter.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.MappingInput;
import java.io.IOException;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MetadataFileParser {

    public MappingInput parseFile(MetadataFile inputFile) throws InputProcessingException {
        log.info("Parsing input file {}", inputFile);
        final MappingInput.MappingInputBuilder builder = MappingInput.builder();
        try {
            builder.dataRoot(inputFile.getDataRoot());
            builder.sourcePath(inputFile.getLocation().toString());
            xmlParse(inputFile, builder);
            log.debug("Builder after parsing: {}", builder);
        } catch (IOException | VTDException ex) {
            throw new InputProcessingException(String.format("Error while trying to parse input file %s", inputFile), ex);
        }
        return builder.build();
    }

    private void xmlParse(MetadataFile inputFile, MappingInput.MappingInputBuilder builder) throws IOException, VTDException {
        final VTDGen vg = new VTDGen();
        //TODO: replace these two lines with `vg.parseFile()` call?
        vg.setDoc(Files.readAllBytes(inputFile.getLocation()));
        vg.parse(true);

        final VTDNav nav = vg.getNav();
        final String profileId = SchemaParsingUtil.extractProfileId(nav);
        builder.profileId(profileId);
        
        nav.toElement(VTDNav.ROOT);
    }
}
