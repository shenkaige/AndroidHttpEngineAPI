package xyz.mosd.http.api;

public interface RequestCallback2<Result> extends RequestCallback<Result> {

	public void afterResult(Result result, Object reqId);

	public void beforeResult(Result result, Object reqId);
}