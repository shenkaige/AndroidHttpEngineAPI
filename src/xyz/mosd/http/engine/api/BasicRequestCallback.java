package xyz.mosd.http.engine.api;

public abstract class BasicRequestCallback<Result> implements RequestCallback2<Result> {

	@Override
	public void beforeResult(Result result, Object reqId) {
	}

	@Override
	public void afterResult(Result result, Object reqId) {
	}
}
