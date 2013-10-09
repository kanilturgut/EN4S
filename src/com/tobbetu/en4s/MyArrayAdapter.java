package com.tobbetu.en4s;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<String> {

	private Context context;
	private String[] myArray;

	public MyArrayAdapter(Context c, String[] array) {
		super(c, R.layout.drawer_list_item, array);

		this.context = c;
		this.myArray = array;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater
				.inflate(R.layout.drawer_list_item, parent, false);

		
		TextView tv = (TextView) rowView.findViewById(R.id.tvNavigator);
		tv.setText(myArray[position]);
		
		return rowView;
	}

}
