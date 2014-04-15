/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.service.handle.impl;

import eu.clarin.cmdi.vlo.service.handle.impl.HandleRestApiClient;
import eu.clarin.cmdi.vlo.service.handle.HandleClient;

/**
 * Test runner for the HandleClient implementation
 *
 * @author twagoo
 */
public class HandleRestApiClientRunner {

    public final static String HANDLE = "1839/00-0000-0000-0000-0000-4";
    public final static String EXPECTED_URL = "http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final HandleClient client = new HandleRestApiClient();

        System.out.println("Requesting handle...");
        final String result = client.getUrl(HANDLE);

        System.out.println(String.format("%s -> %s", HANDLE, result));

        if (EXPECTED_URL.equals(result)) {
            System.out.println("Success");
        } else {
            System.err.println("!!! Unexpected result !!!\n\n" + result);
        }

    }

}
