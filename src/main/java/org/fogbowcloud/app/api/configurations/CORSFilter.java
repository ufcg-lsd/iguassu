package org.fogbowcloud.app.api.configurations;

import static org.fogbowcloud.app.api.constants.CORSProperties.ALLOWED_CLIENT_FRONT_LOCAL;
import static org.fogbowcloud.app.api.constants.CORSProperties.ALLOWED_CREDENTIALS_HEADER;
import static org.fogbowcloud.app.api.constants.CORSProperties.ALLOWED_CREDENTIALS_HEADER_VALUE;
import static org.fogbowcloud.app.api.constants.CORSProperties.ALLOWED_HEADERS_HEADER;
import static org.fogbowcloud.app.api.constants.CORSProperties.ALLOWED_HEADERS_HEADER_VALUES;
import static org.fogbowcloud.app.api.constants.CORSProperties.ALLOWED_METHODS_HEADER;
import static org.fogbowcloud.app.api.constants.CORSProperties.ALLOWED_METHODS_HEADER_VALUE;
import static org.fogbowcloud.app.api.constants.CORSProperties.ALLOWED_ORIGINS_HEADER;
import static org.fogbowcloud.app.api.constants.CORSProperties.MAX_AGE_HEADER;
import static org.fogbowcloud.app.api.constants.CORSProperties.MAX_AGE_HEADER_VALUE;
import static org.fogbowcloud.app.api.constants.CORSProperties.VARY_HEADER;
import static org.fogbowcloud.app.api.constants.CORSProperties.VARY_HEADER_VALUE;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** This class wrappers operations to trap requests to add CORS headers. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter implements Filter {

    /** Collection of origin addresses that the system allows external requests. */
    private final List<String> allowedOrigins =
            Collections.singletonList(ALLOWED_CLIENT_FRONT_LOCAL);

    /**
     * This method is run on every request received before sending the response, adding the
     * necessary headers.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            final String origin = request.getHeader(VARY_HEADER_VALUE);
            response.setHeader(
                    ALLOWED_ORIGINS_HEADER, allowedOrigins.contains(origin) ? origin : "*");
            response.setHeader(VARY_HEADER, VARY_HEADER_VALUE);

            response.setHeader(MAX_AGE_HEADER, MAX_AGE_HEADER_VALUE);

            response.setHeader(ALLOWED_CREDENTIALS_HEADER, ALLOWED_CREDENTIALS_HEADER_VALUE);

            response.setHeader(ALLOWED_METHODS_HEADER, ALLOWED_METHODS_HEADER_VALUE);

            response.setHeader(ALLOWED_HEADERS_HEADER, ALLOWED_HEADERS_HEADER_VALUES);
        }

        chain.doFilter(req, res);
    }

    /** Part of the filter contract but not applied in our context. */
    public void init(FilterConfig filterConfig) {}

    /** Part of the filter contract but not applied in our context. */
    public void destroy() {}
}
