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
import android.media.MediaPlayer;
import com.guidebee.game.GameEngine;

import java.io.IOException;

//[------------------------------ MAIN CLASS ----------------------------------]
public class Music implements com.guidebee.game.audio.Music, MediaPlayer.OnCompletionListener {
    private final Audio audio;
    private MediaPlayer player;
    private boolean isPrepared = true;
    protected boolean wasPlaying = false;
    private float volume = 1f;
    protected OnCompletionListener onCompletionListener;

    Music(Audio audio, MediaPlayer player) {
        this.audio = audio;
        this.player = player;
        this.onCompletionListener = null;
        this.player.setOnCompletionListener(this);
    }

    @Override
    public void dispose() {
        if (player == null) return;
        try {
            if (player.isPlaying()) player.stop();
            player.release();
        } catch (Throwable t) {
            GameEngine.app.log("Music",
                    "error while disposing Music instance, non-fatal");
        } finally {
            player = null;
            onCompletionListener = null;
            synchronized (audio.musics) {
                audio.musics.remove(this);
            }
        }
    }

    @Override
    public boolean isLooping() {
        if (player == null) return false;
        return player.isLooping();
    }

    @Override
    public boolean isPlaying() {
        if (player == null) return false;
        return player.isPlaying();
    }

    @Override
    public void pause() {
        if (player == null) return;
        if (player.isPlaying()) {
            player.pause();
        }
        wasPlaying = false;
    }

    @Override
    public void play() {
        if (player == null) return;
        if (player.isPlaying()) return;

        try {
            if (!isPrepared) {
                player.prepare();
                isPrepared = true;
            }
            player.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setLooping(boolean isLooping) {
        if (player == null) return;
        player.setLooping(isLooping);
    }

    @Override
    public void setVolume(float volume) {
        if (player == null) return;
        player.setVolume(volume, volume);
        this.volume = volume;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public void setPan(float pan, float volume) {
        if (player == null) return;
        float leftVolume = volume;
        float rightVolume = volume;

        if (pan < 0) {
            rightVolume *= (1 - Math.abs(pan));
        } else if (pan > 0) {
            leftVolume *= (1 - Math.abs(pan));
        }

        player.setVolume(leftVolume, rightVolume);
        this.volume = volume;
    }

    @Override
    public void stop() {
        if (player == null) return;
        if (isPrepared) {
            player.seekTo(0);
        }
        player.stop();
        isPrepared = false;
    }

    public void setPosition(float position) {
        if (player == null) return;
        try {
            if (!isPrepared) {
                player.prepare();
                isPrepared = true;
            }
            player.seekTo((int) (position * 1000));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public float getPosition() {
        if (player == null) return 0.0f;
        return player.getCurrentPosition() / 1000f;
    }

    public float getDuration() {
        if (player == null) return 0.0f;
        return player.getDuration() / 1000f;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        onCompletionListener = listener;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (onCompletionListener != null) {
            GameEngine.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    onCompletionListener.onCompletion(Music.this);
                }
            });
        }
    }

    ;
}