package com.casvanluijtelaar.beatable;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class homeListAdapter extends ArrayAdapter<String> {
    private int layout;
    private Context Context;
    private List<String> events;
    Dialog settings;
    View view;


    public homeListAdapter(Context context, int resource, List<String> objects, View view) {
        super(context, resource, objects);
        layout = resource;
        Context = context;
        events = objects;
        this.view = view;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        homeViewHolder mainViewHolder = null;
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Context);
        final SharedPreferences.Editor editor = sharedPref.edit();


        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            final homeViewHolder homeViewHolder = new homeViewHolder();

            homeViewHolder.homeInstrumentName = (TextView) convertView.findViewById(R.id.homeInstrumentName);
            homeViewHolder.homeInstrumentSound = (TextView) convertView.findViewById(R.id.homeInstrumentSound);

            final String name = getItem(position);
            homeViewHolder.homeInstrumentName.setText(name);
            final String sound = sharedPref.getString("com.cvanluijtelaar.eindwerk.sound." + name, null);
            homeViewHolder.homeInstrumentSound.setText(sound);

            homeViewHolder.homeOptionsBtn = (FrameLayout) convertView.findViewById(R.id.homeOptionsBtn);
            homeViewHolder.homeOptionsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final PopupMenu settingMenu = new PopupMenu(parent.getContext(), homeViewHolder.homeOptionsBtn);
                    settingMenu.getMenuInflater().inflate(R.menu.instrument_settings, settingMenu.getMenu());

                    settingMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.deleteBtn:
                                    editor.remove("com.cvanluijtelaar.eindwerk.NAME." + name);
                                    editor.remove("com.cvanluijtelaar.eindwerk.sound." + name);
                                    editor.remove("com.cvanluijtelaar.eindwerk.pitch." + name);
                                    editor.remove("com.cvanluijtelaar.eindwerk.amp." + name);
                                    editor.commit();
                                    events.remove(getItem(position));
                                    notifyDataSetChanged();
                                    Toast.makeText(getContext(), name + " removed", Toast.LENGTH_LONG).show();
                                    listChecker listChecker = new listChecker();
                                    listChecker.checkForInstrument(events, view);
                                    settingMenu.dismiss();
                                    break;
                            }
                            return true;
                        }
                    });
                    settingMenu.show();
                }
            });

            settings = new Dialog(parent.getContext());
            convertView.setTag(homeViewHolder);
        } else {
            mainViewHolder = (homeViewHolder) convertView.getTag();
            mainViewHolder.homeInstrumentName.setText(getItem(position));
        }
        return convertView;
    }
}

class homeViewHolder {
    TextView homeInstrumentName;
    TextView homeInstrumentSound;
    FrameLayout homeOptionsBtn;
}

