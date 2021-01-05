package eu.clarin.cmdi.vlo.monitor.util;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PreemptiveAuthInterceptor implements HttpRequestInterceptor {
    
    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
        // If no auth scheme available yet, try to initialize it
        // preemptively
        if (authState.getAuthScheme() == null) {
            CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);
            Credentials creds = credsProvider.getCredentials(AuthScope.ANY);
            if (creds == null) {
                throw new HttpException("No credentials for preemptive authentication");
            }
            authState.update(new BasicScheme(), creds);
        }
    }
    
}
