package com.example.meme;

public class ChatMessage {
	private String messageID;
	private String message;
	private String timeSent;

	public ChatMessage(){}

	public ChatMessage(int viewType, String messageID, String message, String timeSent) {
		this.messageID = messageID;
		this.message = message;
		this.timeSent = timeSent;
	}

	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTimeSent() {
		return timeSent;
	}

	public void setTimeSent(String timeSent) {
		this.timeSent = timeSent;
	}

}
