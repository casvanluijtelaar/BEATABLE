package com.casvanluijtelaar.beatable;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

public class liveFeedback {


    float pitch;
    float amp;


    Thread startDetect = new Thread();
    AudioDispatcher dispatcher;

    public float returnPitch() {
        return this.pitch;
    }

    public float returnAmp() {
        return this.amp;
    }

    public void RunDetection() {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, final AudioEvent e) {

                final float pitchInHz = result.getPitch();
                final float[] amplitudes = new float[e.getBufferSize()];

                new Thread(new Runnable() {
                    public void run() {

                        pitch = pitchInHz;

                        float[] audioFloatBuffer = e.getFloatBuffer();
                        float[] transformBuffer = new float[e.getBufferSize() * 2];
                        FFT fft = new FFT(e.getBufferSize());
                        System.arraycopy(audioFloatBuffer, 0, transformBuffer, 0, audioFloatBuffer.length);
                        fft.forwardTransform(transformBuffer);
                        fft.modulus(transformBuffer, amplitudes);

                        amp = 0;
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
        startDetect = new Thread(dispatcher, "Audio Dispatcher");
        startDetect.start();
    }

    public void stopDetection() {
        if (dispatcher != null && startDetect != null) {
            dispatcher.stop();
            startDetect.interrupt();
            dispatcher = null;
        }
    }
}
