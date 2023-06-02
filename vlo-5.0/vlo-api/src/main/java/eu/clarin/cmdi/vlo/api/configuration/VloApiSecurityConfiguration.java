/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 *
 * @author twagoo
 */
@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class VloApiSecurityConfiguration {

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                // Demonstrate that method security works
                // Best practice to use both for defense in depth
                .authorizeExchange(exchanges
                        -> exchanges
                        .pathMatchers(HttpMethod.POST, "/records")
                        .hasAnyRole("ADMIN")
                        .anyExchange().permitAll()
                )
                .httpBasic(withDefaults())
                .formLogin().disable()
                .csrf().disable()
                .logout().disable()
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        //TODO: switcht to user builder that is fit for production
        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
        UserDetails admin = userBuilder.username("admin")
                .password("admin")
                .roles("USER", "ADMIN")
                .build();
        return new MapReactiveUserDetailsService(admin);
    }
}
