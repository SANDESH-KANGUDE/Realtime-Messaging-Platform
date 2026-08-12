package com.company.chatplatform.realtimeservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PresenceService {

    private final StringRedisTemplate redisTemplate;

    public PresenceService(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void registerConnect(String userId, String socketId) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set("socket:" + socketId, userId);
            redisTemplate.opsForSet().add("connections:" + userId, socketId);
            redisTemplate.opsForValue().set("presence:" + userId, "ONLINE");
        }
    }

    public void registerDisconnect(String socketId) {
        if (redisTemplate != null) {
            String userId = redisTemplate.opsForValue().get("socket:" + socketId);
            if (userId != null) {
                redisTemplate.opsForSet().remove("connections:" + userId, socketId);
                redisTemplate.delete("socket:" + socketId);

                Long remainingSockets = redisTemplate.opsForSet().size("connections:" + userId);
                if (remainingSockets == null || remainingSockets == 0) {
                    redisTemplate.opsForValue().set("presence:" + userId, "OFFLINE");
                }
            }
        }
    }

    public String getUserStatus(String userId) {
        if (redisTemplate != null) {
            String status = redisTemplate.opsForValue().get("presence:" + userId);
            return status != null ? status : "OFFLINE";
        }
        return "OFFLINE";
    }

    public boolean isUserOnline(String userId) {
        return "ONLINE".equalsIgnoreCase(getUserStatus(userId));
    }
}
