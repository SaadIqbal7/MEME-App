package com.example.meme;

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

	Comment() {
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

	void setPostId(String postId) {
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

	int getNumberOfUpvotes() {
		return numberOfUpvotes;
	}

	void setNumberOfUpvotes(int numberOfUpvotes) {
		this.numberOfUpvotes = numberOfUpvotes;
	}

	int getNumberOfDownvotes() {
		return numberOfDownvotes;
	}

	void setNumberOfDownvotes(int numberOfDownvotes) {
		this.numberOfDownvotes = numberOfDownvotes;
	}

	int getNumberOfReplies() {
		return numberOfReplies;
	}

	void setNumberOfReplies(int numberOfReplies) {
		this.numberOfReplies = numberOfReplies;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	String getTimeCreated() {
		return timeCreated;
	}

	void setTimeCreated(String timeCreated) {
		this.timeCreated = timeCreated;
	}

	int getUpvoteImage() {
		return upvoteImage;
	}

	public void setUpvoteImage(int upvoteImage) {
		this.upvoteImage = upvoteImage;
	}

	int getDownvoteImage() {
		return downvoteImage;
	}

	public void setDownvoteImage(int downvoteImage) {
		this.downvoteImage = downvoteImage;
	}
}
