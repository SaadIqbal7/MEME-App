package com.example.meme.container;

public class MemeTemplate {
	private String name;
	private String imageURL;

	public MemeTemplate() {
		name = "";
		imageURL = "";
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
