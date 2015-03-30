package com.baloise.confluence.dashboardplus.exception;

@SuppressWarnings("serial")
public class ServiceUnavailableException extends Exception {

	public ServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceUnavailableException(String message) {
		super(message);
	}

}
