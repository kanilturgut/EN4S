package com.tobbetu.en4s.announcement;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.ImageView;

import com.tobbetu.en4s.backend.Requests;
import com.tobbetu.en4s.cache.Cache;

public class Announcement implements Serializable {

    private static final long serialVersionUID = 2052730493990030947L;

    private String id;
    private Set<String> areas;
    private String city;
    private String description;
    private String district;
    private String endDate;
    private String icon;
    private String insertDate;
    private double latitude;
    private double longitude;
    private String sender;
    private String slugCity;
    private String slugDistrict;
    private String slugURL;
    private String source;
    private String startDate;
    private Set<String> tags;
    private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getAreas() {
        return areas;
    }

    public void setAreas(Set<String> areas) {
        this.areas = areas;
    }

    public int getAreasLength() {
        return areas.size();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = "http://enforceapp.com" + icon;
    }

    public String getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(String insertDate) {
        this.insertDate = insertDate;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSlugCity() {
        return slugCity;
    }

    public void setSlugCity(String slugCity) {
        this.slugCity = slugCity;
    }

    public String getSlugDistrict() {
        return slugDistrict;
    }

    public void setSlugDistrict(String slugDistrict) {
        this.slugDistrict = slugDistrict;
    }

    public String getSlugURL() {
        return slugURL;
    }

    public void setSlugURL(String slugURL) {
        this.slugURL = "http://enforceapp.com" + slugURL;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private static Announcement fromJSON(JSONObject elem) throws JSONException {
        Announcement obj = new Announcement();

        // set id of announcement
        obj.setId(elem.getString("_id"));

        // set effected areas of announcement
        JSONArray areas = elem.getJSONArray("areas");
        Set<String> tmp = new HashSet<String>();
        for (int i = 0; i < areas.length(); i++) {
            String area = areas.getString(i);
            tmp.add(area);
        }
        obj.areas = tmp;

        // set city of announcement
        obj.setCity(elem.getString("city"));

        // set description of announcement
        obj.setDescription(elem.getString("description"));

        // set district of announcement
        obj.setDistrict(elem.getString("district"));

        // set start, inserted and end date information of announcement
        JSONObject date = elem.getJSONObject("end_date");
        obj.setEndDate(date.getString("$date"));

        date = elem.getJSONObject("insert_date");
        obj.setInsertDate(date.getString("$date"));

        date = elem.getJSONObject("start_date");
        obj.setStartDate(date.getString("$date"));

        // set icon announcement
        obj.setIcon(elem.getString("icon"));

        // set latitude and longitude of announcement
        JSONArray geo = elem.getJSONArray("location");
        obj.setLatitude(geo.getDouble(0));
        obj.setLongitude(geo.getDouble(1));

        // set sender info of announcement
        obj.setSender(elem.getString("sender"));

        // set slug informations of announcement
        obj.setSlugCity(elem.getString("slug_city"));
        obj.setSlugDistrict(elem.getString("slug_district"));
        obj.setSlugURL(elem.getString("slug_url"));

        // set source of announcement
        obj.setSource(elem.getString("source"));

        // set tags of announcement
        JSONArray tags = elem.getJSONArray("tags");
        tmp = new HashSet<String>();
        for (int i = 0; i < tags.length(); i++) {
            String tag = tags.getString(i);
            tmp.add(tag);
        }
        obj.tags = tmp;

        // set title of announcement
        obj.setTitle(elem.getString("title"));

        return obj;
    }

    public static List<Announcement> getList(String url) throws IOException,
            JSONException {

        HttpResponse get = Requests.get(url);

        if (!Requests.checkStatusCode(get, HttpStatus.SC_OK))
            Log.e("Announcement.getList", "[ERROR] Status Code: "
                    + get.getStatusLine().getStatusCode());
        String response = Requests.readResponse(get);
        return Announcement.parseList(response);
    }

    private static List<Announcement> parseList(String response)
            throws JSONException {
        List<Announcement> list = new LinkedList<Announcement>();

        JSONArray results = new JSONArray(response);
        for (int i = 0; i < results.length(); i++) {
            JSONObject item = results.getJSONObject(i);
            list.add(Announcement.fromJSON(item));
        }

        return list;
    }

    public void getImage(String url, String size, ImageView iv) {
        if (!url.equals(null)) {
            Cache.getInstance().getImage(url, iv);
        }
    }
}
