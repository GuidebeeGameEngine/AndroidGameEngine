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
import android.view.View;
import com.guidebee.game.activity.BaseActivity;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * Allows immersive mode support while maintaining compatibility with Android
 * versions before API Level 19 (4.4)
 *
 * @author Unkn0wn0ne
 */
public class VisibilityListener {

    public void createListener(final BaseActivity application) {
        try {
            View rootView = application.getApplicationWindow().getDecorView();
            rootView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int arg0) {
                    application.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            application.useImmersiveMode(true);
                        }
                    });
                }
            });
        } catch (Throwable t) {
            application.log("Application",
                    "Can't create OnSystemUiVisibilityChangeListener", t);
        }
    }
}
