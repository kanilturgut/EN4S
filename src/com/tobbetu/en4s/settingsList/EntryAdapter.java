package com.tobbetu.en4s.settingsList;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.listItems.EntryItem;
import com.tobbetu.en4s.listItems.ListItem;
import com.tobbetu.en4s.listItems.SectionItem;

public class EntryAdapter extends ArrayAdapter<ListItem> {

    private ArrayList<ListItem> list;
    private LayoutInflater inflater;

    public EntryAdapter(Context context, ArrayList<ListItem> items) {
        super(context, 0, items);

        this.list = items;
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        final ListItem listItem = list.get(position);

        if (listItem != null) {
            if (listItem.isSection()) {
                SectionItem si = (SectionItem) listItem;
                v = inflater.inflate(R.layout.list_section_item, null);

                // section item
                v.setOnClickListener(null);
                v.setOnLongClickListener(null);
                v.setLongClickable(false);

                final TextView sectionView = (TextView) v
                        .findViewById(R.id.list_item_section_text);
                sectionView.setText(si.getTitle());
                sectionView.setTextSize(18);
            } else {
                EntryItem ei = (EntryItem) listItem;
                v = inflater.inflate(R.layout.list_entry_item, null);

                final TextView title = (TextView) v
                        .findViewById(R.id.tvListEntryItem);

                title.setTextSize(15);

                if (title != null)
                    title.setText(ei.getTitle());
            }
        }

        return v;
    }
}
