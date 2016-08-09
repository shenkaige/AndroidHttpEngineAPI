package xyz.mosd.http.api.sample;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import xyz.mosd.http.api.RequestCallback;
import xyz.mosd.http.api.okhttp.HttpEngineImpl;
import xyz.mosd.http.api.okhttp.RequestEntityImpl;
import xyz.mosd.http.api.okhttp.SharedPreferenceCookieHandler;
import xyz.mosd.http.engine.api.R;

public class TestActivity extends Activity {
	private TextView msgTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		msgTv = (TextView) findViewById(R.id.msg_tv);
		//
		SharedPreferences sp = getSharedPreferences("cookies", Context.MODE_PRIVATE);
		HttpEngineImpl.getInstance().setCookieHandler(new SharedPreferenceCookieHandler(sp));
		//
		testSetCookies();
		// testShowCookies();
	}

	private void testSetCookies() {
		RequestEntityImpl req = new RequestEntityImpl();
		req.setAction("http://10.0.2.2:8080/TestCookies/cookies/addCookies.do");
		HttpEngineImpl.getInstance().enqueue(req, new RequestCallback<String>() {

			@Override
			public String onResponseInBackground(String resp, Object reqId) throws Exception {
				return resp;
			}

			@Override
			public void onResult(String result, Object reqId) {
				msgTv.setText(msgTv.getText() + "\n" + result);
				Log.d("cookie_test", "setup cookies:" + result);
				testShowCookies();
			}

			@Override
			public void onFailure(IOException e, Object reqId) {

			}

		});
	}

	private void testShowCookies() {
		Log.d("cookie_test", "--------------------------------------------");
		Log.d("cookie_test", "--------------------------------------------");
		RequestEntityImpl req = new RequestEntityImpl();
		req.setAction("http://10.0.2.2:8080/TestCookies/cookies/show/show.do");
		HttpEngineImpl.getInstance().enqueue(req, new RequestCallback<String>() {

			@Override
			public String onResponseInBackground(String resp, Object reqId) throws Exception {
				return resp;
			}

			@Override
			public void onResult(String result, Object reqId) {
				msgTv.setText(msgTv.getText() + "\n" + result);
				Log.d("cookie_test", "show cookies:" + result);
			}

			@Override
			public void onFailure(IOException e, Object reqId) {

			}

		});
	}
}
