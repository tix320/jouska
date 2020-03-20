package com.github.tix320.jouska.client.ui.controller;

public interface Controller<T> {

	void init(T data);

	void destroy();
}
