package autoever_2st.project.filter.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {
    private static final long MAX_LOG_LENGTH = 1000;

    public String maskSensitiveData(String requestBody) {
        return requestBody.replaceAll("(?<=password=)(.*?)(?=,)", "******");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String responseData = "";
        Exception caughtException = null;

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception e) {
            caughtException = e;
            log.warn("Request processing failed - Method: [{}], URI: [{}], Error: {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
        } finally {
            String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8).trim();
            if (!requestBody.isEmpty()) {
                requestBody = maskSensitiveData(requestBody);
                log.info(">>> request-body : {}", requestBody);
            }

            byte[] contentAsByteArray = responseWrapper.getContentAsByteArray();
            if (contentAsByteArray.length > 0) {
                String contentType = responseWrapper.getContentType();

                if (contentType != null && (
                    contentType.startsWith("image/") || 
                    contentType.startsWith("audio/") || 
                    contentType.startsWith("video/") ||
                    contentType.startsWith("application/octet-stream"))) {
                    responseData = "response-body : [binary data - " + contentType + "]";
                } else {
                    String responseBody = new String(contentAsByteArray, StandardCharsets.UTF_8).trim();
                    if (responseBody.length() > MAX_LOG_LENGTH) {
                        responseData = "response-body : too long, skipped";
                    } else {
                        responseData = "response-body : " + responseBody;
                    }
                }
                responseWrapper.copyBodyToResponse();
            }

            long end = System.currentTimeMillis();

            log.info("\n" + "Http Method : [ {} ] End-Point : [ {} ] Content-Type : [ {} ] Authorization : [ {} ] User-Agent : [ {} ] Host : [ {} ] Content-Length : [ {} ] Spend-Time : [ {} ] Response-Body : [ {} ]",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getContentType(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"),
                    request.getHeader("Host"),
                    request.getContentLength(),
                    (end - start) + "ms",
                    responseData);
        }

        if (caughtException != null) {
            if (caughtException instanceof ServletException) {
                throw (ServletException) caughtException;
            } else if (caughtException instanceof IOException) {
                throw (IOException) caughtException;
            } else if (caughtException instanceof RuntimeException) {
                throw (RuntimeException) caughtException;
            } else {
                throw new ServletException("Unexpected exception", caughtException);
            }
        }
    }
}