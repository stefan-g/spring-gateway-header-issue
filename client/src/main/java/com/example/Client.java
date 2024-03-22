package com.example;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.log4j.Log4j2;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.MDC;

@Log4j2
public class Client {

  private static final String url = "http://localhost:8080"; //change port after gateway is started
  private static final AtomicLong counter = new AtomicLong((new SecureRandom().nextLong(9999) + 1) * 1000000000);

  private static final String HEADER_REQUEST_COUNTER = "RequestCounter";
  private static final String HEADER_RESPONSE_COUNTER = "ResponseCounter";

  public static void main(String[] args) throws IOException, InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(10);

    for (int t = 0; t < 10; t++) {
      executor.submit(() -> {
        log.info("Thread: " + Thread.currentThread().getId());

        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient client = HttpClientBuilder.create()
                                                           .setDefaultCookieStore(cookieStore)
                                                           .build()) {
          for (int i = 0; i < 10000; i++) {
            final String requestId = String.valueOf(counter.getAndIncrement());
            try (MDC.MDCCloseable closeable = MDC.putCloseable(HEADER_REQUEST_COUNTER, requestId)) {
              doLogin(client, requestId);
              doLogout(client, requestId);
            }
          }
        } catch (IOException ex) {
          log.error("", ex);
        }
      });
    }
    executor.shutdown();
    executor.awaitTermination(60, TimeUnit.SECONDS);
  }

  private static void doLogin(final CloseableHttpClient client, final String requestId) throws IOException {
    HttpPost post = new HttpPost(url + "/login");
    post.setHeader(HEADER_REQUEST_COUNTER, requestId);
    CloseableHttpResponse response = (CloseableHttpResponse) client
        .execute(post, response1 -> response1);

    Header header = response.getFirstHeader(HEADER_RESPONSE_COUNTER);
    if (header == null || !header.getValue().equals(requestId)) {
      log.error("Count after login is different {}", header);
    }

    if (response.getCode() != HttpStatus.SC_OK) {
      log.error(requestId + " : login failed");
    }
  }

  private static void doLogout(final CloseableHttpClient client, final String requestId) throws IOException {
    HttpPost post = new HttpPost(url + "/logout");
    post.setHeader(HEADER_REQUEST_COUNTER, requestId);
    CloseableHttpResponse response = (CloseableHttpResponse) client
        .execute(post, response1 -> response1);

    Header header = response.getFirstHeader(HEADER_RESPONSE_COUNTER);
    if (header == null || !header.getValue().equals(requestId)) {
      log.error("Count after logout is different {}", header);
    }

    if (response.getCode() != HttpStatus.SC_OK) {
      log.error(requestId + " : logout failed");
    }

  }

}