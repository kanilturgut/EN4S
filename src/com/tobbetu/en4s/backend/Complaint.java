package com.tobbetu.en4s.backend;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.Utils;
import com.tobbetu.en4s.cache.Cache;
import com.tobbetu.en4s.helpers.VoteRejectedException;

public class Complaint implements Serializable {

    private static final long serialVersionUID = -4700299102770387240L;

    private String id;
    private String title;
    private Date date;
    private User reporter;
    private String category;
    private int upVote;
    private int downVote;
    private Set<String> upvoters;
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private List<String> imageURLs = null;
    private List<Image> images = new ArrayList<Image>();
    private List<Comment> comments;

    public Complaint() {
    }

    public Complaint(String title, Date date, String address) {
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

    public boolean alreadyUpVoted(User me) {
        // HashSet has O(1) lookup, I hope device has enough memory
        return upvoters.contains(me.getId());
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setImages(List<String> images) {
        this.imageURLs = images;
    }

    public void addJustUploadedImage(String url) {
        if (this.imageURLs == null)
            this.imageURLs = new ArrayList<String>();
        imageURLs.add(url);
    }

    public int imageCount() {
        return imageURLs.size();
    }

    public String getDateAsString(Context ctx) {
        // WARNING this is going to be fucking ugly
        long now = System.currentTimeMillis();
        long unixtime = this.date.getTime();

        if (now - unixtime < 0) {
            return "Just Now";
        } else if (now - 60 * 1000 < unixtime) { // in fucking min
            return String.format(ctx.getString(R.string.comp_sec),
                    ((now - unixtime) / 1000));
        } else if (now - 60 * 60 * 1000 < unixtime) { // fucking hour
            return String.format(ctx.getString(R.string.comp_min),
                    ((now - unixtime) / 60 / 1000));
        } else if (now - 24 * 60 * 60 * 1000 < unixtime) { // fucking day
            return String.format(ctx.getString(R.string.comp_hour),
                    ((now - unixtime) / 60 / 60 / 1000));
        } else if (now - 7 * 24 * 60 * 60 * 1000 < unixtime) { // fucking week
            return String.format(ctx.getString(R.string.comp_day),
                    ((now - unixtime) / 24 / 60 / 60 / 1000));
        } else {
            return this.date.toString();
        }
    }

    public String getDistance(Context ctx, double lat, double lon) {
        float distance = Utils.calculateDistance(lat, lon, this.latitude,
                this.longitude);
        if (distance < 0.0001) {
            return "Just Here!";
        } else if (distance < 10000) {
            return String.format(ctx.getString(R.string.comp_meter), distance);
        } else {
            return String.format(ctx.getString(R.string.comp_km),
                    distance / 1000);
        }
    }

    public void getImage(int index, String size, ImageView iv) {
        if (index > imageURLs.size())
            throw new IndexOutOfBoundsException();

        String url = imageURLs.get(index);
        Cache.getInstance().getImage(Image.getImageURL(url, size), iv);
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

    public void upvote(String location) throws IOException,
            VoteRejectedException {
        HttpResponse put = Requests.put(String.format(
                "http://en4s.msimav.net/complaint/%s/upvote", this.id),
                location);
        if (Requests.checkStatusCode(put, HttpStatus.SC_NOT_ACCEPTABLE)) {
            Log.e(getClass().getName(), "Upvote Rejected");
            throw new VoteRejectedException("Upvote Rejected");
        } else if (Requests.checkStatusCode(put, HttpStatus.SC_NOT_FOUND)) {
            Log.e(getClass().getName(),
                    "Upvote Rejected because complaint id is wrong");
            throw new VoteRejectedException("There is no such complaint");
        }
    }

    public void downvote(String location) throws IOException {
        HttpResponse put = Requests.put(String.format(
                "http://en4s.msimav.net/complaint/%s/downvote", this.id),
                location);
        if (Requests.checkStatusCode(put, HttpStatus.SC_NOT_ACCEPTABLE)) {
            Log.e(getClass().getName(), "Downvote Rejected");
            // TODO throw new Exception("Upvote Rejected");
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
        obj.setReporter(User.fromJSON(elem.optJSONObject("user")));
        obj.setCategory(elem.optString("category"));
        obj.setUpVote(elem.optInt("upvote_count", 0));
        obj.setDownVote(elem.optInt("downvote_count", 0));
        obj.setAddress(elem.optString("address"));
        obj.setCity(elem.optString("city"));

        JSONArray upvoters = elem.optJSONArray("upvoters");
        Set<String> tmp = new HashSet<String>();
        for (int i = 0; i < upvoters.length(); i++) {
            String user = upvoters.optString(i);
            tmp.add(user);
        }
        obj.upvoters = tmp;

        JSONArray comments = elem.optJSONArray("comments");
        List<Comment> tmpCmt = new LinkedList<Comment>();
        for (int i = 0; i < comments.length(); i++) {
            tmpCmt.add(Comment.fromJSON(comments.optJSONObject(i)));
        }
        obj.comments = tmpCmt;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        try {
            obj.setDate(df.parse(elem.optString("date")));
        } catch (ParseException e) {
            Log.e("Complaint.fromJSON",
                    "Date Parse Error: " + elem.optString("date"), e);
        }

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
