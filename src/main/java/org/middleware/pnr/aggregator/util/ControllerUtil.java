package org.middleware.pnr.aggregator.util;

import io.vertx.circuitbreaker.OpenCircuitException;
import io.vertx.core.Future;
import jakarta.servlet.http.HttpServletRequest;
import org.middleware.pnr.aggregator.exceptions.BookingNotFoundException;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

public class ControllerUtil {

    public static String  getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Client-IP");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "unknown";
    }


    public static <T> CompletableFuture<ResponseEntity<T>> toCompletableFuture(Future<ResponseEntity<T>> future) {

        CompletableFuture<ResponseEntity<T>> completableFuture = new CompletableFuture<>();

        future.onSuccess(completableFuture::complete).onFailure(throwable -> {
            if (throwable instanceof OpenCircuitException) {
                completableFuture.complete(ResponseEntity.status(503).build());
            } else if (throwable instanceof BookingNotFoundException) {
                completableFuture.completeExceptionally(throwable);
            } else if (throwable.getCause() instanceof BookingNotFoundException) {
                completableFuture.completeExceptionally(throwable.getCause());
            } else {
                completableFuture.complete(ResponseEntity.internalServerError().build());
            }
        });
        return completableFuture;
    }



}
