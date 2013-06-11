package com.tobbetu.en4s.backend;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Complaint implements Serializable {

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
    private List<String> imageURLs = null;
    private List<Image> images = null;

    public Complaint() {
    }

    public Complaint(String title, String date, String address) {
        this.title = title;
        this.date = date;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public void setImages(List<String> images) {
        this.imageURLs = images;
    }

    public int imageCount() {
        return imageURLs.size();
    }

    public Image getImage(int index) throws IOException {
        if (index > imageURLs.size())
            throw new IndexOutOfBoundsException();
        if (images == null)
            images = new ArrayList<Image>(imageURLs.size());
        if (images.get(index) == null)
            images.set(index, Image.download(imageURLs.get(index)));
        return images.get(index);

    }

    public void save() throws IOException {
        Log.d("[JSON]", this.toJSON());
        HttpResponse post = Requests.post("http://en4s.msimav.net/complaint",
                this.toJSON());
        if (!Requests.checkStatusCode(post, HttpStatus.SC_CREATED)) {
            // TODO throw exception
            Log.d(getClass().getName(), "Status Code in not 201");
        }
        try {
            Complaint response = fromJSON(new JSONObject(
                    Requests.readResponse(post)));
            this.id = response.id;
            this.date = response.date;
            this.reporter = response.reporter;
            this.upVote = response.upVote;
            this.downVote = response.downVote;

            // TODO save image too
        } catch (JSONException e) {
            Log.e(getClass().getName(), "Impossible JSONException throwed", e);
        }
    }

    public String toJSON() {
        JSONObject newObj = new JSONObject();
        try {
            newObj.put("title", this.title);
            newObj.put("category", this.category);
            newObj.put("city", this.city);
            newObj.put("address", this.address);

            JSONArray geo = new JSONArray();
            geo.put(this.latitude);
            geo.put(this.longitude);
            newObj.put("location", geo);
        } catch (JSONException e) {
            Log.e("Complaint.toJSON", "Unexpected JSONException", e);
        }
        return newObj.toString();
    }

    private static Complaint fromJSON(JSONObject elem) {
        Complaint obj = new Complaint();

        obj.setId(elem.optString("_id"));
        obj.setTitle(elem.optString("title"));
        obj.setReporter(elem.optString("user"));
        obj.setCategory(elem.optString("category"));
        obj.setUpVote(elem.optInt("upvote_count", 0));
        obj.setDownVote(elem.optInt("downvote_count", 0));
        obj.setAddress(elem.optString("address"));
        obj.setCity(elem.optString("city"));

        JSONArray geo = elem.optJSONArray("location");
        obj.setLatitude(geo.optDouble(0, 0));
        obj.setLongitude(geo.optDouble(1, 0));

        if (elem.has("pics")) {
            JSONArray pics = elem.optJSONArray("pics");
            ArrayList<String> picsList = new ArrayList<String>();
            for (int i = 0; i < pics.length(); i++) {
                picsList.add(pics.optString(i));
            }
            obj.setImages(picsList);
        }

        return obj;
    }

    private static List<Complaint> parseList(String jsonResponse) {
        List<Complaint> list = new LinkedList<Complaint>();
        try {
            JSONArray results = new JSONArray(jsonResponse);
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                list.add(Complaint.fromJSON(item));
            }
        } catch (JSONException e) {
            Log.e("Complaint.parseList", "Unexpected JSON Error", e);
        }
        return list;
    }

    public static List<Complaint> getHotList() throws IOException {
        // TODO not forget to change that
        HttpResponse get = Requests
                .get("http://en4s.msimav.net/complaint/recent");

        if (!Requests.checkStatusCode(get, HttpStatus.SC_OK))
            Log.e("Complaint.getHotList", "[ERROR] Status Code: "
                    + get.getStatusLine().getStatusCode());
        String response = Requests.readResponse(get);
        return Complaint.parseList(response);
    }

    public static List<Complaint> getNewList() throws IOException {
        HttpResponse get = Requests
                .get("http://en4s.msimav.net/complaint/recent");

        if (!Requests.checkStatusCode(get, HttpStatus.SC_OK))
            Log.e("Complaint.getHotList", "[ERROR] Status Code: "
                    + get.getStatusLine().getStatusCode());
        String response = Requests.readResponse(get);
        return Complaint.parseList(response);
    }

    public static List<Complaint> getTopList() throws IOException {
        HttpResponse get = Requests.get("http://en4s.msimav.net/complaint/top");

        if (!Requests.checkStatusCode(get, HttpStatus.SC_OK))
            Log.e("Complaint.getHotList", "[ERROR] Status Code: "
                    + get.getStatusLine().getStatusCode());
        String response = Requests.readResponse(get);
        return Complaint.parseList(response);
    }

    public static List<Complaint> getNearList(double lat, double lon)
            throws IOException {
        HttpResponse get = Requests
                .get(String
                        .format("http://en4s.msimav.net/complaint/near?latitude=%s&longitude=%s",
                                Double.toString(lat), Double.toString(lon)));
        if (!Requests.checkStatusCode(get, HttpStatus.SC_OK))
            Log.e("Complaint.getHotList", "[ERROR] Status Code: "
                    + get.getStatusLine().getStatusCode());
        String response = Requests.readResponse(get);
        return Complaint.parseList(response);
    }
}
