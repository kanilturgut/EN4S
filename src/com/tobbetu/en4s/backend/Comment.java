/**
 * Kadir Anil Turgut
 * 
 * Comment listesini gostermek icin gereken comment sinifi. Basecamp teki API ye gore
 * yazmaya calisiyorum ama daha sonra gozden gecirelim.
 * 
 */
package com.tobbetu.en4s.backend;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Comment implements Serializable {

    private static final long serialVersionUID = 5661075950176963308L;
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

    public String toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("text", this.text);
        } catch (JSONException e) {
            // TODO: handle exception
        }

        return obj.toString();
    }

    public static Comment fromJSON(String json) throws JSONException {
        return Comment.fromJSON(new JSONObject(json));
    }

    public static Comment fromJSON(JSONObject elem) throws JSONException {
        Comment obj = new Comment();

        obj.setId(elem.getString("_id"));
        obj.setAuthor(User.fromJSON(elem.getJSONObject("author")));
        obj.setText(elem.getString("text"));
        obj.setLike(elem.getInt("like"));
        obj.setDislike(elem.getInt("dislike"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        try {
            obj.setDate(df.parse(elem.optString("date")));
        } catch (ParseException e) {
            Log.e("Comment.fromJSON",
                    "Date Parse Error: " + elem.optString("date"), e);
        }

        return obj;
    }

    public static List<Comment> getComments(Complaint c) throws IOException,
            JSONException {
        List<Comment> list = new LinkedList<Comment>();

        HttpResponse get = Requests.get("/comments/" + c.getId());

        if (!Requests.checkStatusCode(get, HttpStatus.SC_OK)) {
            Log.e("Complaint.getHotList", "[ERROR] Status Code: "
                    + get.getStatusLine().getStatusCode());
            throw new HttpResponseException(
                    get.getStatusLine().getStatusCode(),
                    "Status is not equal 200");
        }
        String response = Requests.readResponse(get);

        JSONArray results = new JSONArray(response);
        for (int i = 0; i < results.length(); i++) {
            JSONObject item = results.getJSONObject(i);
            list.add(Comment.fromJSON(item));
        }

        return list;
    }
}
