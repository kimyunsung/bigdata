package com.sist.naver;

import javax.xml.bind.annotation.XmlElement;

public class Item {
	private String title;
	private String link;
	private String description;
	private String pubDate;
	
	
	public String getPubDate() {
		return pubDate;
	}

	@XmlElement
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public String getTitle() {
		return title;
	}

	@XmlElement
	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	@XmlElement
	public void setLink(String link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}

	@XmlElement
	public void setDescription(String description) {
		this.description = description;
	}

}
