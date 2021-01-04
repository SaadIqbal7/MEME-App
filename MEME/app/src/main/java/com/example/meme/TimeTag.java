package com.example.meme;

public class TimeTag {
	private String day;
	private int date;
	private int month;

	public TimeTag(){}

	public TimeTag(String day, int date, int month) {
		this.day = day;
		this.date = date;
		this.month = month;
	}


	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}
}
