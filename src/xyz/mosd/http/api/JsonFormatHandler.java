package xyz.mosd.http.api;

import java.util.Map;

public interface JsonFormatHandler {
	public String toJson(Map<String, Object> fieldsMap);
}