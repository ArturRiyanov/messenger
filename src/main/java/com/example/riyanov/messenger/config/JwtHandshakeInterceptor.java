package com.example.riyanov.messenger.config;

import com.example.riyanov.messenger.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");
            System.out.println("WebSocket handshake, token: " + token);

            if (token != null) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                try {
                    String username = jwtUtil.extractUsername(token);
                    Long userId = jwtUtil.extractUserId(token);
                    System.out.println("Extracted username: " + username + ", userId: " + userId);

                    if (username != null && jwtUtil.validateToken(token, username)) {
                        if (userId != null) {
                            attributes.put("userId", userId);
                            attributes.put("username", username);
                            System.out.println("User authenticated via WebSocket: " + username);
                        } else {
                            System.out.println("Token valid but userId missing");
                        }
                    } else {
                        System.out.println("Token validation failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No token provided");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}