package com.casvanluijtelaar.beatable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;


public class recordingListAdapter extends ArrayAdapter<String> {
    private int layout;
    private List<String> rEvents;
    Context context;
    Dialog namePopup;
    View view;

    public recordingListAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        layout = resource;
        rEvents = objects;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder mainViewHolder = null;
        view = convertView;
        final playSound playSound = new playSound(getContext());

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.recordingName = (TextView) convertView.findViewById(R.id.recordingName);
            String name = stripExtension(getItem(position));
            viewHolder.recordingName.setText(name);

            viewHolder.playBtn = (Button) convertView.findViewById(R.id.playBtn);
            viewHolder.playBtn.setOnClickListener(new View.OnClickListener() {
                Boolean playing = false;

                @Override
                public void onClick(View v) {
                    if (!playing) {
                        viewHolder.playBtn.setBackgroundResource(R.drawable.ic_stop_circle_icon);
                        playing = true;
                        String dir = Environment.getExternalStorageDirectory() + "/Beatable/Recordings/" + getItem(position);

                        try {
                            playSound.play(dir).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                public void onCompletion(MediaPlayer mp) {
                                    viewHolder.playBtn.setBackgroundResource(R.drawable.ic_playbutton);
                                    playing = false;
                                }
                            });
                        } catch (NullPointerException e) {

                            viewHolder.playBtn.setBackgroundResource(R.drawable.ic_playbutton);
                            playing = false;
                        }
                    } else {
                        viewHolder.playBtn.setBackgroundResource(R.drawable.ic_playbutton);
                        playing = false;
                        playSound.stop();
                    }

                }
            });

            viewHolder.optionsBtn = (FrameLayout) convertView.findViewById(R.id.optionsBtn);
            viewHolder.optionsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu settingMenu = new PopupMenu(getContext(), viewHolder.optionsBtn);

                    settingMenu.getMenuInflater().inflate(R.menu.recordings_settings, settingMenu.getMenu());
                    settingMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.recordingDeleteBtn:
                                    String deletePath = Environment.getExternalStorageDirectory() + "/Beatable/Recordings/" + getItem(position);
                                    rEvents.remove(getItem(position));
                                    notifyDataSetChanged();
                                    File dir = new File(deletePath);
                                    dir.delete();
                                    Toast.makeText(getContext(), "recoding removed", Toast.LENGTH_LONG).show();
                                    settingMenu.dismiss();
                                    break;

                                case R.id.recordingShareBtn:
                                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                    StrictMode.setVmPolicy(builder.build());
                                    Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                                    String myFilePath = Environment.getExternalStorageDirectory() + "/Beatable/Recordings/" + getItem(position);
                                    File fileWithinMyDir = new File(myFilePath);
                                    if (fileWithinMyDir.exists()) {
                                        intentShareFile.setType("audio/wav");
                                        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + myFilePath));
                                        parent.getContext().startActivity(Intent.createChooser(intentShareFile, "Share File"));
                                    }
                                    settingMenu.dismiss();
                                    break;

                                case R.id.changeNameBtn:
                                    namePopup = new Dialog(context);
                                    namePopup.setContentView(R.layout.rename_popup);
                                    namePopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                    final EditText renameEditText = (EditText) namePopup.findViewById(R.id.renameEditText);
                                    Button renameBtn = (Button) namePopup.findViewById(R.id.setNameBtn);
                                    renameBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            final String name = renameEditText.getText().toString();
                                            if (name.equals("")) {
                                                Toast.makeText(context, "please enter a name", Toast.LENGTH_LONG).show();
                                            } else {

                                                File old = new File(Environment.getExternalStorageDirectory() + "/Beatable/Recordings/" + getItem(position));
                                                File newFile = new File(Environment.getExternalStorageDirectory() + "/Beatable/Recordings/" + name + ".wav");
                                                old.renameTo(newFile);

                                                FragmentTransaction fr = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
                                                Fragment InstrumentFragment = new RecordingsFragment();

                                                fr.replace(R.id.fragment_container, InstrumentFragment);
                                                fr.commit();

                                                namePopup.dismiss();
                                            }
                                        }
                                    });
                                    namePopup.show();
                                    break;
                            }
                            return true;
                        }
                    });
                    settingMenu.show();
                }
            });

            convertView.setTag(viewHolder);
        } else {
            mainViewHolder = (ViewHolder) convertView.getTag();
            mainViewHolder.recordingName.setText(stripExtension(getItem(position)));
        }
        return convertView;
    }

    public static String stripExtension(final String s) {
        return s != null && s.lastIndexOf(".") > 0 ? s.substring(0, s.lastIndexOf(".")) : s;
    }

    public static String getExtension(final String s) {
        if (s != null && s.lastIndexOf(".") > 0) {
            return s.substring(s.lastIndexOf("."));
        } else {
            return null;
        }
    }
}

class ViewHolder {
    TextView recordingName;
    Button playBtn;
    FrameLayout optionsBtn;
}

