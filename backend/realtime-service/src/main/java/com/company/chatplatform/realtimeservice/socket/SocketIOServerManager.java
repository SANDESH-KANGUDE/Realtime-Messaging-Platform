package com.company.chatplatform.realtimeservice.socket;

import com.company.chatplatform.common.security.jwt.JwtTokenProvider;
import com.company.chatplatform.realtimeservice.service.PresenceService;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class SocketIOServerManager {

    private static final Logger log = LoggerFactory.getLogger(SocketIOServerManager.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final PresenceService presenceService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SocketIOServer server;

    @Value("${socketio.host:0.0.0.0}")
    private String host;

    @Value("${socketio.port:8085}")
    private int port;

    @Value("${socketio.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    public SocketIOServerManager(JwtTokenProvider jwtTokenProvider, PresenceService presenceService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.presenceService = presenceService;
    }

    private String getAuthToken(HandshakeData data) {
        // 1. Try "auth" query param (JSON)
        String authJson = data.getSingleUrlParam("auth");
        if (authJson != null && !authJson.isEmpty()) {
            try {
                Map<String, Object> authMap = objectMapper.readValue(authJson, Map.class);
                return (String) authMap.get("token");
            } catch (Exception e) {
                log.warn("Failed to parse socket.io auth handshake query payload: {}", authJson);
            }
        }
        // 2. Try "token" query param (plain string)
        String tokenParam = data.getSingleUrlParam("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }
        // 3. Try Authorization header
        String authHeader = data.getHttpHeaders().get("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    @PostConstruct
    public void start() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setOrigin(null); // Disable origin validation to eliminate all CORS blockages in dev
        config.setAuthorizationListener(handshakeData -> {
            log.info("Incoming Socket.IO Handshake - URL: {}, Query Params: {}, Headers: {}", 
                     handshakeData.getUrl(), 
                     handshakeData.getUrlParams(), 
                     handshakeData.getHttpHeaders().entries());
            String token = getAuthToken(handshakeData);
            log.info("Extracted Token: {}", token != null ? (token.substring(0, Math.min(token.length(), 15)) + "...") : "null");
            boolean isValid = token != null && jwtTokenProvider.validateToken(token);
            log.info("Token Validation Result: {}", isValid);
            if (isValid) {
                return new AuthorizationResult(true);
            }
            log.warn("Unauthorized Socket.IO handshake attempt");
            return new AuthorizationResult(false);
        });

        server = new SocketIOServer(config);

        server.addConnectListener(client -> {
            try {
                String token = getAuthToken(client.getHandshakeData());
                log.info("ConnectListener called - Extracted token: {}", token != null ? (token.substring(0, Math.min(token.length(), 15)) + "...") : "null");
                if (token != null) {
                    String userId = jwtTokenProvider.getUserIdFromToken(token);
                    log.info("ConnectListener success - userId resolved: {}", userId);
                    client.set("userId", userId);
                    client.joinRoom(userId); // Join private user room for targeted notifications
                    presenceService.registerConnect(userId, client.getSessionId().toString());
                    log.info("Socket client connected: sessionId={}, userId={}", client.getSessionId(), userId);
                    server.getBroadcastOperations().sendEvent("user.online", Map.of("userId", userId), client);
                } else {
                    log.warn("ConnectListener warning - token is null, disconnecting client");
                    client.disconnect();
                }
            } catch (Exception e) {
                log.error("Exception thrown in connect listener: ", e);
                client.disconnect();
            }
        });

        server.addDisconnectListener(client -> {
            String userId = client.get("userId");
            presenceService.registerDisconnect(client.getSessionId().toString());
            log.info("Socket client disconnected: sessionId={}, userId={}", client.getSessionId(), userId);
            if (userId != null) {
                server.getBroadcastOperations().sendEvent("user.offline", Map.of("userId", userId));
            }
        });

        server.addEventListener("join.room", String.class, (client, room, ackSender) -> {
            client.joinRoom(room);
            log.info("Client {} joined room {}", client.getSessionId(), room);
        });

        server.addEventListener("typing.start", Map.class, (client, data, ackSender) -> {
            String chatId = (String) data.get("chatId");
            String userId = client.get("userId");
            if (chatId != null && userId != null) {
                server.getRoomOperations(chatId).sendEvent("typing.started", Map.of("chatId", chatId, "userId", userId));
                try {
                    String url = "http://localhost:8083/internal/v1/chats/" + chatId + "/member-ids";
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("X-Internal-Token", "secret-internal-service-token");
                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
                    List<String> memberIds = response.getBody();
                    if (memberIds != null) {
                        for (String memberId : memberIds) {
                            if (!memberId.equals(userId)) {
                                server.getRoomOperations(memberId).sendEvent("typing.started", Map.of("chatId", chatId, "userId", userId));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to broadcast typing.started internally", e);
                }
            }
        });

        server.addEventListener("typing.stop", Map.class, (client, data, ackSender) -> {
            String chatId = (String) data.get("chatId");
            String userId = client.get("userId");
            if (chatId != null && userId != null) {
                server.getRoomOperations(chatId).sendEvent("typing.stopped", Map.of("chatId", chatId, "userId", userId));
                try {
                    String url = "http://localhost:8083/internal/v1/chats/" + chatId + "/member-ids";
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("X-Internal-Token", "secret-internal-service-token");
                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
                    List<String> memberIds = response.getBody();
                    if (memberIds != null) {
                        for (String memberId : memberIds) {
                            if (!memberId.equals(userId)) {
                                server.getRoomOperations(memberId).sendEvent("typing.stopped", Map.of("chatId", chatId, "userId", userId));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to broadcast typing.stopped internally", e);
                }
            }
        });

        server.addEventListener("query_presence", Map.class, (client, data, ackSender) -> {
            String targetUserId = (String) data.get("userId");
            if (targetUserId != null) {
                String status = presenceService.getUserStatus(targetUserId);
                client.sendEvent("presence_updated", Map.of("userId", targetUserId, "status", status));
            }
        });

        try {
            server.start();
            log.info("Netty Socket.IO Server started successfully on port {}", port);
        } catch (Exception e) {
            log.error("Failed to start Socket.IO server on port {}", port, e);
        }
    }

    public void broadcastToRoom(String room, String eventName, Object data) {
        if (server != null) {
            server.getRoomOperations(room).sendEvent(eventName, data);
        }
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.stop();
            log.info("Socket.IO Server stopped");
        }
    }
}
