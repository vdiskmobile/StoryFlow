package com.youqude.storyflow.pcs.exception;

/**
 * 所有pcs 异常的父类
 * @author ning
 *
 */
public class PcsException extends Exception {
	public PcsException(String detailMessage) {
		super(detailMessage);
	}
}
