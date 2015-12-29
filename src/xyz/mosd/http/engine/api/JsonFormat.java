package xyz.mosd.http.engine.api;

import java.util.Map;

public class JsonFormat {
	private JsonFormat() {
	}

	private static JsonFormatHandler mHandler;

	public static void configJsonFormatHandler(JsonFormatHandler handler) {
		mHandler = handler;
	}

	public static String toJson(Map<String, Object> fieldsMap) {
		if (mHandler == null) {
			throw new RuntimeException(
					"if you want to support JSON post,you must configJsonFormatHandler:"
							+ JsonFormat.class.getCanonicalName());
		} else {
			return mHandler.toJson(fieldsMap);
		}
	}

}
