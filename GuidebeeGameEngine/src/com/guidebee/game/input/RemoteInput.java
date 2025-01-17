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
package com.guidebee.game.input;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.GameEngine;
import com.guidebee.game.GameEngineRuntimeException;
import com.guidebee.game.Input;
import com.guidebee.game.InputProcessor;
import com.guidebee.game.graphics.Pixmap;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * <p>
 * An {@link com.guidebee.game.Input} implementation that receives touch, key, accelerometer and
 * compass events from a remote Android device. Just
 * instantiate it and specify the port it should listen on for incoming
 * connections (default 8190). Then store the new RemoteInput
 * instance in GameEngine.input. That's it.
 * </p>
 * <p>
 * <p>
 * On your Android device you can use the remote application available
 * on the Google Code page as an APK or in SVN
 * (extensions/remote). Open it, specify the IP address and the port of
 * the PC your libgameengine app is running on and then tap
 * away.
 * </p>
 * <p>
 * <p>
 * The touch coordinates will be translated to the desktop window's coordinate
 * system, no matter the orientation of the device
 * </p>
 *
 * @author mzechner
 */
public class RemoteInput implements Runnable, Input {

    /**
     * remote input listener.
     */
    public interface RemoteInputListener {
        void onConnected();

        void onDisconnected();
    }

    class KeyEvent {
        static final int KEY_DOWN = 0;
        static final int KEY_UP = 1;
        static final int KEY_TYPED = 2;

        long timeStamp;
        int type;
        int keyCode;
        char keyChar;
    }

    class TouchEvent {
        static final int TOUCH_DOWN = 0;
        static final int TOUCH_UP = 1;
        static final int TOUCH_DRAGGED = 2;

        long timeStamp;
        int type;
        int x;
        int y;
        int pointer;
    }

    class EventTrigger implements Runnable {
        TouchEvent touchEvent;
        KeyEvent keyEvent;

        public EventTrigger(TouchEvent touchEvent, KeyEvent keyEvent) {
            this.touchEvent = touchEvent;
            this.keyEvent = keyEvent;
        }

        @Override
        public void run() {
            justTouched = false;
            if (keyJustPressed) {
                keyJustPressed = false;
                for (int i = 0; i < justPressedKeys.length; i++) {
                    justPressedKeys[i] = false;
                }
            }

            if (processor != null) {
                if (touchEvent != null) {
                    touchX[touchEvent.pointer] = touchEvent.x;
                    touchY[touchEvent.pointer] = touchEvent.y;
                    switch (touchEvent.type) {
                        case TouchEvent.TOUCH_DOWN:
                            processor.touchDown(touchEvent.x, touchEvent.y,
                                    touchEvent.pointer, Input.Buttons.LEFT);
                            isTouched[touchEvent.pointer] = true;
                            justTouched = true;
                            break;
                        case TouchEvent.TOUCH_UP:
                            processor.touchUp(touchEvent.x, touchEvent.y,
                                    touchEvent.pointer, Input.Buttons.LEFT);
                            isTouched[touchEvent.pointer] = false;
                            break;
                        case TouchEvent.TOUCH_DRAGGED:
                            processor.touchDragged(touchEvent.x,
                                    touchEvent.y, touchEvent.pointer);
                            break;
                    }
                }
                if (keyEvent != null) {
                    switch (keyEvent.type) {
                        case KeyEvent.KEY_DOWN:
                            processor.keyDown(keyEvent.keyCode);
                            if (!keys[keyEvent.keyCode]) {
                                keyCount++;
                                keys[keyEvent.keyCode] = true;
                            }
                            keyJustPressed = true;
                            justPressedKeys[keyEvent.keyCode] = true;
                            break;
                        case KeyEvent.KEY_UP:
                            processor.keyUp(keyEvent.keyCode);
                            if (keys[keyEvent.keyCode]) {
                                keyCount--;
                                keys[keyEvent.keyCode] = false;
                            }
                            break;
                        case KeyEvent.KEY_TYPED:
                            processor.keyTyped(keyEvent.keyChar);
                            break;
                    }
                }
            } else {
                if (touchEvent != null) {
                    touchX[touchEvent.pointer] = touchEvent.x;
                    touchY[touchEvent.pointer] = touchEvent.y;
                    if (touchEvent.type == TouchEvent.TOUCH_DOWN) {
                        isTouched[touchEvent.pointer] = true;
                        justTouched = true;
                    }
                    if (touchEvent.type == TouchEvent.TOUCH_UP) {
                        isTouched[touchEvent.pointer] = false;
                    }
                }
                if (keyEvent != null) {
                    if (keyEvent.type == KeyEvent.KEY_DOWN) {
                        if (!keys[keyEvent.keyCode]) {
                            keyCount++;
                            keys[keyEvent.keyCode] = true;
                        }
                        keyJustPressed = true;
                        justPressedKeys[keyEvent.keyCode] = true;
                    }
                    if (keyEvent.type == KeyEvent.KEY_UP) {
                        if (keys[keyEvent.keyCode]) {
                            keyCount--;
                            keys[keyEvent.keyCode] = false;
                        }
                    }
                }
            }
        }
    }

    public static int DEFAULT_PORT = 8190;
    private ServerSocket serverSocket;
    private float[] accel = new float[3];
    private float[] compass = new float[3];
    private boolean multiTouch = false;
    private float remoteWidth = 0;
    private float remoteHeight = 0;
    private boolean connected = false;
    private RemoteInputListener listener;
    int keyCount = 0;
    boolean[] keys = new boolean[256];
    boolean keyJustPressed = false;
    boolean[] justPressedKeys = new boolean[256];
    int[] touchX = new int[20];
    int[] touchY = new int[20];
    boolean isTouched[] = new boolean[20];
    boolean justTouched = false;
    InputProcessor processor = null;
    private final int port;
    public final String[] ips;

    public RemoteInput() {
        this(DEFAULT_PORT);
    }

    public RemoteInput(RemoteInputListener listener) {
        this(DEFAULT_PORT, listener);
    }

    public RemoteInput(int port) {
        this(port, null);
    }

    public RemoteInput(int port, RemoteInputListener listener) {
        this.listener = listener;
        try {
            this.port = port;
            serverSocket = new ServerSocket(port);
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
            InetAddress[] allByName
                    = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            ips = new String[allByName.length];
            for (int i = 0; i < allByName.length; i++) {
                ips[i] = allByName[i].getHostAddress();
            }
        } catch (Exception e) {
            throw new GameEngineRuntimeException("Couldn't open listening socket at port '"
                    + port + "'", e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                connected = false;
                if (listener != null) listener.onDisconnected();

                System.out.println("listening, port " + port);
                Socket socket = null;

                socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(3000);
                connected = true;
                if (listener != null) listener.onConnected();

                DataInputStream in = new DataInputStream(socket.getInputStream());
                multiTouch = in.readBoolean();
                while (true) {
                    int event = in.readInt();
                    KeyEvent keyEvent = null;
                    TouchEvent touchEvent = null;
                    switch (event) {
                        case RemoteSender.ACCEL:
                            accel[0] = in.readFloat();
                            accel[1] = in.readFloat();
                            accel[2] = in.readFloat();
                            break;
                        case RemoteSender.COMPASS:
                            compass[0] = in.readFloat();
                            compass[1] = in.readFloat();
                            compass[2] = in.readFloat();
                            break;
                        case RemoteSender.SIZE:
                            remoteWidth = in.readFloat();
                            remoteHeight = in.readFloat();
                            break;
                        case RemoteSender.KEY_DOWN:
                            keyEvent = new KeyEvent();
                            keyEvent.keyCode = in.readInt();
                            keyEvent.type = KeyEvent.KEY_DOWN;
                            break;
                        case RemoteSender.KEY_UP:
                            keyEvent = new KeyEvent();
                            keyEvent.keyCode = in.readInt();
                            keyEvent.type = KeyEvent.KEY_UP;
                            break;
                        case RemoteSender.KEY_TYPED:
                            keyEvent = new KeyEvent();
                            keyEvent.keyChar = in.readChar();
                            keyEvent.type = KeyEvent.KEY_TYPED;
                            break;
                        case RemoteSender.TOUCH_DOWN:
                            touchEvent = new TouchEvent();
                            touchEvent.x = (int) ((in.readInt() / remoteWidth)
                                    * GameEngine.graphics.getWidth());
                            touchEvent.y = (int) ((in.readInt() / remoteHeight)
                                    * GameEngine.graphics.getHeight());
                            touchEvent.pointer = in.readInt();
                            touchEvent.type = TouchEvent.TOUCH_DOWN;
                            break;
                        case RemoteSender.TOUCH_UP:
                            touchEvent = new TouchEvent();
                            touchEvent.x = (int) ((in.readInt() / remoteWidth)
                                    * GameEngine.graphics.getWidth());
                            touchEvent.y = (int) ((in.readInt() / remoteHeight)
                                    * GameEngine.graphics.getHeight());
                            touchEvent.pointer = in.readInt();
                            touchEvent.type = TouchEvent.TOUCH_UP;
                            break;
                        case RemoteSender.TOUCH_DRAGGED:
                            touchEvent = new TouchEvent();
                            touchEvent.x = (int) ((in.readInt() / remoteWidth)
                                    * GameEngine.graphics.getWidth());
                            touchEvent.y = (int) ((in.readInt() / remoteHeight)
                                    * GameEngine.graphics.getHeight());
                            touchEvent.pointer = in.readInt();
                            touchEvent.type = TouchEvent.TOUCH_DRAGGED;
                            break;
                    }

                    GameEngine.app.postRunnable(new EventTrigger(touchEvent, keyEvent));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public float getAccelerometerX() {
        return accel[0];
    }

    @Override
    public float getAccelerometerY() {
        return accel[1];
    }

    @Override
    public float getAccelerometerZ() {
        return accel[2];
    }

    @Override
    public int getX() {
        return touchX[0];
    }

    @Override
    public int getX(int pointer) {
        return touchX[pointer];
    }

    @Override
    public int getY() {
        return touchY[0];
    }

    @Override
    public int getY(int pointer) {
        return touchY[pointer];
    }

    @Override
    public boolean isTouched() {
        return isTouched[0];
    }

    @Override
    public boolean justTouched() {
        return justTouched;
    }

    @Override
    public boolean isTouched(int pointer) {
        return isTouched[pointer];
    }

    @Override
    public boolean isButtonPressed(int button) {
        if (button != Buttons.LEFT) return false;
        for (int i = 0; i < isTouched.length; i++)
            if (isTouched[i]) return true;
        return false;
    }

    @Override
    public boolean isKeyPressed(int key) {
        if (key == Input.Keys.ANY_KEY) {
            return keyCount > 0;
        }
        if (key < 0 || key > 255) {
            return false;
        }
        return keys[key];
    }

    @Override
    public boolean isKeyJustPressed(int key) {
        if (key == Input.Keys.ANY_KEY) {
            return keyJustPressed;
        }
        if (key < 0 || key > 255) {
            return false;
        }
        return justPressedKeys[key];
    }

    @Override
    public void getTextInput(TextInputListener listener, String title,
                             String text) {
        GameEngine.app.getInput().getTextInput(listener, title, text);
    }

    @Override
    public void getPlaceholderTextInput(TextInputListener listener,
                                        String title, String placeholder) {
        GameEngine.app.getInput().getPlaceholderTextInput(listener,
                title, placeholder);
    }

    @Override
    public void setOnscreenKeyboardVisible(boolean visible) {
    }

    @Override
    public void vibrate(int milliseconds) {

    }

    @Override
    public void vibrate(long[] pattern, int repeat) {

    }

    @Override
    public void cancelVibrate() {

    }

    @Override
    public float getAzimuth() {
        return compass[0];
    }

    @Override
    public float getPitch() {
        return compass[1];
    }

    @Override
    public float getRoll() {
        return compass[2];
    }

    @Override
    public void setCatchBackKey(boolean catchBack) {

    }

    @Override
    public void setInputProcessor(InputProcessor processor) {
        this.processor = processor;
    }

    @Override
    public InputProcessor getInputProcessor() {
        return this.processor;
    }

    /**
     * @return the IP addresses {@link RemoteSender} or remote should
     * connect to. Most likely the LAN addresses if behind a NAT.
     */
    public String[] getIPs() {
        return ips;
    }

    @Override
    public boolean isPeripheralAvailable(Peripheral peripheral) {
        if (peripheral == Peripheral.Accelerometer) return true;
        if (peripheral == Peripheral.Compass) return true;
        if (peripheral == Peripheral.MultitouchScreen) return multiTouch;
        return false;
    }

    @Override
    public int getRotation() {
        return 0;
    }

    @Override
    public Orientation getNativeOrientation() {
        return Orientation.Landscape;
    }

    @Override
    public void setCursorCaught(boolean catched) {

    }

    @Override
    public boolean isCursorCaught() {
        return false;
    }

    @Override
    public int getDeltaX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getDeltaX(int pointer) {
        return 0;
    }

    @Override
    public int getDeltaY() {
        return 0;
    }

    @Override
    public int getDeltaY(int pointer) {
        return 0;
    }

    @Override
    public void setCursorPosition(int x, int y) {
    }

    @Override
    public void setCursorImage(Pixmap pixmap, int xHotspot, int yHotspot) {
    }

    @Override
    public void setCatchMenuKey(boolean catchMenu) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getCurrentEventTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void getRotationMatrix(float[] matrix) {
        // TODO Auto-generated method stub

    }
}
