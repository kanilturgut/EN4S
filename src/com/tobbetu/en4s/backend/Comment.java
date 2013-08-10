/**
 * Kadir Anil Turgut
 * 
 * Comment listesini gostermek icin gereken comment sinifi. Basecamp teki API ye gore
 * yazmaya calisiyorum ama daha sonra gozden gecirelim.
 * 
 */
package com.tobbetu.en4s.backend;

public class Comment {

	private String id;
	private String author;
	private String text;

	private int upVote;
	private int downVote;

	// for future
	// private Image userAvatar;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getUpVote() {
		return upVote;
	}

	public void setUpVote(int upVote) {
		this.upVote = upVote;
	}

	public int getDownVote() {
		return downVote;
	}

	public void setDownVote(int downVote) {
		this.downVote = downVote;
	}

}
