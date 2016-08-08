package xyz.mosd.http.engine.api.okhttp;

import java.io.IOException;
import java.net.CookieHandler;
import java.util.concurrent.TimeUnit;

import xyz.mosd.http.engine.api.HttpConstants;
import xyz.mosd.http.engine.api.HttpEngine;
import xyz.mosd.http.engine.api.RequestCallback;
import xyz.mosd.http.engine.api.RequestCallback2;
import xyz.mosd.http.engine.api.RequestEntity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class HttpEngineImpl extends HttpEngine {

	private static final OkHttpClient mOkHttpClient = new OkHttpClient();

	static {
		mOkHttpClient.setConnectTimeout(HttpConstants.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		mOkHttpClient.setReadTimeout(HttpConstants.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		mOkHttpClient.setWriteTimeout(HttpConstants.HTTP_STREAM_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}

	@Override
	public void setCookieHandler(CookieHandler handler) {
		mOkHttpClient.setCookieHandler(handler);
	}

	@Override
	public <T> void enqueue(RequestEntity req, RequestCallback<T> cb) {
		enqueue(req, null, cb);
	}

	@Override
	public <T> void enqueue(RequestEntity req, Object reqId, RequestCallback<T> cb) {
		Request.Builder b = (Request.Builder) req;
		mOkHttpClient.newCall(b.build()).enqueue(new CallbackImpl<T>(cb, reqId));
	}

	@Override
	public String execute(RequestEntity req) throws HttpBadStateException, IOException {
		Request.Builder b = (Request.Builder) req;
		Response resp = mOkHttpClient.newCall(b.build()).execute();
		String rawResp = resp.body().string();
		if (HttpConstants.DEBUG) {
			debug(resp.request(), rawResp, null);
		}
		if (resp.isSuccessful()) {
			return rawResp;
		} else {
			throw new HttpBadStateException(resp.code());
		}
	}

	private static final int MSG_DISPATCH_FAILURE = 1;
	private static final int MSG_DISPATCH_RESULT = 2;
	private Handler handler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			if (msg != null && msg.obj instanceof CallbackImpl) {
				CallbackImpl<?> cb = (CallbackImpl<?>) msg.obj;
				cb.beforeResult();
				switch (msg.what) {
				case MSG_DISPATCH_RESULT:
					cb.dispatchFailure();
					break;
				case MSG_DISPATCH_FAILURE:
					cb.dispatchResult();
					break;
				}
				cb.afterResult();
			}
		}
	};

	class CallbackImpl<T> implements Callback {
		final RequestCallback<T> mCallback;
		T resultObj = null;
		IOException exception;
		final Object reqId;

		public CallbackImpl(RequestCallback<T> cb, Object reqId) {
			mCallback = cb;
			this.reqId = reqId;
		}

		@Override
		public final void onFailure(Request req, IOException e) {
			if (HttpConstants.DEBUG) {
				debug(req, null, e);
			}
			if (mCallback != null) {
				exception = e;
				Message msg = handler.obtainMessage(MSG_DISPATCH_FAILURE);
				msg.obj = this;
				handler.sendMessage(msg);
			}
		}

		@Override
		public final void onResponse(Response resp) throws IOException {
			String str = resp.body().string();
			if (HttpConstants.DEBUG) {
				debug(resp.request(), str, null);
			}
			if (mCallback != null) {
				try {
					resultObj = mCallback.onResponseInBackground(str, reqId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Message msg = handler.obtainMessage(MSG_DISPATCH_RESULT);
				msg.obj = this;
				handler.sendMessage(msg);
			}
		}

		void dispatchResult() {
			if (mCallback != null) {
				mCallback.onFailure(exception, reqId);
			}
		}

		void dispatchFailure() {
			if (mCallback != null) {
				mCallback.onResult(resultObj, reqId);
			}
		}

		void beforeResult() {
			if (mCallback instanceof RequestCallback2) {
				((RequestCallback2<T>) mCallback).beforeResult(resultObj, reqId);
			}
		}

		void afterResult() {
			if (mCallback instanceof RequestCallback2) {
				((RequestCallback2<T>) mCallback).afterResult(resultObj, reqId);
			}
		}
	}

	private void debug(Request req, String rawData, IOException e) {
		if (req == null) {
			Log.e(TAG, "Request:null");
		} else {
			Log.d(TAG, "URL:" + req.urlString());
		}
		if (e != null) {
			Log.e(TAG, "IOException:" + e);
		}
		Log.d(TAG, "raw data:" + rawData);
	}

}