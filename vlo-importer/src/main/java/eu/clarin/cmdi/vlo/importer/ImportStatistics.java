/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.importer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ImportStatistics {
    
    private final AtomicInteger nrOFDocumentsSent = new AtomicInteger();
    private final AtomicInteger nrOfFilesAnalyzed = new AtomicInteger();
    private final AtomicInteger nrOfFilesSkipped = new AtomicInteger();
    private final AtomicInteger nrOfFilesWithoutId = new AtomicInteger();
    private final AtomicInteger nrOfFilesWithError = new AtomicInteger();
    private final AtomicInteger nrOfFilesTooLarge = new AtomicInteger();

    /**
     * @return the nrOFDocumentsSent
     */
    public AtomicInteger nrOFDocumentsSent() {
        return nrOFDocumentsSent;
    }

    /**
     * @return the nrOfFilesAnalyzed
     */
    public AtomicInteger nrOfFilesAnalyzed() {
        return nrOfFilesAnalyzed;
    }

    /**
     * @return the nrOfFilesSkipped
     */
    public AtomicInteger nrOfFilesSkipped() {
        return nrOfFilesSkipped;
    }

    /**
     * @return the nrOfFilesWithoutId
     */
    public AtomicInteger nrOfFilesWithoutId() {
        return nrOfFilesWithoutId;
    }

    /**
     * @return the nrOfFilesWithError
     */
    public AtomicInteger nrOfFilesWithError() {
        return nrOfFilesWithError;
    }

    /**
     * @return the nrOfFilesTooLarge
     */
    public AtomicInteger nrOfFilesTooLarge() {
        return nrOfFilesTooLarge;
    }
    
}
