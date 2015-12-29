package xyz.mosd.http.engine.api.okhttp;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import xyz.mosd.http.engine.api.JsonFormat;
import xyz.mosd.http.engine.api.RequestEntity;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;

/**
 * OkHttp对RequestEntity的实现
 */
public class RequestEntityImpl extends Builder implements RequestEntity {
	private Map<String, Object> mFieldsMap = new Hashtable<String, Object>() {
		private static final long serialVersionUID = 8096750948729357542L;

		@Override
		public synchronized Object put(String key, Object value) {
			if (key == null || value == null) {
				return null;
			}
			return super.put(key, value);
		}

	};
	private String jsonPostContent;
	private boolean mHaveFileUpload = false;
	private String mAction;
	private Method mMethod;
	private Charset mCharset;

	@Override
	public RequestEntity setAction(String prefix, String action) {
		return setAction(prefix + action);
	}

	@Override
	public RequestEntity setAction(String action) {
		mAction = action;
		return this;
	}

	@Override
	public RequestEntity setCharset(Charset charset) {
		mCharset = charset;
		if (mCharset != null && !"utf-8".equalsIgnoreCase(mCharset.name())) {
			throw new UnsupportedCharsetException("OkHttp do not support " + charset);
		}
		return this;
	}

	@Override
	public RequestEntity setMethod(Method method) {
		mMethod = method;
		return this;
	}

	@Override
	public RequestEntity addReqeustHeader(String name, Object value) {
		addHeader(name, value.toString());
		return this;
	}
	
	@Override
	public RequestEntity addQuery(String name, byte value) {
		mFieldsMap.put(name, value);
		return this;
	}

	@Override
	public RequestEntity addQuery(String name, char value) {
		mFieldsMap.put(name, value);
		return this;
	}

	@Override
	public RequestEntity addQuery(String name, int value) {
		mFieldsMap.put(name, value);
		return this;
	}

	@Override
	public RequestEntity addQuery(String name, long value) {
		mFieldsMap.put(name, value);
		return this;
	}

	@Override
	public RequestEntity addQuery(String name, float value) {
		mFieldsMap.put(name, value);
		return this;
	}

	@Override
	public RequestEntity addQuery(String name, double value) {
		mFieldsMap.put(name, value);
		return this;
	}

	@Override
	public RequestEntity addQuery(String name, String value) {
		mFieldsMap.put(name, value);
		return this;
	}

	@Override
	public RequestEntity addQuery(String name, Object value) {
		mFieldsMap.put(name, value);
		return this;
	}
	
	@Override
	public RequestEntity addJSONQuery(String value) {
		jsonPostContent = value;
		return this;
	}
	
	private class FileEntry {
		String file;
		String mime;

		FileEntry(String file, String contentType) {
			this.file = file;
			this.mime = contentType;
		}
	}

	@Override
	public RequestEntity addFile(String name, String filePath, String contentType) {
		mFieldsMap.put(name, new FileEntry(filePath, contentType));
		mHaveFileUpload = true;
		return this;
	}

	@Override
	public Request build() {
		if (RequestEntity.Method.POST == mMethod) {
			if (mHaveFileUpload) {
				handlerPostMultipartBuild();
			} else {
				handlerPostBuild();
			}
		} else if (RequestEntity.Method.POST_JSON == mMethod) {
			addHeader("Content-type", "application/json");
			handlerJsonPost();
		} else {
			handlerGetBuild(true);
		}
		return super.build();
	}
	
	private final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
	
	private void handlerJsonPost() {
		handlerGetBuild(false);
		if (jsonPostContent != null && !mFieldsMap.isEmpty()) {
			throw new RuntimeException("ReqeustEntity can not set json content and ohter query params in same times");
		}
		final String jsonStr;
		if (jsonPostContent != null) {
			jsonStr = jsonPostContent;
		} else {
			jsonStr = JsonFormat.toJson(mFieldsMap);
		}
		RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, jsonStr);
		post(body);
	}

	/**
	 * 处理get请求
	 * 
	 * @param appendQueryParams
	 *            是否携带query参数
	 */
	private void handlerGetBuild(boolean appendQueryParams) {
		String url = mAction;
		if (appendQueryParams && mFieldsMap.size() > 0) {
			StringBuilder sb = new StringBuilder();
			if (url != null) {
				sb.append(url);
				if (url.length() > 0 && url.charAt(url.length() - 1) != '?') {
					sb.append('?');
				}
			}
			Set<Map.Entry<String, Object>> ens = mFieldsMap.entrySet();
			for (Map.Entry<String, Object> e : ens) {
				if (e.getValue() == null) {
					continue;
				}
				sb.append(e.getKey());
				sb.append('=');
				sb.append(e.getValue());
				sb.append('&');
			}
			if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '&') {
				sb.deleteCharAt(sb.length() - 1);
			}
			url = sb.toString();
		}
		url(url);
		get();
	}

	/**
	 * 处理post请求，不带文件
	 */
	private void handlerPostBuild() {
		handlerGetBuild(false);
		if (mFieldsMap.size() <= 0) {
			return;
		}
		FormEncodingBuilder builder = new FormEncodingBuilder();
		Set<Map.Entry<String, Object>> ens = mFieldsMap.entrySet();
		for (Map.Entry<String, Object> e : ens) {
			if (e.getValue() == null) {
				continue;
			}
			builder.add(e.getKey(), e.getValue().toString());
		}
		super.post(builder.build());
	}

	/**
	 * 处理post请求，带文件
	 */
	private void handlerPostMultipartBuild() {
		handlerGetBuild(false);
		if (mFieldsMap.size() <= 0) {
			return;
		}
		MultipartBuilder builder = new MultipartBuilder();
		builder.type(MultipartBuilder.FORM);
		Set<Map.Entry<String, Object>> ens = mFieldsMap.entrySet();
		for (Map.Entry<String, Object> e : ens) {
			if (e.getValue() == null) {
				continue;
			}
			if (e.getValue() instanceof FileEntry) {
				// add file
				FileEntry fe = (FileEntry) e.getValue();
				File f = new File(fe.file);
				RequestBody rb = RequestBody.create(MediaType.parse(fe.mime), f);
				builder.addFormDataPart(e.getKey(), f.getName(), rb);
			} else {
				// add string kv
				builder.addFormDataPart(e.getKey(), e.getValue().toString());
			}
		}
		super.post(builder.build());
	}
}
