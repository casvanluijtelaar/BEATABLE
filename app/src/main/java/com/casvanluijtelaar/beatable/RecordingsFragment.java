package com.casvanluijtelaar.beatable;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class RecordingsFragment extends Fragment {

    private ArrayList<String> recordings = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordings, null);


        String recordingsfolder = "Recordings";
        File f = new File(Environment.getExternalStorageDirectory() + "/" + "Beatable" + "/", recordingsfolder);
        if (!f.exists()) {
            f.mkdirs();
        }

        String path = Environment.getExternalStorageDirectory() + "/" + "Beatable" + "/" + recordingsfolder;
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                recordings.add(name);
            }
        }

        ListView recordingsList = (ListView) view.findViewById(R.id.recordingsListView);
        recordingsList.setAdapter(new recordingListAdapter(getActivity(), R.layout.recordings_listitem, recordings));


        return view;
    }


}