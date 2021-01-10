package com.example.meme.listitem;

public class ConversationItem {
	private String conversationID;
	private String name;
	private String imageURL;
	private String email;
	private String lastMessage;
	private String lastMessageTime;

	public ConversationItem(){}

	public ConversationItem(String conversationID, String name, String email, String lastMessage, String lastMessageTime, String imageURL) {
		this.conversationID = conversationID;
		this.name = name;
		this.email = email;
		this.lastMessage = lastMessage;
		this.lastMessageTime = lastMessageTime;
		this.imageURL = imageURL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public String getLastMessageTime() {
		return lastMessageTime;
	}

	public void setLastMessageTime(String lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}

	public String getConversationID() {
		return conversationID;
	}

	public void setConversationID(String conversationID) {
		this.conversationID = conversationID;
	}
}
