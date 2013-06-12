package de.yovi.console;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

public class Authentication {

	private final static Logger logger = Logger.getLogger("Login");
	
	private MessageDigest digest = null;
	
	private final HttpClient client;
	private final HttpContext context;
	
	public Authentication(HttpClient client, HttpContext context) {
		this.client = client;
		this.context = context;
	}
	
	public boolean login(String username, String password) throws ClientProtocolException, IOException, URISyntaxException {
		System.out.println("building Post");
		HttpPost post = new HttpPost();
		post.setURI(new URI("http://localhost:8080/YourChatWeb/session"));
		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("action", "sugar"));
		params.add(new BasicNameValuePair("user", "Michi"));
		post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		System.out.println("executing");
		HttpResponse response = client.execute(post, context);
		StatusLine statusLine = response.getStatusLine();
		System.out.println(statusLine.getStatusCode() + ":" + statusLine.getReasonPhrase());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		HttpEntity entity = response.getEntity();
		entity.writeTo(bos);
		JSONObject jso;
		String sugar;
		try {
			jso = new JSONObject( new String(bos.toByteArray()));
			sugar = jso.getString("sugar");
			logger.info("got sugar:" + sugar);
		} catch (JSONException e) {
			sugar = null;
		}
		if (sugar != null) {
			post = new HttpPost();
			post.setURI(new URI("http://localhost:8080/YourChatWeb/session"));
			params = new ArrayList<NameValuePair>(3);
			params.add(new BasicNameValuePair("action", "login"));
			params.add(new BasicNameValuePair("username", username));
			String shaPass = sha(password, null);
			String shaSugarPass = sha(shaPass, sugar);
			params.add(new BasicNameValuePair("passwordHash", shaSugarPass));
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			logger.info("Logging " + username + " in");
			response = client.execute(post, context);
			response.getEntity().writeTo(new OutputStream() {
				@Override
				public void write(int b) throws IOException {}
			});
			statusLine = response.getStatusLine();
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized String sha(String input, String sugar) {
		String result = null;
		if (input != null) {
			try {
				if (digest == null) {
					digest = MessageDigest.getInstance("SHA-256");
				} else {
					digest.reset();
				}
				if (sugar != null) {
					digest.update(sugar.getBytes());
				}
				digest.update(input.getBytes());
				byte[] output = digest.digest();
				result = bytesToHex(output);
			} catch (NoSuchAlgorithmException e) {
				logger.severe("Error while creating a MessageDigester for algorithm: " + "SHA-256");
			} finally {
				if (digest != null) {
					digest.reset();
				}
			}
		}
		return result;
	}
	
	private static String bytesToHex(byte[] bytes) {
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
}
