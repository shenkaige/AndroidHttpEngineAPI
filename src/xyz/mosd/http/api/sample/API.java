package xyz.mosd.http.api.sample;

import java.nio.charset.Charset;

import xyz.mosd.http.api.RequestEntity;
import xyz.mosd.http.api.RequestEntity.Method;
import xyz.mosd.http.api.okhttp.RequestEntityImpl;

/**
 * 服务器接口
 */
public class API {
	private API() {
	}

	public static final String CHARSET_STR = "UTF-8";
	public static final Charset CHARSET = Charset.forName(CHARSET_STR);
	//
	private final static String ADDR_PREFIX = "http://www.baidu.com/product/interface/";
	//
	public static final int PER_REQUEST_ITEMS_COUNT = 20;
	//
	public static final int CLIENT_TYPE_ID = 1;// 1 Android 2 IOS
	public static final String platform_name = "Android";
	public static final int platform_code = 1;// 1 Android 2 IOS

	/**
	 * 拦截返回，添加公用数据
	 * 
	 * @param e
	 * @return
	 */
	private static RequestEntity perReturn(RequestEntity e) {
		e.addQuery("dev", CLIENT_TYPE_ID);
		e.addReqeustHeader("header", "header value");
		return e;
	}

	private static RequestEntity newRequestEntity(RequestEntity.Method method) {
		RequestEntityImpl ok = new RequestEntityImpl();
		ok.setMethod(method);
		return ok;
	}

	/**
	 * 登陆
	 */
	public static RequestEntity login(String mobileNo, String password) {
		RequestEntity e = newRequestEntity(Method.POST);
		e.setAction(ADDR_PREFIX, "/login.do");
		e.addQuery("mobileNo", mobileNo);
		e.addQuery("password", password);
		//
		e.addReqeustHeader("header_name", "header value");
		return perReturn(e);
	}

	/**
	 * 上传图片
	 */
	public static RequestEntity uploadImg(int uid, String... imgPaths) {
		RequestEntity e = newRequestEntity(Method.POST);
		e.setAction(ADDR_PREFIX, "/uploadImg.do");
		e.addQuery("uid", uid);
		if (imgPaths != null && imgPaths.length > 0) {
			int size = imgPaths.length;
			for (int i = 0; i < size; i++) {
				e.addFile("image", imgPaths[i], RequestEntity.MIME_IMAGE);
			}
		}
		return perReturn(e);
	}

}
