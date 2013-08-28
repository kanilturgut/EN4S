/**
 * Kadir Anil Turgut
 * 
 * Comment listesini gostermek icin gereken comment sinifi. Basecamp teki API ye gore
 * yazmaya calisiyorum ama daha sonra gozden gecirelim.
 * 
 */
package com.tobbetu.en4s.backend;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Comment {

    private String id;
    private User author;
    private String text;

    private Date date;

    private int like;
    private int dislike;

    // for future
    // private Image userAvatar;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void save(Complaint c) throws IOException {
        Log.d("[JSON]", this.toJSON());
        HttpResponse post = Requests.post(
                "http://en4s.msimav.net/comment/" + c.getId(), this.toJSON());
        if (!Requests.checkStatusCode(post, HttpStatus.SC_CREATED)) {
            // TODO throw exception
            Log.d("Comment", "Status Code in not 201");
        }

        try {
            Comment response = Comment.fromJSON(new JSONObject(Requests
                    .readResponse(post)));

            this.id = response.id;
            this.author = response.author;
            this.text = response.text;
            this.date = response.date;
            this.like = response.like;
            this.dislike = response.dislike;
        } catch (JSONException e) {
            Log.e("Comment", "Impossible JSONException throwed", e);
        }

    }

    public String toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("text", this.text);
        } catch (JSONException e) {
            // TODO: handle exception
        }

        return obj.toString();
    }

    public static Comment fromJSON(String json) {
        try {
            return Comment.fromJSON(new JSONObject(json));
        } catch (JSONException e) {
            Log.e("Comment", "JSONException on Comment.fromJSON", e);
            return null;
        }
    }

    public static Comment fromJSON(JSONObject elem) {
        Comment obj = new Comment();

        obj.setId(elem.optString("_id"));
        obj.setAuthor(User.fromJSON(elem.optJSONObject("author")));
        obj.setText(elem.optString("text"));
        obj.setLike(elem.optInt("like"));
        obj.setDislike(elem.optInt("dislike"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        try {
            obj.setDate(df.parse(elem.optString("date")));
        } catch (ParseException e) {
            Log.e("Comment.fromJSON",
                    "Date Parse Error: " + elem.optString("date"), e);
        }

        return obj;
    }

    public static List<Comment> getComments(Complaint c) throws IOException {
        List<Comment> list = new LinkedList<Comment>();

        HttpResponse get = Requests.get("http://en4s.msimav.net/comments/"
                + c.getId());

        if (!Requests.checkStatusCode(get, HttpStatus.SC_OK))
            Log.e("Complaint.getHotList", "[ERROR] Status Code: "
                    + get.getStatusLine().getStatusCode());
        String response = Requests.readResponse(get);

        try {
            JSONArray results = new JSONArray(response);
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                list.add(Comment.fromJSON(item));
            }
        } catch (JSONException e) {
            Log.e("Complaint.parseList", "Unexpected JSON Error", e);
        }

        return list;
    }

}
