package com.company.chatplatform.realtimeservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);
    private final StringRedisTemplate redisTemplate;

    public PresenceService(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void registerConnect(String userId, String socketId) {
        log.info("Registering socket connect for userId={}, socketId={}", userId, socketId);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set("socket:" + socketId, userId);
            redisTemplate.opsForSet().add("connections:" + userId, socketId);
            redisTemplate.opsForValue().set("presence:" + userId, "ONLINE");
            log.info("Successfully updated presence to ONLINE in Redis for userId={}", userId);
        } else {
            log.warn("RedisTemplate is not available. Cannot store presence.");
        }
    }

    public void registerDisconnect(String socketId) {
        log.info("Registering socket disconnect for socketId={}", socketId);
        if (redisTemplate != null) {
            String userId = redisTemplate.opsForValue().get("socket:" + socketId);
            if (userId != null) {
                redisTemplate.opsForSet().remove("connections:" + userId, socketId);
                redisTemplate.delete("socket:" + socketId);

                Long remainingSockets = redisTemplate.opsForSet().size("connections:" + userId);
                log.info("Remaining socket connections for userId={}: {}", userId, remainingSockets);
                if (remainingSockets == null || remainingSockets == 0) {
                    redisTemplate.opsForValue().set("presence:" + userId, "OFFLINE");
                    log.info("Successfully updated presence to OFFLINE in Redis for userId={}", userId);
                }
            }
        } else {
            log.warn("RedisTemplate is not available. Cannot handle disconnect.");
        }
    }

    public String getUserStatus(String userId) {
        if (redisTemplate != null) {
            String status = redisTemplate.opsForValue().get("presence:" + userId);
            log.debug("Queried presence status for userId={}: {}", userId, status);
            return status != null ? status : "OFFLINE";
        }
        return "OFFLINE";
    }

    public boolean isUserOnline(String userId) {
        return "ONLINE".equalsIgnoreCase(getUserStatus(userId));
    }
}
