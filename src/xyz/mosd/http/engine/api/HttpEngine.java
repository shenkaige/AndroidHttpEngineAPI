package xyz.mosd.http.engine.api;

import java.io.IOException;
import java.net.CookieHandler;

import xyz.mosd.http.engine.api.okhttp.HttpEngineImpl;

/**
 * 网络请求类
 */
public abstract class HttpEngine {
	public static final String TAG = "HttpEngine";
	private static final HttpEngine mHttpEngine = new HttpEngineImpl();

	public static HttpEngine getInstance() {
		return mHttpEngine;
	}

	public abstract void setCookieHandler(CookieHandler handler);

	public abstract <T> void enqueue(RequestEntity req, RequestCallback<T> cb);

	public abstract <T> void enqueue(RequestEntity req, Object reqId, RequestCallback<T> cb);

	public abstract String execute(RequestEntity req) throws HttpBadStateException, IOException;

	public static class HttpBadStateException extends IOException {
		private static final long serialVersionUID = -5812346139602784871L;
		private final int stateCode;

		public HttpBadStateException(int stateCode) {
			this.stateCode = stateCode;
		}

		public int getHttpStateCode() {
			return stateCode;
		}

	}
}