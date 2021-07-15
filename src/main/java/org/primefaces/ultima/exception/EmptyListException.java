package org.primefaces.ultima.exception;


public class EmptyListException extends Exception {
  private static final long serialVersionUID = 1L;
  
  public EmptyListException(String message) {
    super(message);
  }

  
  public EmptyListException(String message, StackTraceElement[] stackTrace) {
    super(message);
    setStackTrace(stackTrace);
  }
}
