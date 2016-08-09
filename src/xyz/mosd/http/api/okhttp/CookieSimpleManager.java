package xyz.mosd.http.api.okhttp;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;
import xyz.mosd.http.api.HttpConstants;

/**
 * Cookie Manager Simple implementation,just match cookie by host now
 * 
 * @author skg
 *
 */
public abstract class CookieSimpleManager extends CookieHandler {
	private static final String TAG = "CookieSimpleManager";

	private static final ConcurrentHashMap<String, ConcurrentHashMap<String, HttpCookie>> domainCookiesCache = new ConcurrentHashMap<String, ConcurrentHashMap<String, HttpCookie>>();
	private static final String VERSION_ZERO_HEADER = "Set-cookie";
	private static final String VERSION_ONE_HEADER = "Set-cookie2";
	private boolean cookiesInited = false;

	private void parseAndCacheCookie(URI uri, List<String> cookiesStrList) {
		if (cookiesStrList == null || uri == null) {
			return;
		}
		for (String cookieStr : cookiesStrList) {
			List<HttpCookie> cookies = null;
			try {
				cookies = HttpCookie.parse(cookieStr);
			} catch (IllegalArgumentException ignored) {
				// this string is invalid, jump to the next one.
				continue;
			}
			if (cookies == null || cookies.isEmpty()) {
				continue;
			}
			for (HttpCookie cookie : cookies) {
				String domain = uri.getHost();// FIXME cookie.getDomain()
				ConcurrentHashMap<String, HttpCookie> cookiesMap = null;
				synchronized (domainCookiesCache) {
					cookiesMap = domainCookiesCache.get(domain);
					if (cookiesMap == null) {
						cookiesMap = new ConcurrentHashMap<String, HttpCookie>();
						domainCookiesCache.put(domain, cookiesMap);
					}
				}
				if (HttpConstants.DEBUG) {
					Log.d(TAG, "add new cookie:" + cookie + ",uri host:" + uri.getHost());
				}
				cookiesMap.put(cookie.getName(), cookie);
				saveCookie(domain, cookie);
			}
		}

	}

	private Map<String, List<String>> cookiesToHeaders(List<HttpCookie> cookies) {
		if (cookies.isEmpty()) {
			return Collections.emptyMap();
		}
		StringBuilder result = new StringBuilder();
		// If all cookies are version 1, add a version 1 header. No header
		// for
		// version 0 cookies.
		int minVersion = 1;
		for (HttpCookie cookie : cookies) {
			minVersion = Math.min(minVersion, cookie.getVersion());
		}
		if (minVersion == 1) {
			result.append("$Version=\"1\"; ");
		}
		result.append(cookies.get(0).toString());
		for (int i = 1; i < cookies.size(); i++) {
			result.append("; ").append(cookies.get(i).toString());
		}
		return Collections.singletonMap("Cookie", Collections.singletonList(result.toString()));
	}

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
		parseAndCacheCookie(uri, responseHeaders.get(VERSION_ZERO_HEADER));
		parseAndCacheCookie(uri, responseHeaders.get(VERSION_ONE_HEADER));
	}

	@Override
	public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
		synchronized (CookieSimpleManager.this) {
			if (!cookiesInited) {
				loadAllCookies(domainCookiesCache);
				cookiesInited = true;
			}
		}
		// FIXME domain
		ConcurrentHashMap<String, HttpCookie> cookiesMap = domainCookiesCache.get(uri.getHost());
		if (HttpConstants.DEBUG) {
			Log.d(TAG, "append " + uri.getHost() + "'s match cookies:" + cookiesMap);
		}
		if (cookiesMap != null && !cookiesMap.isEmpty()) {
			List<HttpCookie> cookieList = new ArrayList<HttpCookie>(cookiesMap.values());
			return cookiesToHeaders(cookieList);
		}
		return requestHeaders;
	}

	protected abstract void saveCookie(String domain, HttpCookie cookie);

	protected abstract void loadAllCookies(ConcurrentHashMap<String, ConcurrentHashMap<String, HttpCookie>> out);
}
