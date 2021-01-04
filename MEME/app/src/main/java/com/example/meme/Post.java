package com.example.meme;

import java.util.ArrayList;

public class Post {
	private String postID;
	private String email;
	private String timeCreated;
	private String username;
	private String description;
	private ArrayList<String> tags;
	private String category;
	private String base64Image;
	private String imageURL;
	private int numberOfUpvotes;
	private int numberOfDownvotes;
	private int numberOfComments;
	private int upvoteImage;
	private int downvoteImage;

	Post() {
		postID = "";
		email = "";
		description = "";
		timeCreated = "";
		username = "";
		tags = null;
		category = "";
		base64Image = "";
		imageURL = "";
		numberOfUpvotes = 0;
		numberOfDownvotes = 0;
		numberOfComments = 0;
		upvoteImage = R.drawable.empty_heart;
		downvoteImage = R.drawable.empty_dislike;
	}

	String getPostID() {
		return postID;
	}

	void setPostID(String postID) {
		this.postID = postID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	int getNumberOfUpVotes() {
		return numberOfUpvotes;
	}

	void setNumberOfUpVotes(int numberOfUpVotes) {
		this.numberOfUpvotes = numberOfUpVotes;
	}

	int getNumberOfDownVotes() {
		return numberOfDownvotes;
	}

	void setNumberOfDownVotes(int numberOfDownVotes) {
		this.numberOfDownvotes = numberOfDownVotes;
	}

	int getNumberOfComments() {
		return numberOfComments;
	}

	void setNumberOfComments(int numberOfComments) {
		this.numberOfComments = numberOfComments;
	}

	String getBase64Image() {
		return base64Image;
	}

	void setBase64Image(String base64Image) {
		this.base64Image = base64Image;
	}

	ArrayList<String> getTags() {
		return tags;
	}

	void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	String getCategory() {
		return category;
	}

	void setCategory(String category) {
		this.category = category;
	}

	String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	void setUpvoteImage(int upvoteImage) {
		this.upvoteImage = upvoteImage;
	}

	int getDownvoteImage() {
		return downvoteImage;
	}

	void setDownvoteImage(int downvoteImage) {
		this.downvoteImage = downvoteImage;
	}
}
