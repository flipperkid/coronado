package coronado.api;

import java.io.IOException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.client.ContentExchange;

public class SyncRequest {
	private static final String baseUri = "https://api.tradeking.com/v1/";
	private final OAuthHttpClient client;
	private final ContentExchange request;

	public SyncRequest(final String url, final OAuthHttpClient client) {
		this.client = client;
		request = new ContentExchange(true) {
			@Override
			protected void onResponseComplete() throws IOException {
				int status = getResponseStatus();
				if (status != 200) {
					System.out.println("Error Code Received: " + status);
				}
			}
		};

		// setup the request
		request.setMethod(HttpMethods.GET);
		request.setURL(baseUri + url);
	}

	public void setContent(final String content) {
		request.setMethod(HttpMethods.POST);
		request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		request.setRequestContent(new ByteArrayBuffer(content));
	}

	public String send() throws OAuthMessageSignerException,
	OAuthExpectationFailedException, OAuthCommunicationException,
	IOException, InterruptedException {
		client.SendRequest(request);
		request.waitForDone();
		String response = request.getResponseContent();
		return response;
	}
}