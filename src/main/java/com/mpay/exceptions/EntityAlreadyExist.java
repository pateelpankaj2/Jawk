package com.mpay.exceptions;

public class EntityAlreadyExist extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EntityAlreadyExist(String msg) {
		super(msg);
	}
}
