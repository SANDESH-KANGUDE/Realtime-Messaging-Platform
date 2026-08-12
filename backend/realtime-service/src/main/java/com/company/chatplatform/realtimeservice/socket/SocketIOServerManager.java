package com.company.chatplatform.realtimeservice.socket;

import com.company.chatplatform.common.security.jwt.JwtTokenProvider;
import com.company.chatplatform.realtimeservice.service.PresenceService;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.listener.DataListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SocketIOServerManager {

    private static final Logger log = LoggerFactory.getLogger(SocketIOServerManager.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final PresenceService presenceService;
    private SocketIOServer server;

    @Value("${socketio.host:0.0.0.0}")
    private String host;

    @Value("${socketio.port:8085}")
    private int port;

    public SocketIOServerManager(JwtTokenProvider jwtTokenProvider, PresenceService presenceService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.presenceService = presenceService;
    }

    @PostConstruct
    public void start() {
        Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setAuthorizationListener(handshakeData -> {
            String token = handshakeData.getSingleUrlParam("token");
            if (token != null && jwtTokenProvider.validateToken(token)) {
                return new AuthorizationResult(true);
            }
            log.warn("Unauthorized Socket.IO handshake attempt");
            return new AuthorizationResult(false);
        });

        server = new SocketIOServer(config);

        server.addConnectListener(client -> {
            String token = client.getHandshakeData().getSingleUrlParam("token");
            if (token != null) {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                client.set("userId", userId);
                presenceService.registerConnect(userId, client.getSessionId().toString());
                log.info("Socket client connected: sessionId={}, userId={}", client.getSessionId(), userId);
                server.getBroadcastOperations().sendEvent("user.online", Map.of("userId", userId));
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
            if (chatId != null) {
                server.getRoomOperations(chatId).sendEvent("typing.started", Map.of("chatId", chatId, "userId", userId));
            }
        });

        server.addEventListener("typing.stop", Map.class, (client, data, ackSender) -> {
            String chatId = (String) data.get("chatId");
            String userId = client.get("userId");
            if (chatId != null) {
                server.getRoomOperations(chatId).sendEvent("typing.stopped", Map.of("chatId", chatId, "userId", userId));
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
