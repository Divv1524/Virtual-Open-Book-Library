package com.project.virtualopenbooklibrary.exception;

public class BookServiceException extends RuntimeException {
  public BookServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
