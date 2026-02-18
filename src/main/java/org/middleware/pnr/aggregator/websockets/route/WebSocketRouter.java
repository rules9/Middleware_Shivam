package org.middleware.pnr.aggregator.websockets.route;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.websockets.TripWebSocketHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketRouter {
    private final Vertx vertx;
    private final TripWebSocketHandler handler;
    private HttpServer server;


    @PostConstruct
    public void start() {
        log.info("Staring web socket server.");
        server = vertx.createHttpServer()
                .webSocketHandler(ws -> {
                    if ("/ws/trips".equals(ws.path())) {
                        log.info("Web socket accepted, handling the websocket.");
                        handler.handle(ws);
                    } else {
                        log.warn("Rejecting the web socket {}", ws);
                        ws.reject();
                    }
                });
        server.listen(8081);
    }


    @PreDestroy
    public void stop() {
        if (server != null) {
            server.close();
        }
    }

}
