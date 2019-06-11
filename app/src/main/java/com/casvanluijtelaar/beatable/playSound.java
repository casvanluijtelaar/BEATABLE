package com.casvanluijtelaar.beatable;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class playSound {
    Context context;
    MediaPlayer mp;
    MediaRecorder mr;

    playSound(Context context) {
        this.context = context;
    }


    public MediaPlayer play(String fileDir) {
        mp = null;
        File f = new File(fileDir);
        mp = MediaPlayer.create(context, Uri.parse(String.valueOf(Uri.fromFile(f))));


        if (mp != null) {

            try {
                mp.prepareAsync();
            } catch (IllegalStateException e) {
            }


            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });
        }
        return mp;
    }

    public MediaPlayer playLocal(int resID) {

        mp = MediaPlayer.create(context, resID);

        if (mp != null) {

            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    mp.release();
                    try {


                        mp.reset();
                        mp = null;

                    }catch (IllegalStateException e){}
                }
            });
        }
        return mp;
    }




    public void stop() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    public void recordMicrophoneStart() {

        if (mr == null) {
            Date currentTime = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm");
            String strDate = dateFormat.format(currentTime);

            mr = new MediaRecorder();
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);
            mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Beatable/Recordings/" + strDate + ".3gp";
            mr.setOutputFile(outputFile);
            mr.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            mr.setAudioEncodingBitRate(16);
            mr.setAudioSamplingRate(44100);

            try {
                mr.prepare();

            } catch (IllegalStateException ise) {
                // make something ...
            } catch (IOException ioe) {
                // make something
            }
            mr.start();
        }
    }

    public void recordMicrophoneStop() {
        if (mr != null) {
            mr.stop();
            mr.release();
            mr = null;
        }
    }


}
