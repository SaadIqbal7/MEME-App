package com.example.meme;

public class ChatItem {
	private ChatMessage message;
	private TimeTag timeTag;
	private int viewType;

	public ChatItem() {
		message = null;
		timeTag = null;
		viewType = -1;
	}

	public ChatItem(ChatMessage message, int viewType) {
		this.message = message;
		this.viewType = viewType;
		timeTag = null;
	}

	public ChatItem(TimeTag timeTag) {
		this.timeTag = timeTag;
		this.viewType = 2;
		message = null;
	}

	public ChatMessage getMessage() {
		return message;
	}

	public void setMessage(ChatMessage message) {
		this.message = message;
	}

	public TimeTag getTimeTag() {
		return timeTag;
	}

	public void setTimeTag(TimeTag timeTag) {
		this.timeTag = timeTag;
	}

	public int getViewType() {
		return viewType;
	}

	public void setViewType(int viewType) {
		this.viewType = viewType;
	}
}
