package xyz.mosd.http.api.okhttp;

import java.net.HttpCookie;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.content.SharedPreferences;

public class SharedPreferenceCookieHandler extends CookieSimpleManager {

	private final SharedPreferences sp;

	public SharedPreferenceCookieHandler(SharedPreferences sp) {
		this.sp = sp;
	}

	@Override
	protected void saveCookie(String domain, HttpCookie cookie) {
		sp.edit().putString(toSaveKey(domain, cookie), toCookieString(cookie)).commit();
	}

	private String toSaveKey(String domain, HttpCookie cookie) {
		return domain + ";" + cookie.getName();
	}

	private String fetchDomainFromKey(String key) {
		String[] dn = key.split(";");
		if (dn != null && dn.length > 0) {
			return dn[0];
		}
		return null;
	}

	@Override
	protected void loadAllCookies(ConcurrentHashMap<String, ConcurrentHashMap<String, HttpCookie>> out) {
		Map<String, ?> spValues = null;
		try {
			spValues = sp.getAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (spValues == null || spValues.isEmpty()) {
			return;
		}
		Set<String> keys = spValues.keySet();
		for (String k : keys) {
			final Object v = spValues.get(k);
			if (v == null) {
				continue;
			}
			String domain = fetchDomainFromKey(k);
			ConcurrentHashMap<String, HttpCookie> cookiesMap = out.get(domain);
			if (cookiesMap == null) {
				cookiesMap = new ConcurrentHashMap<String, HttpCookie>();
				out.put(domain, cookiesMap);
			}
			HttpCookie cookie = toCookie(v.toString());
			if (cookie != null) {
				cookiesMap.put(cookie.getName(), cookie);
			}
		}

	}

	private String toCookieString(HttpCookie cookie) {
		StringBuilder sb = new StringBuilder();
		sb.append(cookie.getName());
		sb.append(";");
		sb.append(cookie.getValue());
		sb.append(";");
		sb.append(cookie.getDomain());
		sb.append(";");
		sb.append(cookie.getMaxAge());
		sb.append(";");
		sb.append(cookie.getVersion());
		return sb.toString();
	}

	private HttpCookie toCookie(String cookieStr) {
		String[] cookieAttrs = cookieStr.split(";");
		HttpCookie result = new HttpCookie(//
				cookieAttrs[0], // name
				cookieAttrs[1]// value
		);
		result.setDomain(cookieAttrs[2]);// domain
		result.setMaxAge(Long.parseLong(cookieAttrs[3]));// max age
		result.setVersion(Integer.parseInt(cookieAttrs[4]));// version
		return result;
	}

}
