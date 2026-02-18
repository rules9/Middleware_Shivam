package org.middleware.pnr.aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.dto.TripResponse;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
public class FunctionalTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static int passed = 0;
    private static int failed = 0;
    private static final int TOTAL_TESTS = 5;

    public static void main(String[] args) {
        log.info("[TEST]: Starting test execution");
        System.out.println("Test: Starting test case execution");

        if (!isServiceAvailable()) {
            log.error("[TEST]: Service is not available at {}. Please start the application first.", BASE_URL);
            System.out.println("ERROR: Service is not available at " + BASE_URL);
            System.out.println("Please start the application and try again.");
            System.exit(1);
        }

        testCase1_ValidPnr();
        testCase2_InvalidPnr();
        testCase3_ValidCustomerId();
        testCase4_RateLimiting();
        testCase5_RateLimitReset();


        int testsRun = passed + failed;

        log.info("[TEST]: Test Summary - Passed: {}, Failed: {}, Total: {}", passed, failed, testsRun);
        System.out.println("\nTest Summary");
        System.out.println("##################################");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + testsRun);
        System.out.println("##################################");
    }


    private static boolean isServiceAvailable() {
        log.info("[TEST]: Checking if service is available at {}", BASE_URL);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/v1/booking/PNR123"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("[TEST]: Service is available (status: {})", response.statusCode());
            return true;
        } catch (ConnectException e) {
            log.error("[TEST]: Cannot connect to service at {}", BASE_URL);
            return false;
        } catch (Exception e) {
            log.warn("[TEST]: Service check returned error: {}", e.getMessage());
            return false;
        }
    }

    private static void testCase1_ValidPnr() {
        System.out.print("[TEST] Test 1: GET /v1/booking/PNR123 (Valid PNR) ");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/v1/booking/PNR123"))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("[TEST]: Test 1 - Status: {}", response.statusCode());

            if (response.statusCode() == 200) {
                String body = response.body();
                if (body != null && body.contains("pnr")) {
                    log.info("[TEST]: Test 1: PASS");
                    passed++;
                } else {
                    log.error("[TEST]: Test 1: FAIL - Missing required fields");
                    System.out.println("FAIL: Response missing required fields");
                    failed++;
                }
            } else {
                log.error("[TEST]: Test 1: FAIL - Expected 200, got {}", response.statusCode());
                System.out.println("FAIL: Expected 200, got " + response.statusCode());
                failed++;
            }
        } catch (ConnectException e) {
            log.error("[TEST]: Test 1: FAIL - Cannot connect to service at {}", BASE_URL);
            System.out.println("FAIL: Cannot connect to service");
            failed++;
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.error("[TEST]: Test 1: FAIL - {}", errorMsg, e);
            System.out.println("FAIL: " + errorMsg);
            failed++;
        }
    }

    private static void testCase2_InvalidPnr() {
        System.out.print("[TEST] Test 2: GET /v1/booking/pnrNotPresent (Invalid PNR - not present) ");
        try {
            String url = BASE_URL + "/v1/booking/pnrNotPresent";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("[TEST]: Test 2 - Status: {}", response.statusCode());

            if (response.statusCode() == 200) {
                String body = response.body();
                if (body != null) {
                   ObjectMapper objectMapper = new ObjectMapper();
                    TripResponse tripResponse = objectMapper.readValue(body, TripResponse.class);

                    boolean isEmpty = (tripResponse.getPnr() == null || tripResponse.getPnr().isEmpty()) &&
                            (tripResponse.getPassengers() == null || tripResponse.getPassengers().isEmpty()) &&
                            (tripResponse.getFlights() == null || tripResponse.getFlights().isEmpty());
                    if (isEmpty) {
                        log.info("[TEST]: Test 2: PASS - Empty booking response received");
                        passed++;
                    } else {
                        log.error("[TEST]: Test 2: FAIL - Expected empty booking, but got data: pnr={}, passengers={}, flights={}",
                                tripResponse.getPnr(),
                                tripResponse.getPassengers() != null ? tripResponse.getPassengers().size() : 0,
                                tripResponse.getFlights() != null ? tripResponse.getFlights().size() : 0);
                        System.out.println("FAIL: Expected empty booking, but response contains data");
                        failed++;
                    }
                } else {
                    log.error("[TEST]: Test 2: FAIL - Response body is null");
                    System.out.println("FAIL: Response body is null");
                    failed++;
                }
            } else {
                log.error("[TEST]: Test 2: FAIL - Expected 200, got {}", response.statusCode());
                System.out.println("FAIL: Expected 200, got " + response.statusCode());
                failed++;
            }
        } catch (ConnectException e) {
            log.error("[TEST]: Test 2: FAIL - Cannot connect to service at {}", BASE_URL);
            System.out.println("FAIL: Cannot connect to service");
            failed++;
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.error("[TEST]: Test 2: FAIL - {}", errorMsg, e);
            System.out.println("FAIL: " + errorMsg);
            failed++;
        }
    }

    private static void testCase3_ValidCustomerId() {
        System.out.print("[TEST] Test 3: GET /v1/booking/customer/CUST-001 (Valid Customer ID) ");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/v1/booking/customer/CUST-001"))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("[TEST]: Test 3 - Status: {}", response.statusCode());

            if (response.statusCode() == 200) {
                String body = response.body();
                if (body != null && body.contains("pnr")) {
                    log.info("[TEST]: Test 3: PASS");
                    passed++;
                } else {
                    log.error("[TEST]: Test 3: FAIL - Missing required fields");
                    System.out.println("FAIL: Response missing required fields");
                    failed++;
                }
            } else {
                log.error("[TEST]: Test 3: FAIL - Expected 200, got {}", response.statusCode());
                System.out.println("FAIL: Expected 200, got " + response.statusCode());
                failed++;
            }
        } catch (ConnectException e) {
            log.error("[TEST]: Test 3: FAIL - Cannot connect to service at {}", BASE_URL);
            System.out.println("FAIL: Cannot connect to service");
            failed++;
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.error("[TEST]: Test 3: FAIL - {}", errorMsg, e);
            System.out.println("FAIL: " + errorMsg);
            failed++;
        }
    }

    private static void testCase4_RateLimiting() {
        System.out.print("[TEST] Test 4: Rate Limiting (11 requests, 11th should be 429) ");
        try {
            int rateLimitedCount = 0;
            int successCount = 0;

            for (int i = 1; i <= 11; i++) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/v1/booking/PNR123"))
                        .timeout(Duration.ofSeconds(3))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.debug("[TEST]: Test 4 - Request {} - Status: {}", i, response.statusCode());

                if (response.statusCode() == 429) {
                    rateLimitedCount++;
                } else if (response.statusCode() == 200 || response.statusCode() == 500) {
                    successCount++;
                }
                Thread.sleep(100);
            }

            if (rateLimitedCount > 0) {
                log.info("[TEST]: Test 4: PASS - Rate limited: {}, Success: {}", rateLimitedCount, successCount);
                passed++;
            } else {
                log.error("[TEST]: Test 4: FAIL - No requests were rate limited. Success: {}", successCount);
                System.out.println("FAIL: No requests were rate limited. Success: " + successCount);
                failed++;
            }
        } catch (ConnectException e) {
            log.error("[TEST]: Test 4: FAIL - Cannot connect to service at {}", BASE_URL);
            System.out.println("FAIL: Cannot connect to service");
            failed++;
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.error("[TEST]: Test 4: FAIL - {}", errorMsg, e);
            System.out.println("FAIL: " + errorMsg);
            failed++;
        }
    }

    private static void testCase5_RateLimitReset() {
        System.out.print("[TEST] Test 5: Rate Limit Window Reset (wait 11s after exhausting limit) ");
        try {
            for (int i = 0; i < 10; i++) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/v1/booking/PNR123"))
                        .timeout(Duration.ofSeconds(3))
                        .GET()
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Thread.sleep(100);
            }

            HttpRequest checkRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/v1/booking/PNR123"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> checkResponse = httpClient.send(checkRequest, HttpResponse.BodyHandlers.ofString());
            log.debug("[TEST]: Test 5 - Check request Status: {}", checkResponse.statusCode());

            if (checkResponse.statusCode() == 429) {
                Thread.sleep(11000);

                HttpRequest resetRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/v1/booking/PNR123"))
                        .timeout(Duration.ofSeconds(8))
                        .GET()
                        .build();
                HttpResponse<String> resetResponse = httpClient.send(resetRequest, HttpResponse.BodyHandlers.ofString());
                log.debug("[TEST]: Test 5 - Reset request Status: {}", resetResponse.statusCode());

                if (resetResponse.statusCode() == 200 || resetResponse.statusCode() == 500) {
                    log.info("[TEST]: Test 5: PASS");
                    passed++;
                } else {
                    log.error("[TEST]: Test 5: FAIL - After reset, got {} (expected 200 or 500)", resetResponse.statusCode());
                    System.out.println("FAIL: After reset, got " + resetResponse.statusCode() + " (expected 200 or 500)");
                    failed++;
                }
            } else {
                log.error("[TEST]: Test 5: FAIL - Rate limit not exhausted. Got {}", checkResponse.statusCode());
                System.out.println("FAIL: Rate limit not exhausted (got " + checkResponse.statusCode() + ")");
                failed++;
            }
        } catch (ConnectException e) {
            log.error("[TEST]: Test 5: FAIL - Cannot connect to service at {}", BASE_URL);
            System.out.println("FAIL: Cannot connect to service");
            failed++;
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.error("[TEST]: Test 5: FAIL - {}", errorMsg, e);
            System.out.println("FAIL: " + errorMsg);
            failed++;
        }
    }
}