package de.yovi.console;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

public class ChatConnector {

	private final HttpClient client;
	private final HttpContext context;
	
	public ChatConnector(HttpClient client, HttpContext context) {
		this.client = client;
		this.context = context;
	}
	
	public JSONObject refresh() {
		JSONObject result = null;
		try {
			HttpGet get = new HttpGet("http://localhost:8080/YourChatWeb/action?action=refresh");
			// Request parameters and other properties.
			synchronized (client) {
				HttpResponse response = client.execute(get, context);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				response.getEntity().writeTo(bos);
				result = new JSONObject(new String(bos.toByteArray()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void send(String text) {
		try {
			HttpPost post = new HttpPost("http://localhost:8080/YourChatWeb/input");
			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("action", "talk"));
			params.add(new BasicNameValuePair("message", text));
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			synchronized (client) {
				HttpResponse response = client.execute(post, context);
				response.getEntity().writeTo(new OutputStream() {
					@Override
					public void write(int b) throws IOException {}
				});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
