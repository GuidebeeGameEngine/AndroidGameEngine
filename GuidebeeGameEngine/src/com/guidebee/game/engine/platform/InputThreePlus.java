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
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import com.guidebee.game.Application;
import com.guidebee.game.Configuration;

import java.util.ArrayList;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * Subclass of Input, used on Android +3.x to get generic motion events for
 * things like gamepads/joysticks and so on.
 *
 * @author mzechner
 */
public class InputThreePlus extends Input implements OnGenericMotionListener {
    ArrayList<OnGenericMotionListener> genericMotionListeners = new ArrayList();

    public InputThreePlus(Application activity, Context context,
                          Object view, Configuration config) {
        super(activity, context, view, config);
        // we hook into View, for LWPs we call onTouch below directly from
        // within the AndroidLivewallpaperEngine#onTouchEvent() method.
        if (view instanceof View) {
            View v = (View) view;
            v.setOnGenericMotionListener(this);
        }
    }

    @Override
    public boolean onGenericMotion(View view, MotionEvent event) {
        for (int i = 0, n = genericMotionListeners.size(); i < n; i++)
            if (genericMotionListeners.get(i).onGenericMotion(view, event)) return true;
        return false;
    }

    public void addGenericMotionListener(OnGenericMotionListener listener) {

        genericMotionListeners.add(listener);
    }
}
