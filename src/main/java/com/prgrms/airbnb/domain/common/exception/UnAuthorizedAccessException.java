package com.prgrms.airbnb.domain.common.exception;

public class UnAuthorizedAccessException extends RuntimeException {

  public UnAuthorizedAccessException(String msg) {
    super(msg);
  }
}
