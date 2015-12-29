package xyz.mosd.http.engine.api;

import java.util.Map;

public interface JsonFormatHandler {
	public String toJson(Map<String, Object> fieldsMap);
}