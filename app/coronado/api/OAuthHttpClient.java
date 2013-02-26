package coronado.api;
import java.io.IOException;

import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpExchange;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.jetty.JettyOAuthConsumer;

public class OAuthHttpClient {
	private final OAuthConsumer consumer;
	private final HttpClient client;

	public OAuthHttpClient(final SecretKeys keys) throws Exception {
		// create a consumer object and configure it with the access
		// token and token secret obtained from the service provider
		consumer = new JettyOAuthConsumer(keys.CONSUMER_KEY, keys.CONSUMER_SECRET);
		consumer.setTokenWithSecret(keys.ACCESS_TOKEN, keys.ACCESS_TOKEN_SECRET);

		// send the request
		client = new HttpClient();
		client.start();
	}
	
	public void SendRequest(HttpExchange request)
			throws OAuthMessageSignerException,
			OAuthExpectationFailedException, OAuthCommunicationException,
			IOException {
		consumer.sign(request);
		client.send(request);
	}
}