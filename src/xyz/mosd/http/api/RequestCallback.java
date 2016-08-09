package xyz.mosd.http.api;

import java.io.IOException;

public interface RequestCallback<Result> {

	public Result onResponseInBackground(String resp, Object reqId) throws Exception;

	public void onResult(Result result, Object reqId);

	public void onFailure(IOException e, Object reqId);

	// public ResultParser<Result> getResultParser();
	//
	// public interface ResultParser<T> {
	// public T handlerResponse(String resp);
	// }

}