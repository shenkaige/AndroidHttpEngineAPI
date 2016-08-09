package xyz.mosd.http.api;

import java.nio.charset.Charset;

/**
 * 请求实例封装
 */
public interface RequestEntity {

	public enum Method {
		/**
		 * get方式提交
		 */
		GET,
		/**
		 * post方式提交，不支持文件上传
		 */
		POST,
		/**
		 * Query参数用json的格式发送
		 */
		POST_JSON
	}

	public static final String MIME_IMAGE = "image/*";
	
	public RequestEntity addReqeustHeader(String name, Object value);

	public RequestEntity setAction(String prefix, String action);

	public RequestEntity setCharset(Charset charset);

	public RequestEntity setAction(String action);

	/**
	 * @param method
	 *            决定提交方式，仅仅当method={@link Method#POST}的时候支持文件传递
	 * @return
	 */
	public RequestEntity setMethod(Method method);

	public RequestEntity addQuery(String name, byte value);

	public RequestEntity addQuery(String name, char value);

	public RequestEntity addQuery(String name, int value);

	public RequestEntity addQuery(String name, long value);

	public RequestEntity addQuery(String name, float value);

	public RequestEntity addQuery(String name, double value);

	public RequestEntity addQuery(String name, String value);

	public RequestEntity addQuery(String name, Object value);
	
	public RequestEntity addJSONQuery(String value);

	/**
	 * 该方法仅仅在{@link Method#POST}方式的时候起作用，否则提交的时候会报错,请参考
	 * {@link RequestEntity#setMethod(Method)}
	 * 
	 * @param name
	 * @param filePath
	 * @param mime
	 * @return
	 */
	public RequestEntity addFile(String name, String filePath, String mime);

	public class UnsupportUploadFileException extends RuntimeException {
		private static final long serialVersionUID = -8618015003656800874L;

		public UnsupportUploadFileException() {
			super();
		}

		public UnsupportUploadFileException(String detailMessage) {
			super(detailMessage);
		}

	}
}
