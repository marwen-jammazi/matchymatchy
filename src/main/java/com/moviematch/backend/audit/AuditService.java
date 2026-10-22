package com.moviematch.backend.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger auditLogger =
            LoggerFactory.getLogger("AUDIT");

    public void logLoginSuccess(String email) {
        auditLogger.info(
                "event=LOGIN_SUCCESS email={}",
                email
        );
    }

    public void logLoginFailure() {
        auditLogger.warn(
                "event=LOGIN_FAILURE"
        );
    }

    public void logUserRegistration(String email) {
        auditLogger.info(
                "event=USER_REGISTERED email={}",
                email
        );
    }

    public void logAccessDenied(String email, String resource) {
        auditLogger.warn(
                "event=ACCESS_DENIED email={} resource={}",
                email,
                resource
        );
    }

    public void logRateLimitExceeded(String clientId, String endpoint) {
        auditLogger.warn(
                "event=RATE_LIMIT_EXCEEDED clientId={} endpoint={}",
                clientId,
                endpoint
        );
    }
}