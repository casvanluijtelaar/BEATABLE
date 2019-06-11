package com.casvanluijtelaar.beatable;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

public class InstrumentFragment extends Fragment {
    Dialog setupPopup;
    Context context;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_instrument, null);
        context = view.getContext();


        Bundle bundle = getArguments();
        setupPopup = new Dialog(getActivity());


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = sharedPref.edit();

        Spinner selectSound = (Spinner) view.findViewById(R.id.spinner1);
        final ArrayList<String> listItems = new ArrayList<String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectSound.setAdapter(adapter);

        Field[] fields = R.raw.class.getFields();
        for (int count = 0; count < fields.length; count++) {
            listItems.add(fields[count].getName());
        }
        adapter.notifyDataSetChanged();


        if (bundle == null) {
            showPopup();
        } else {
            final String passedName = bundle.getString("name");
            final String passedSound = sharedPref.getString("com.cvanluijtelaar.eindwerk.sound." + passedName, null);
            final Float passedPitch = sharedPref.getFloat("com.cvanluijtelaar.eindwerk.pitch." + passedName, 0);

            final EditText nameEditText = (EditText) view.findViewById(R.id.nameEditText);
            final Spinner spinner = (Spinner) view.findViewById(R.id.spinner1);
            final ProgressBar pitchProgressBar = (ProgressBar) view.findViewById(R.id.PitchProgressBar);
            final ProgressBar ampProgressBar = (ProgressBar) view.findViewById(R.id.ampProgressBar);
            Float storedPitch = sharedPref.getFloat("com.cvanluijtelaar.eindwerk.pitch." + passedName, 0);
            Float storedAmp = sharedPref.getFloat("com.cvanluijtelaar.eindwerk.amp." + passedName, 0);
            pitchProgressBar.setProgress((int) map(storedPitch.longValue(), 20, 6000, 0, 100));
            ampProgressBar.setProgress((int) map(storedAmp.longValue(), 20, 200, 0, 100));


            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    sound = (String) spinner.getItemAtPosition(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            nameEditText.setText(passedName);
            selectSound.setSelection(getIndex(selectSound, passedSound));


            Button saveBtn = (Button) view.findViewById(R.id.setupBtn);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = nameEditText.getText().toString();
                    editor.remove("com.cvanluijtelaar.eindwerk.NAME." + passedName);
                    editor.putString("com.cvanluijtelaar.eindwerk.NAME." + name, name);

                    editor.remove("com.cvanluijtelaar.eindwerk.sound." + passedName);
                    editor.putString("com.cvanluijtelaar.eindwerk.sound." + name, sound);

                    editor.remove("com.cvanluijtelaar.eindwerk.pitch." + passedName);
                    editor.putFloat("com.cvanluijtelaar.eindwerk.pitch." + name, pitch);

                    editor.remove("com.cvanluijtelaar.eindwerk.amp." + passedName);
                    editor.putFloat("com.cvanluijtelaar.eindwerk.amp." + name, amp);

                    FragmentTransaction fr = getFragmentManager().beginTransaction();
                    Fragment HomeFragment = new HomeFragment();
                    fr.replace(R.id.fragment_container, HomeFragment);
                    fr.commit();
                }
            });

            Button InstrumentEditBtn = (Button) view.findViewById(R.id.instrumentEditBtn);
            InstrumentEditBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup3();
                }
            });


        }

        return view;
    }

    String name;

    public void showPopup() {
        setupPopup.setContentView(R.layout.setup_popup);
        setupPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = sharedPref.edit();

        final EditText instrumentName = (EditText) setupPopup.findViewById(R.id.instrumentName);

        Button next1Btn = (Button) setupPopup.findViewById(R.id.next1Btn);
        next1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = instrumentName.getText().toString();
                if (name == null || name.equals("")) {
                    Toast.makeText(context, "please enter a name", Toast.LENGTH_LONG).show();
                } else {
                    editor.putString("com.cvanluijtelaar.eindwerk.NAME." + name, name);
                    editor.commit();
                    showPopup2();
                }
            }
        });
        setupPopup.show();
    }


    String sound;

    public void showPopup2() {
        setupPopup.setContentView(R.layout.setup_popup2);
        setupPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = sharedPref.edit();

        final Spinner setupSelectSound = (Spinner) setupPopup.findViewById(R.id.spinner2);
        final ArrayList<String> listItems = new ArrayList<String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, listItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setupSelectSound.setAdapter(adapter);

        Field[] fields = R.raw.class.getFields();
        for (int count = 0; count < fields.length; count++) {
            listItems.add(fields[count].getName());
        }
        adapter.notifyDataSetChanged();

        setupSelectSound.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sound = (String) setupSelectSound.getItemAtPosition(position);
                int soundID = getResources().getIdentifier(sound, "raw", getActivity().getPackageName());
                playSound playSound = new playSound(context);
                playSound.playLocal(soundID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button next2Btn = (Button) setupPopup.findViewById(R.id.next2Btn);
        next2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("com.cvanluijtelaar.eindwerk.sound." + name, sound);
                editor.commit();
                showPopup3();
            }
        });

        Button prev2Btn = (Button) setupPopup.findViewById(R.id.prev2Btn);
        prev2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
        setupPopup.show();
    }


    float pitch;
    float amp;

    public void showPopup3() {
        setupPopup.setContentView(R.layout.setup_popup3);
        setupPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = sharedPref.edit();

        final Animation showProgress = AnimationUtils.loadAnimation(getContext(), R.anim.show_progress);
        final Animation hideProgress = AnimationUtils.loadAnimation(getContext(), R.anim.hide_progress);

        final ProgressBar progressBar = (ProgressBar) setupPopup.findViewById(R.id.detectProgressBar);
        progressBar.setVisibility(View.GONE);

        Button detectBtn = (Button) setupPopup.findViewById(R.id.detectBtn);
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                progressBar.startAnimation(showProgress);

                pitch = 0;
                amp = 0;
                final AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
                PitchDetectionHandler pdh = new PitchDetectionHandler() {

                    @Override
                    public void handlePitch(PitchDetectionResult result, final AudioEvent e) {

                        final float pitchInHz = result.getPitch();
                        final float[] amplitudes = new float[e.getBufferSize()];

                        new Thread(new Runnable() {
                            public void run() {
                                if (pitchInHz > pitch) {
                                    pitch = pitchInHz;
                                }

                                float[] audioFloatBuffer = e.getFloatBuffer();
                                float[] transformBuffer = new float[e.getBufferSize() * 2];
                                FFT fft = new FFT(e.getBufferSize());
                                System.arraycopy(audioFloatBuffer, 0, transformBuffer, 0, audioFloatBuffer.length);
                                fft.forwardTransform(transformBuffer);
                                fft.modulus(transformBuffer, amplitudes);


                                for (int index = 0; index < amplitudes.length; index++) {
                                    if (amplitudes[index] > amp) {
                                        amp = amplitudes[index];
                                    }
                                }
                            }
                        }).start();
                    }
                };

                AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
                dispatcher.addAudioProcessor(p);
                final Thread startPitch = new Thread(dispatcher, "Audio Dispatcher");

                startPitch.start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dispatcher.stop();
                        startPitch.interrupt();
                        progressBar.startAnimation(hideProgress);

                        hideProgress.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation arg0) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation arg0) {
                            }

                            @Override
                            public void onAnimationEnd(Animation arg0) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }, 2000);
            }
        });

        Button next3Btn = (Button) setupPopup.findViewById(R.id.next3Btn);
        next3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("com.cvanluijtelaar.eindwerk.pitch." + name, pitch);
                editor.putFloat("com.cvanluijtelaar.eindwerk.amp." + name, amp);

                editor.commit();
                setupPopup.dismiss();

                Bundle bundle = new Bundle();
                bundle.putString("name", sharedPref.getString("com.cvanluijtelaar.eindwerk.NAME." + name, null));
                FragmentTransaction fr = getFragmentManager().beginTransaction();
                Fragment InstrumentFragment = new HomeFragment();
                InstrumentFragment.setArguments(bundle);
                fr.replace(R.id.fragment_container, InstrumentFragment);
                fr.commit();
            }
        });

        Button prev3Btn = (Button) setupPopup.findViewById(R.id.prev3Btn);
        prev3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup2();
            }
        });
        setupPopup.show();
    }


    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }

    long map(long x, long in_min, long in_max, long out_min, long out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}


