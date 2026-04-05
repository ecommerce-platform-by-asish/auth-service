package com.ecommerce.auth.exception;

import com.ecommerce.common.exception.BaseException;
import com.ecommerce.common.exception.GlobalErrorCode;

/**
 * Exception thrown when RSA key operations fail. Uses HttpStatus.INTERNAL_SERVER_ERROR and
 * GlobalErrorCode.INTERNAL_SERVER_ERROR.
 */
public class RsaException extends BaseException {

  public RsaException(String message, Throwable cause) {
    super(message, GlobalErrorCode.INTERNAL_SERVER_ERROR, cause);
  }
}
