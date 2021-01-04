package com.example.meme;

public class SearchItem {
	private String imageURL;
	private String username;
	private String name;
	private String email;

	public SearchItem() {

	}

	public SearchItem(String username, String name, String email, String imageURL) {
		this.username = username;
		this.name = name;
		this.email = email;
		this.imageURL = imageURL;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
