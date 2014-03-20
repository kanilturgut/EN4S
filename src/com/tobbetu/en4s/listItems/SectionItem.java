package com.tobbetu.en4s.listItems;

public class SectionItem implements ListItem {

    private final String title;

    public SectionItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
