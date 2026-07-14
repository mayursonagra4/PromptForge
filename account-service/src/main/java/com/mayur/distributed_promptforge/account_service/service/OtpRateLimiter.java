package com.mayur.distributed_promptforge.account_service.service;

public interface OtpRateLimiter {
    /**
     * Checks if the email is rate-limited for OTP sending.
     * If rate-limited, throws a BadRequestException.
     * Otherwise, sets the rate limit key in Redis.
     */
    void checkAndSetLimit(String email);

    /**
     * Clears the rate limit marker for the given email.
     */
    void clearLimit(String email);
}
