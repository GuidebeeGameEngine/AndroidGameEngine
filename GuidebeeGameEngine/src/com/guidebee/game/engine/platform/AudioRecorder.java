/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
//--------------------------------- PACKAGE ------------------------------------
package com.guidebee.game.engine.platform;

//--------------------------------- IMPORTS ------------------------------------
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.guidebee.game.GameEngineRuntimeException;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * {@link com.guidebee.game.audio.AudioRecorder} implementation for
 * the android system based on AudioRecord
 *
 * @author badlogicgames@gmail.com
 */
public class AudioRecorder implements com.guidebee.game.audio.AudioRecorder {
    /**
     * the audio track we read samples from *
     */
    private AudioRecord recorder;

    public AudioRecorder(int samplingRate, boolean isMono) {
        int channelConfig = isMono ? AudioFormat.CHANNEL_IN_MONO
                : AudioFormat.CHANNEL_IN_STEREO;
        int minBufferSize = AudioRecord.getMinBufferSize(samplingRate,
                channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                samplingRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize);
        if (recorder.getState() != AudioRecord.STATE_INITIALIZED)
            throw new GameEngineRuntimeException("Unable to initialize AudioRecorder.\n" +
                    "Do you have the RECORD_AUDIO permission?");
        recorder.startRecording();
    }

    @Override
    public void dispose() {
        recorder.stop();
        recorder.release();
    }

    @Override
    public void read(short[] samples, int offset, int numSamples) {
        int read = 0;
        while (read != numSamples) {
            read += recorder.read(samples, offset + read, numSamples - read);
        }
    }

}
