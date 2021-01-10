package com.example.meme.container;

import com.example.meme.R;

public class Comment {
	private String commentId;
	private String postId;
	private String email;
	private String parent;
	private String description;
	private int numberOfUpvotes;
	private int numberOfDownvotes;
	private int numberOfReplies;
	private String username;
	private String timeCreated;
	private int upvoteImage;
	private int downvoteImage;

	public Comment() {
		commentId = "";
		postId = "";
		email = "";
		parent = "";
		description = "";
		numberOfUpvotes = 0;
		numberOfDownvotes = 0;
		numberOfReplies = 0;
		username = "";
		timeCreated = "";
		upvoteImage = R.drawable.empty_heart;
		downvoteImage = R.drawable.empty_dislike;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getNumberOfUpvotes() {
		return numberOfUpvotes;
	}

	public void setNumberOfUpvotes(int numberOfUpvotes) {
		this.numberOfUpvotes = numberOfUpvotes;
	}

	public int getNumberOfDownvotes() {
		return numberOfDownvotes;
	}

	public void setNumberOfDownvotes(int numberOfDownvotes) {
		this.numberOfDownvotes = numberOfDownvotes;
	}

	public int getNumberOfReplies() {
		return numberOfReplies;
	}

	public void setNumberOfReplies(int numberOfReplies) {
		this.numberOfReplies = numberOfReplies;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(String timeCreated) {
		this.timeCreated = timeCreated;
	}

	public int getUpvoteImage() {
		return upvoteImage;
	}

	public void setUpvoteImage(int upvoteImage) {
		this.upvoteImage = upvoteImage;
	}

	public int getDownvoteImage() {
		return downvoteImage;
	}

	public void setDownvoteImage(int downvoteImage) {
		this.downvoteImage = downvoteImage;
	}
}
