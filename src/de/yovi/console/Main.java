package de.yovi.console;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import de.yovi.console.gui.ChatWindow;

public class Main {

	public static void main(String[] args) {
		try {
			System.out.println("starting client");
			DefaultHttpClient client = new DefaultHttpClient();
			
			System.out.println("setting cookie store");
			HttpContext context = new BasicHttpContext();
			CookieStore cookieStore = new BasicCookieStore();  
			context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);  
  
			boolean login = new Authentication(client, context).login("Michi", "frekie");
			final ChatConnector connector = new ChatConnector(client, context);
			if (login) {
				ChatWindow chatWindow = new ChatWindow(connector);
				new Listener(context).read(chatWindow.getChatOutStream());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	
}
