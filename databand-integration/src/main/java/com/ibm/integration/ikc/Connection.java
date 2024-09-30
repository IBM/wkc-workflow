/*
 * Copyright IBM Corp. 2024
 *
 * The following sample of source code ("Sample") is owned by International
 * Business Machines Corporation or one of its subsidiaries ("IBM") and is
 * copyrighted and licensed, not sold. You may use, copy, modify, and
 * distribute the Sample in any form without payment to IBM, for the purpose of
 * assisting you in the development of your applications.
 *
 * The Sample code is provided to you on an "AS IS" basis, without warranty of
 * any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do
 * not allow for the exclusion or limitation of implied warranties, so the above
 * limitations or exclusions may not apply to you. IBM shall not be liable for
 * any damages you suffer as a result of using, copying, modifying or
 * distributing the Sample, even if IBM has been advised of the possibility of
 * such damages.
 */
package com.ibm.integration.ikc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.ibm.integration.util.DummyTrustManager;
import com.ibm.integration.util.JsonParser;

public class Connection {

    private final String AUTH_ENDPOINT = "/v1/preauth/validateAuth";
    private final HttpClient client;
    private final String bearerToken;

    public Connection() throws KeyManagementException, NoSuchAlgorithmException, IOException, InterruptedException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] { new DummyTrustManager() }, new SecureRandom());
        this.client = HttpClient.newBuilder().sslContext(sslContext).build();
        this.bearerToken = generateBearerToken();
    }

    public static void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() > 299) {
            System.out.println("Request failed. Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
        }
    }

    public HttpClient getHttpClient() {
        return client;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    private String generateBearerToken() throws IOException, InterruptedException {
        System.out.println("Connecting to " + Config.getInstance().getIkcHost());
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(Config.getInstance().getIkcHost() + AUTH_ENDPOINT))
                .header("Authorization", "Basic " + Config.getInstance().getBasicToken()).method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        validateResponse(response);
        return JsonParser.createJsonFromString(response.body()).get("accessToken").asText();
    }
}
