package org.fogbowcloud.app.api.configuration;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.fogbowcloud.app.api.constants.CORSProperties.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter implements Filter {

    private final List<String> allowedOrigins =
            Collections.singletonList(ALLOWED_CLIENT_FRONT_LOCAL);

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
