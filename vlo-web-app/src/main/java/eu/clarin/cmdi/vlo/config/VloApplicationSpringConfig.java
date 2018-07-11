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
package eu.clarin.cmdi.vlo.config;

import eu.clarin.cmdi.vlo.VloWicketApplication;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main VLO web application beans
 *
 * @author twagoo
 */
@Configuration
public class VloApplicationSpringConfig {
    
    @Bean
    public FieldNameService fieldNameService() {
        return new FieldNameServiceImpl(vloConfig());
    }

    @Bean
    public VloConfig vloConfig() {
        try {
            return vloConfigFactory().newConfig();
        } catch (IOException ex) {
            throw new RuntimeException("Could not read VLO configuration", ex);
        }
    }

    @Bean
    public VloConfigFactory vloConfigFactory() {
        return new ServletVloConfigFactory();
    }
    
    /**
    *
    * @return the web application object that represents the Wicket application
    */
   @Bean
   public VloWicketApplication webApplication() {
       return new VloWicketApplication();
   }

}
