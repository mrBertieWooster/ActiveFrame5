package ru.intertrust.cm.core.gui.impl.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.business.api.dto.UserUidWithPassword;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.gui.api.server.LoginService;

import javax.ejb.EJB;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ravil on 12.10.2017.
 */
@Component(value = "httpRequestLogFilter")
public class HttpRequestLogFilter extends CommonsRequestLoggingFilter {
    private static final String EXCLUSIONS_DELIMITER = ",";
    private Long elapsed;
    private Long startTime;
    @Value("${http.request.log.min.time:100}")
    private Integer minTime;

    @Value("${http.request.log.excluded.patterns:#{null}}")
    private String excludePatterns;

    @Override
    protected void afterRequest(HttpServletRequest request,
                                String message) {
        elapsed = System.currentTimeMillis() - startTime;
        if (((excludePatterns != null && !isMatchByPattern(request.getRequestURI()))
                || excludePatterns == null)
                &&
                elapsed>=minTime) {
            super.afterRequest(request, message);
        }
    }

    @Override
    protected String createMessage(HttpServletRequest request, String prefix, String suffix) {
        UserCredentials credentials = (UserCredentials) request.getSession().getAttribute(
                LoginService.USER_CREDENTIALS_SESSION_ATTRIBUTE);
        String requestString = request.getRequestURL() +
                ((request.getQueryString() != null) ? "?" + request.getQueryString() : "");
        String loginName = (credentials != null) ? ((UserUidWithPassword) credentials).getUserUid() : "NOT_LOGGED";
        return elapsed + "\t" + request.getMethod() + "\t"
                + request.getContentLength() + "\t" + request.getRemoteAddr() + "\t" + loginName +
                "\t" + requestString;
    }

    private Boolean isMatchByPattern(String data) {
        List<Pattern> patterns = new ArrayList<>();
        if (excludePatterns != null) {
            for (String exclusion : excludePatterns.split(EXCLUSIONS_DELIMITER)) {
                patterns.add(Pattern.compile(exclusion));
            }
        }
        for (Pattern p : patterns) {
            Matcher matcher = p.matcher(data);
            if (matcher.find()) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        startTime = System.currentTimeMillis();

        boolean isFirstRequest = !this.isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;
        if(this.isIncludePayload() && isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        boolean shouldLog = this.shouldLog(requestToUse);

        try {
            filterChain.doFilter(requestToUse, response);
        } finally {
            if(shouldLog && !this.isAsyncStarted(requestToUse)) {
                this.afterRequest(requestToUse, createMessage(requestToUse,null,null));
            }

        }
    }
}
