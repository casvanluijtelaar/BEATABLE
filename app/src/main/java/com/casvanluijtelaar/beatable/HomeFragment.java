package com.casvanluijtelaar.beatable;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private ArrayList<String> listNames = new ArrayList<String>();
    boolean playing = false;
    boolean recording = false;
    liveFeedback liveFeedback = new liveFeedback();
    float pitchRange = 80;
    float ampRange = 10;
    boolean detect = false;
    int soundID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sharedPref.edit();


        final ListView instrumentList = (ListView) view.findViewById(R.id.InstrumentlistView);
        final homeListAdapter adapter = new homeListAdapter(view.getContext(), R.layout.home_listitem, listNames, view);
        instrumentList.setAdapter(adapter);

        Map<String, ?> all = sharedPref.getAll();

        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().contains("com.cvanluijtelaar.eindwerk.NAME")) {
                String name = entry.getValue().toString();
                listNames.add(name);
            }
        }
        adapter.notifyDataSetChanged();

        instrumentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Bundle bundle = new Bundle();
                String passName = (String) instrumentList.getItemAtPosition(position);
                bundle.putString("name", passName);

                FragmentTransaction fr = getFragmentManager().beginTransaction();
                Fragment InstrumentFragment = new InstrumentFragment();
                InstrumentFragment.setArguments(bundle);
                fr.replace(R.id.fragment_container, InstrumentFragment);
                fr.commit();
            }
        });

        //check if there are instruments
        listChecker listChecker = new listChecker();
        listChecker.checkForInstrument(listNames, view);

        //first instrument button
        LinearLayout createLayout = (LinearLayout) view.findViewById(R.id.createLayout);
        createLayout.setClickable(true);
        createLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fr = getFragmentManager().beginTransaction();
                Fragment InstrumentFragment = new InstrumentFragment();
                fr.replace(R.id.fragment_container, InstrumentFragment);
                fr.commit();
            }
        });

        //references to fabs and layout
        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        final FloatingActionButton fab2 = (FloatingActionButton) view.findViewById(R.id.fab2);
        final LinearLayout recordLayout = (LinearLayout) view.findViewById(R.id.recordLayout);
        final TextView Tv2R = (TextView) view.findViewById(R.id.textView2record);
        recordLayout.setVisibility(View.GONE);

        //translate 2nd fab
        final Animation showLayout = AnimationUtils.loadAnimation(getContext(), R.anim.show_layout);
        final Animation hideLayout = AnimationUtils.loadAnimation(getContext(), R.anim.hide_layout);

        final playSound playSound = new playSound(getActivity());
        //main fab click listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Map<String, Float> pitches = new HashMap();
                final Map<String, Float> amps = new HashMap();
                final Map<String, String> names = new HashMap();


                Map<String, ?> all = sharedPref.getAll();
                for (Map.Entry<String, ?> entry : all.entrySet()) {
                    if (entry.getKey().contains("com.cvanluijtelaar.eindwerk.NAME")) {
                        String name = entry.getValue().toString();
                        Log.d("debug",name);
                        float pitch = sharedPref.getFloat("com.cvanluijtelaar.eindwerk.pitch." + name, 0);
                        float amp = sharedPref.getFloat("com.cvanluijtelaar.eindwerk.amp." + name, 0);
                        pitches.put(name, pitch);
                        amps.put(name, amp);
                        names.put(name, name);
                    }
                }

                Thread run = new Thread(new Runnable() {
                    public void run() {
                        while (playing && !Thread.interrupted()) {

                            float livepitch = liveFeedback.returnPitch();
                            float liveAmp = liveFeedback.returnAmp();
                            //Log.d("debug", "" + livepitch + "    " + liveAmp);

                            for (Map.Entry<String, String> entry : names.entrySet()) {
                                String name = entry.getKey();
                                float pitch = pitches.get(name);
                                float amp = amps.get(name);

                                if (range(pitch, livepitch - pitchRange, livepitch + pitchRange) && range(amp, liveAmp - ampRange, liveAmp + ampRange)) {
                                    detect = true;
                                    Log.d("debug", ""+ pitch + " - " + livepitch + " | " + amp + " - " + liveAmp);
                                    String soundName = sharedPref.getString("com.cvanluijtelaar.eindwerk.sound." + name, null);
                                    soundID = getResources().getIdentifier(soundName, "raw", getActivity().getPackageName());
                                    playSound.playLocal(soundID);
                                    try {
                                        Thread.sleep(370);
                                    } catch (InterruptedException e) {e.printStackTrace();}
                                    break;
                                }
                            }
                        }
                    }
                });


                if (!playing) {
                    playing = true;
                    fab.setImageResource(R.drawable.ic_stop_black_24dp);
                    recordLayout.setVisibility(View.VISIBLE);
                    recordLayout.startAnimation(showLayout);

                    liveFeedback.RunDetection();
                    run.start();

                } else if (playing) {
                    playing = false;
                    recording = false;

                    liveFeedback.stopDetection();
                    run.interrupt();
                    fab.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    recordLayout.setVisibility(View.GONE);
                    fab2.setImageResource(R.drawable.ic_fiber_manual_record_black_24dp);
                    Tv2R.setText("record");
                    recordLayout.startAnimation(hideLayout);
                }
            }
        });

        //second fab click listener
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording) {
                    recording = true;
                    fab2.setImageResource(R.drawable.ic_stop_black_24dp);
                    Tv2R.setText("stop");

                    writeOutput();
                } else {
                    recording = false;
                    fab2.setImageResource(R.drawable.ic_fiber_manual_record_black_24dp);
                    Tv2R.setText("record");
                }
            }
        });
        return view;
    }


    FileOutputStream out;
    long totalAudioLen = 0;
    long totalDataLen = totalAudioLen + 36;
    List<Byte> listO = new ArrayList<>();

    public void writeOutput() {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm");
        String strDate = dateFormat.format(currentTime);
        out = null;
        String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Beatable/Recordings/" + strDate + ".wav";

        try {
            out = new FileOutputStream(outputFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final long longSampleRate = 44100;
        final int channels = 2;
        final long byteRate = 16 * 44100 * channels / 8;
        final byte[] data = new byte[1024];

        final byte[] arr = new byte[100];
        for (int i = 0; i < arr.length; i++){
            arr[i]= 0;
        }
        new Thread(new Runnable() {
            public void run() {
                while (recording && !Thread.interrupted()) {
                    try {
                        if (!detect) {
                            for (byte b : arr) {
                                listO.add(b);
                            }
                            Thread.sleep(1);
                        } else {
                            InputStream input = getResources().openRawResource(soundID);
                            byte[] music = ByteStreams.toByteArray(input);
                            byte[] filteredByteArray = Arrays.copyOfRange(music, 44, music.length);

                            for (byte b : filteredByteArray) {
                                listO.add(b);
                            }

                            input.close();
                            detect = false;
                            Thread.sleep(1);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {

                    byte[] outputB = Bytes.toArray(listO);
                    totalAudioLen = outputB.length;
                    totalDataLen = totalAudioLen + 36;

                    WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                            longSampleRate, channels, byteRate);

                    out.write(outputB);
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);
        header[33] = 0;
        header[34] = 16;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    public static boolean range(float x, float min, float max) {
        return x > min && x < max;
    }

}
