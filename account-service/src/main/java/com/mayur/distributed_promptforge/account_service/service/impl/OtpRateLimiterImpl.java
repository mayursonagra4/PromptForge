package com.mayur.distributed_promptforge.account_service.service.impl;

import com.mayur.distributed_promptforge.account_service.service.OtpRateLimiter;
import com.mayur.distributed_promptforge.common_lib.error.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpRateLimiterImpl implements OtpRateLimiter {

    private static final String PREFIX_OTP_LIMIT = "rl:otp:";
    private static final long COOLDOWN_SECONDS = 60L;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void checkAndSetLimit(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();
        String key = PREFIX_OTP_LIMIT + normalizedEmail;

        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            log.warn("OTP request rate-limited for email: {}", normalizedEmail);
            throw new BadRequestException("Please wait at least 60 seconds before requesting another code.");
        }

        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(COOLDOWN_SECONDS));
        log.info("OTP rate limit marker set for email: {}", normalizedEmail);
    }

    @Override
    public void clearLimit(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();
        redisTemplate.delete(PREFIX_OTP_LIMIT + normalizedEmail);
        log.debug("OTP rate limit marker cleared for email: {}", normalizedEmail);
    }
}
