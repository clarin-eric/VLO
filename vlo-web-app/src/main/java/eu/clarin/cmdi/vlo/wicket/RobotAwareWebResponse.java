/*
 * Copyright (C) 2017 CLARIN
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

import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.http.WebRequest;

/**
 * Servlet Web Response extension that returns "raw" URLs in case the agent is
 * recognised to be a bot/crawler/spider.
 *
 * This is an adaptation of the solution proposed at
 * {@link https://cwiki.apache.org/confluence/display/WICKET/SEO+-+Search+Engine+Optimization#SEO-SearchEngineOptimization-RemovingThejessionid}
 *
 * @author twagoo
 */
public class RobotAwareWebResponse extends ServletWebResponse {

    private static final Pattern BOT_AGENTS = Pattern.compile(".*(curl|crawl|spider|Googlebot|Bingbot|msnbot|Slurp|DuckDuckBot|Baiduspider|YandexBot|ia_archiver|facebot).*", Pattern.CASE_INSENSITIVE);
    private final WebRequest webRequest;

    public RobotAwareWebResponse(ServletWebRequest webRequest, HttpServletResponse httpServletResponse) {
        super(webRequest, httpServletResponse);
        this.webRequest = webRequest;
    }

    @Override
    public String encodeURL(CharSequence url) {
        return isRobot(webRequest) ? url.toString() : super.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(CharSequence url) {
        return isRobot(webRequest) ? url.toString() : super.encodeRedirectURL(url);
    }

    private boolean isRobot(WebRequest request) {
        final String agent = webRequest.getHeader("User-Agent");
        return agent != null && BOT_AGENTS.matcher(agent).matches();
    }

}
