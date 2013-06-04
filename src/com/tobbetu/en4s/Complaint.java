package com.tobbetu.en4s;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Complaint implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4700299102770387240L;
	private String id;
	private String title;
	private String date;
	private String reporter;
	private String category;
	private int upVote;
	private int downVote;
	private double latitude;
	private double longitude;
	private String address;
	private String city;
	
	public Complaint(String title, String date, String address){
		this.title = title;
		this.date = date;
		this.address = address;
	}
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id = id;
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
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getReporter() {
		return reporter;
	}
	public void setReporter(String reporter) {
		this.reporter = reporter;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String toJSON() {
        JSONObject newObj = new JSONObject();
        try {
            newObj.put("title", this.title);
            newObj.put("date", this.date);
            newObj.put("reporter", this.reporter);
            newObj.put("category", this.category);
            newObj.put("upvote", this.upVote);
            newObj.put("downvote", this.downVote);
            newObj.put("latitude", this.latitude);
            newObj.put("longtitude", this.longitude);
            newObj.put("city", this.city);
            newObj.put("address", this.address);
        } catch (JSONException e) {
            Log.e("Complaint", "JSONException Exception", e);
        }
        return newObj.toString();
    }

}
