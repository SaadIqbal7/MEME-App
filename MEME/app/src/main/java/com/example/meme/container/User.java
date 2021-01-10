package com.example.meme.container;

public class User {
	private String description;
	private String dateOfBirth;
	private String email;
	private String fullName;
	private String gender;
	private String imageURL;
	private String location;
	private int numberOfPosts;
	private int numberOfSavedPosts;
	private int numberOfUpvotedPosts;
	private int onlineStatus;
	private String userRank;
	private int userScore;
	private String username;

	public User() {
		description = "";
		dateOfBirth = "";
		email = "";
		fullName = "";
		gender = "";
		imageURL = "";
		location = "";
		numberOfPosts = 0;
		numberOfSavedPosts = 0;
		numberOfUpvotedPosts = 0;
		onlineStatus = 1;
		userRank = "";
		userScore = 0;
		username = "";
	}

	public User(String description, String dateOfBirth, String email, String fullName, String gender, String imageURL, String location, int numberOfPosts, int numberOfSavedPosts, int numberOfUpvotedPosts, int onlineStatus, String userRank, int userScore, String username) {
		this.description = description;
		this.dateOfBirth = dateOfBirth;
		this.email = email;
		this.fullName = fullName;
		this.gender = gender;
		this.imageURL = imageURL;
		this.location = location;
		this.numberOfPosts = numberOfPosts;
		this.numberOfSavedPosts = numberOfSavedPosts;
		this.numberOfUpvotedPosts = numberOfUpvotedPosts;
		this.onlineStatus = onlineStatus;
		this.userRank = userRank;
		this.userScore = userScore;
		this.username = username;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getNumberOfPosts() {
		return numberOfPosts;
	}

	public void setNumberOfPosts(int numberOfPosts) {
		this.numberOfPosts = numberOfPosts;
	}

	public int getNumberOfSavedPosts() {
		return numberOfSavedPosts;
	}

	public void setNumberOfSavedPosts(int numberOfSavedPosts) {
		this.numberOfSavedPosts = numberOfSavedPosts;
	}

	public int getNumberOfUpvotedPosts() {
		return numberOfUpvotedPosts;
	}

	public void setNumberOfUpvotedPosts(int numberOfUpvotedPosts) {
		this.numberOfUpvotedPosts = numberOfUpvotedPosts;
	}

	public int getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(int onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public String getUserRank() {
		return userRank;
	}

	public void setUserRank(String userRank) {
		this.userRank = userRank;
	}

	public int getUserScore() {
		return userScore;
	}

	public void setUserScore(int userScore) {
		this.userScore = userScore;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
