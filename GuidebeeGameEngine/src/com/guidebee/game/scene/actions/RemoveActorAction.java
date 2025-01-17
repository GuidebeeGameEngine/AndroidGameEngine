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
package com.guidebee.game.scene.actions;

//--------------------------------- IMPORTS ------------------------------------
import com.guidebee.game.engine.scene.Actor;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * Removes an actor from the stage.
 *
 * @author Nathan Sweet
 */
public class RemoveActorAction extends Action {
    private Actor removeActor;
    private boolean removed;

    public boolean act(float delta) {
        if (!removed) {
            removed = true;
            (removeActor != null ? removeActor : actor).remove();
        }
        return true;
    }

    public void restart() {
        removed = false;
    }

    public void reset() {
        super.reset();
        removeActor = null;
    }

    public Actor getRemoveActor() {
        return removeActor;
    }

    /**
     * Sets the actor to remove. If null (the default),
     * the {@link #getActor() actor} will be used.
     */
    public void setRemoveActor(Actor removeActor) {
        this.removeActor = removeActor;
    }
}
