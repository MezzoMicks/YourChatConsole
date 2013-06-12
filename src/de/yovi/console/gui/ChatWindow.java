package de.yovi.console.gui;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jcurses.system.CharColor;
import jcurses.system.Toolkit;
import jcurses.widgets.DefaultLayoutManager;
import jcurses.widgets.List;
import jcurses.widgets.TextArea;
import jcurses.widgets.TextField;
import jcurses.widgets.TextField.TextFieldListener;
import jcurses.widgets.WidgetsConstants;
import jcurses.widgets.Window;
import de.yovi.console.ChatConnector;
import de.yovi.console.InputListener;

public class ChatWindow implements TextFieldListener {
											// b      kb     mb
	private final static int BUFFER_PADDING = 1024 * 1024 * 8;
	private final static int MAX_BUFFER_SIZE = 1024 * 1024 * 64;
	private final ChatOutputStream chaoutStream = new ChatOutputStream();
	
	private Window window;
	private TextArea chatout = null;
	private TextField chatin = null;
	private List users = null;
	private final ChatConnector connector;
	
	
	public ChatWindow(ChatConnector connector) {
		this.connector = connector;
		build();
	}
	
	public void build() {
		int width = Toolkit.getScreenWidth();
		System.out.println("building window " + width);
		window = new Window(width, Toolkit.getScreenHeight(), true, "YourChatConsole");
		int chatWidth = (int) ((width / (double) 4) * 3) - 4;
		int chatHeight = Toolkit.getScreenHeight() - 4;
		chatout = new TextArea(chatWidth, chatHeight);
		chatout.setColors(new CharColor(CharColor.BLACK, CharColor.WHITE));
		chatout.setEditable(false);
		DefaultLayoutManager lm = new DefaultLayoutManager();
		window.getRootPanel().setLayoutManager(lm);
		lm.addWidget(chatout, 0, 0, chatWidth, chatHeight, WidgetsConstants.ALIGNMENT_CENTER, WidgetsConstants.ALIGNMENT_CENTER);
		chatin = new TextField(chatWidth);
		lm.addWidget(chatin, 0, chatHeight, chatWidth, 3, WidgetsConstants.ALIGNMENT_LEFT, WidgetsConstants.ALIGNMENT_CENTER);
		chatin.setEditable(true);
		chatin.addTextFieldListener(this);
		users = new List();
		window.show();
		chatin.getFocus();
	}
	
	public OutputStream getChatOutStream() {
		return chaoutStream;
	}

	@Override
	public void enter() {
		String text = chatin.getText();
		if (!text.isEmpty()) {
			connector.send(text);
			chatin.setText("");
		}
	}

	private class RefreshThread extends Thread {
		
		@Override
		public void run() {
			boolean interrupted = false;
			while (!interrupted) {
				try {
					JSONObject data = connector.refresh();
					users.clear();
					JSONArray dataUsers = data.getJSONArray("users");
					int i = 0;
					while (i < dataUsers.length())  {
						JSONObject dataUser = dataUsers.getJSONObject(i);
						users.add(dataUser.getString("username"));
					}
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					interrupted = true;
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	

	private class ChatOutputStream extends OutputStream {
			
		private boolean command;
		private int count = 0;
		StringBuilder buffer = new StringBuilder(MAX_BUFFER_SIZE);
		private String commandBuffer = "";
		
		@Override
		public void write(int b) throws IOException {
			count++;
			if (count > MAX_BUFFER_SIZE) {
				StringBuilder newBuffer = new StringBuilder(MAX_BUFFER_SIZE);
				int brAfterPadding = buffer.indexOf("\n", BUFFER_PADDING);
				newBuffer.append(buffer.subSequence(brAfterPadding, buffer.length()));
				buffer = newBuffer;
			}
			char ch = (char) b;
			if (ch == '$') {
				command = true;
			}
			
			if (command) {
				commandBuffer += ch;
			} else {
				buffer.append(ch);
			}
			if (ch == '\n') {
				if (command) {
					command = false;
					if (commandBuffer.startsWith("$REFRESH")) {
						
					}
				} else {
					if (chatout != null) {
						chatout.setText(buffer.toString());
						window.show();
					}
				}
			}
		}
	}
	
	
}
