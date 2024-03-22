package com.example.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class MyController {

  private static final String HEADER_REQUEST_COUNTER = "RequestCounter";
  private static final String HEADER_RESPONSE_COUNTER = "ResponseCounter";

  private final HttpServletRequest httpServletRequest;
  private final HttpServletResponse httpServletResponse;

  public MyController(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) {
    this.httpServletRequest = httpServletRequest;
    this.httpServletResponse = httpServletResponse;
  }


  @PostMapping("/login")
  public ResponseEntity<String> login() {
    String count = httpServletRequest.getHeader(HEADER_REQUEST_COUNTER);
//    log.info("Login request id : " + count);

    httpServletResponse.addHeader(HEADER_RESPONSE_COUNTER,count);

    return ResponseEntity.ok("");
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout() {
    String count = httpServletRequest.getHeader(HEADER_REQUEST_COUNTER);
//    log.info("Logout request id: " + count);

    httpServletResponse.addHeader(HEADER_RESPONSE_COUNTER,count);

    return ResponseEntity.ok("");
  }


}