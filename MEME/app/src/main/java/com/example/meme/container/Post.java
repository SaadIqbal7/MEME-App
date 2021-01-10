package com.example.meme.container;

import com.example.meme.R;

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

	public Post() {
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

	public String getPostID() {
		return postID;
	}

	public void setPostID(String postID) {
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

	public int getNumberOfUpVotes() {
		return numberOfUpvotes;
	}

	public void setNumberOfUpVotes(int numberOfUpVotes) {
		this.numberOfUpvotes = numberOfUpVotes;
	}

	public int getNumberOfDownVotes() {
		return numberOfDownvotes;
	}

	public void setNumberOfDownVotes(int numberOfDownVotes) {
		this.numberOfDownvotes = numberOfDownVotes;
	}

	public int getNumberOfComments() {
		return numberOfComments;
	}

	public void setNumberOfComments(int numberOfComments) {
		this.numberOfComments = numberOfComments;
	}

	public String getBase64Image() {
		return base64Image;
	}

	public void setBase64Image(String base64Image) {
		this.base64Image = base64Image;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getUsername() {
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
