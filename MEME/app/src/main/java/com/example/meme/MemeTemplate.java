package com.example.meme;

class MemeTemplate {
	private String name;
	private String imageURL;

	MemeTemplate() {
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
