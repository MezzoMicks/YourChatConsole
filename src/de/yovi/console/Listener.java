package de.yovi.console;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

public class Listener {


	private final HttpClient client;
	private final HttpContext context;
	
	
	public Listener(HttpContext context) {
		this.client = new DefaultHttpClient();
		this.context = context;
	}
	
	
	public void read(OutputStream out) {
		try {
			HttpPost post = new HttpPost("http://localhost:8080/YourChatWeb/listen");
			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(1);
			params.add(new BasicNameValuePair("output", "register"));
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse response = client.execute(post, context);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			HttpEntity entity = response.getEntity();
			entity.writeTo(bos);
			String listenKey = new String(bos.toByteArray());
			new ListenerThread(client, listenKey, out).start();
//		BufferedReader br = new BufferedReader(new InputStreamReader(in))
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class ListenerThread extends Thread {
		
		private final HttpClient client;
		private final String id;
		private final OutputStream out;
		
		public ListenerThread(HttpClient client, String id, OutputStream out) {
			this.client = client;
			this.id = id;
			this.out = out;
		}
		
		@Override
		public void run() {
			try {
				HttpGet get = new HttpGet("http://localhost:8080/YourChatWeb/listen?type=async&html=false&listenid=" + id);
				while (true) {
					HttpResponse response;
					response = client.execute(get, context);
					HttpEntity entity = response.getEntity();
					entity.writeTo(out);
					Thread.sleep(250);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
