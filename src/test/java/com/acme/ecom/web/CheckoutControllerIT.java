package com.acme.ecom.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CheckoutControllerIT {

    @LocalServerPort int port;

    @Test
    void quote_EU_free_shipping_when_after_discounts_ge_50() throws Exception {

        String body = """
        {
          "items":[{"sku":"A1","unitPrice":"60.00","qty":1}],
          "loyaltyTier":"GOLD",
          "promoCode":"NOEL10",
          "country":"EU"
        }
        """;

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/checkout/quote"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, res.statusCode());

        String json = res.body();
        assertNotNull(json);

        // Assertions simples (robustes)
        assertTrue(json.contains("\"totalAfterDiscounts\":51.30"));
        assertTrue(json.contains("\"shippingCost\":0.00"));
        assertTrue(json.contains("\"finalTotal\":51.30"));
        assertTrue(json.contains("\"shippingRule\":\"WEIGHT_ZONE_AFTER_DISCOUNTS\""));
    }
}
