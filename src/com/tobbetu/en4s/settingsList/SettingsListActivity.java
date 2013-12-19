package com.tobbetu.en4s.settingsList;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.tobbetu.en4s.R;
import com.tobbetu.en4s.listItems.EntryItem;
import com.tobbetu.en4s.listItems.ListItem;
import com.tobbetu.en4s.listItems.SectionItem;

public class SettingsListActivity extends ListActivity {

    ArrayList<ListItem> items = new ArrayList<ListItem>();
    private ListView liste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list);
        getActionBar().hide();

        liste = (ListView) findViewById(android.R.id.list);

        items.add(new SectionItem("Profil Ayarları"));
        items.add(new EntryItem("Profiliniz"));
        items.add(new EntryItem("Şifre Değiştir"));

        items.add(new SectionItem("Bildirim Ayarları"));
        items.add(new EntryItem("Bildirim Alma Ayarları"));
        items.add(new EntryItem("Bildirim Alanları"));
        items.add(new EntryItem("Bildirim Bölgeleri"));

        items.add(new SectionItem("Oturum Ayarları"));
        items.add(new EntryItem("Oturumu Kapat"));

        EntryAdapter adapter = new EntryAdapter(this, items);
        liste.setAdapter(adapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        if (!items.get(position).isSection()) {

            com.tobbetu.en4s.listItems.EntryItem item = (EntryItem) items
                    .get(position);

            Toast.makeText(getApplicationContext(), item.getTitle(),
                    Toast.LENGTH_SHORT).show();
        }

        super.onListItemClick(l, v, position, id);
    }

}
