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
package com.guidebee.game.audio;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.ApplicationListener;
import com.guidebee.game.files.FileHandle;
import com.guidebee.utils.Disposable;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * <p>
 * A IMusic instance represents a streamed audio file. The interface supports
 * pausing, resuming
 * and so on. When you are done with using the IMusic instance you have to
 * dispose it via the {@link #dispose()} method.
 * </p>
 * <p>
 * <p>
 * IMusic instances are created via
 * {@link com.guidebee.game.Audio#newMusic(FileHandle)}.
 * </p>
 * <p>
 * <p>
 * IMusic instances are automatically paused and resumed when an
 * {@link com.guidebee.game.Application} is paused or resumed. See
 * {@link ApplicationListener}.
 * </p>
 * <p>
 * <p>
 * <b>Note<b>: any values provided will not be clamped, it is
 * the developer's responsibility to do so
 * </p>
 *
 * @author mzechner
 */
public interface Music extends Disposable {
    /**
     * Starts the play back of the music stream. In case the stream was
     * paused this will resume the play back. In case the music
     * stream is finished playing this will restart the play back.
     */
    public void play();

    /**
     * Pauses the play back. If the music stream has not been started
     * yet or has finished playing a call to this method will be
     * ignored.
     */
    public void pause();

    /**
     * Stops a playing or paused IMusic instance. Next time play() is
     * invoked the IMusic will start from the beginning.
     */
    public void stop();

    /**
     * Check whether this music stream is playing
     * @return whether this music stream is playing
     */
    public boolean isPlaying();

    /**
     * Sets whether the music stream is looping. This can be called
     * at any time, whether the stream is playing.
     *
     * @param isLooping whether to loop the stream
     */
    public void setLooping(boolean isLooping);

    /**
     * Check whether the music stream is looping.
     * @return whether the music stream is looping.
     */
    public boolean isLooping();

    /**
     * Sets the volume of this music stream. The volume must be
     * given in the range [0,1] with 0 being silent and 1 being the
     * maximum volume.
     *
     * @param volume
     */
    public void setVolume(float volume);

    /**
     * Get the volume of this music stream.
     * @return the volume of this music stream.
     */
    public float getVolume();

    /**
     * Sets the panning and volume of this music stream.
     *
     * @param pan    panning in the range -1 (full left) to 1
     *               (full right). 0 is center position.
     * @param volume the volume in the range [0,1].
     */
    public void setPan(float pan, float volume);

    /**
     * Returns the playback position in seconds.
     */
    public float getPosition();

    /**
     * Needs to be called when the IMusic is no longer needed.
     */
    public void dispose();

    /**
     * Register a callback to be invoked when the end of a
     * music stream has been reached during playback.
     *
     * @param listener the callback that will be run.
     */
    public void setOnCompletionListener(OnCompletionListener listener);

    /**
     * Interface definition for a callback to be invoked when
     * playback of a music stream has completed.
     */
    public interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         *
         * @param music the IMusic that reached the end of the file
         */
        public abstract void onCompletion(Music music);
    }
}
