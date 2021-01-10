package com.example.meme.listitem;

public class IconTextListItem {
	private int image;
	private String description;
	private int descriptionColor;
	private String detail;


	public IconTextListItem(int image, String description, int descriptionColor, String detail){
		this.image = image;
		this.description = description;
		this.descriptionColor = descriptionColor;
		this.detail = detail;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDescriptionColor() {
		return descriptionColor;
	}

	public void setDescriptionColor(int descriptionColor) {
		this.descriptionColor = descriptionColor;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}
}
