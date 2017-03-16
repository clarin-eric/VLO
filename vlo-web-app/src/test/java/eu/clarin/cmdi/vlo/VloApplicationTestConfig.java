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
package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloApplicationSpringConfig;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloConfigFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Extension of {@link VloApplicationSpringConfig} that returns the
 * {@link DefaultVloConfigFactory} from {@link #vloConfigFactory() }
 *
 * @author twagoo
 */
@Configuration
public class VloApplicationTestConfig extends VloApplicationSpringConfig {

    @Override
    public VloConfigFactory vloConfigFactory() {
        return new DefaultVloConfigFactory();
    }

    @Override
    public VloConfig vloConfig() {
        return DefaultVloConfigFactory.configureDefaultMappingLocations(super.vloConfig());
    }
}
