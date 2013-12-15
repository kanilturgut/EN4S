package com.tobbetu.en4s.listItems;

public class EntryItem implements ListItem {

    private String title;

    public EntryItem(String title) {
        this.title = title;
    }

    @Override
    public boolean isSection() {
        return false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
