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
package com.guidebee.game.ui;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.utils.collections.Array;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Manages a group of buttons to enforce a minimum and maximum number of checked
 * buttons. This enables "radio button"
 * functionality and more. A button may only be in one group at a time.
 *
 * @author Nathan Sweet
 */
public class ButtonGroup {
    private final Array<Button> buttons = new Array();
    private Array<Button> checkedButtons = new Array(1);
    private int minCheckCount, maxCheckCount = 1;
    private boolean uncheckLast = true;
    private Button lastChecked;

    public ButtonGroup() {
        minCheckCount = 1;
    }

    public ButtonGroup(Button... buttons) {
        minCheckCount = 0;
        add(buttons);
        minCheckCount = 1;
    }

    public void add(Button button) {
        if (button == null)
            throw new IllegalArgumentException("button cannot be null.");
        button.buttonGroup = null;
        boolean shouldCheck = button.isChecked() || buttons.size < minCheckCount;
        button.setChecked(false);
        button.buttonGroup = this;
        buttons.add(button);
        if (shouldCheck) button.setChecked(true);
    }

    public void add(Button... buttons) {
        if (buttons == null)
            throw new IllegalArgumentException("buttons cannot be null.");
        for (int i = 0, n = buttons.length; i < n; i++)
            add(buttons[i]);
    }

    public void remove(Button button) {
        if (button == null)
            throw new IllegalArgumentException("button cannot be null.");
        button.buttonGroup = null;
        buttons.removeValue(button, true);
    }

    public void remove(Button... buttons) {
        if (buttons == null)
            throw new IllegalArgumentException("buttons cannot be null.");
        for (int i = 0, n = buttons.length; i < n; i++)
            remove(buttons[i]);
    }

    /**
     * Sets the first {@link TextButton} with the specified text to checked.
     */
    public void setChecked(String text) {
        if (text == null)
            throw new IllegalArgumentException("text cannot be null.");
        for (int i = 0, n = buttons.size; i < n; i++) {
            Button button = buttons.get(i);
            if (button instanceof TextButton
                    && text.contentEquals(((TextButton) button).getText())) {
                button.setChecked(true);
                return;
            }
        }
    }

    /**
     * Called when a button is checked or unchecked.
     *
     * @return true if the new state should be allowed.
     */
    protected boolean canCheck(Button button, boolean newState) {
        if (button.isChecked == newState) return false;

        if (!newState) {
            // Keep button checked to enforce minCheckCount.
            if (checkedButtons.size <= minCheckCount) return false;
            checkedButtons.removeValue(button, true);
        } else {
            // Keep button unchecked to enforce maxCheckCount.
            if (maxCheckCount != -1 && checkedButtons.size >= maxCheckCount) {
                if (uncheckLast) {
                    int old = minCheckCount;
                    minCheckCount = 0;
                    lastChecked.setChecked(false);
                    minCheckCount = old;
                } else
                    return false;
            }
            checkedButtons.add(button);
            lastChecked = button;
        }

        return true;
    }

    /**
     * Sets all buttons' {@link Button#isChecked()} to false,
     * regardless of {@link #setMinCheckCount(int)}.
     */
    public void uncheckAll() {
        int old = minCheckCount;
        minCheckCount = 0;
        for (int i = 0, n = buttons.size; i < n; i++) {
            Button button = buttons.get(i);
            button.setChecked(false);
        }
        minCheckCount = old;
    }

    /**
     * @return the first checked button, or null.
     */
    public Button getChecked() {
        if (checkedButtons.size > 0) return checkedButtons.get(0);
        return null;
    }

    public Array<Button> getAllChecked() {
        return checkedButtons;
    }

    public Array<Button> getButtons() {
        return buttons;
    }

    /**
     * Sets the minimum number of buttons that must be checked. Default is 1.
     */
    public void setMinCheckCount(int minCheckCount) {
        this.minCheckCount = minCheckCount;
    }

    /**
     * Sets the maximum number of buttons that can be checked.
     * Set to -1 for no maximum. Default is 1.
     */
    public void setMaxCheckCount(int maxCheckCount) {
        if (maxCheckCount == 0) maxCheckCount = -1;
        this.maxCheckCount = maxCheckCount;
    }

    /**
     * If true, when the maximum number of buttons are checked and an
     * additional button is checked, the last button to be checked
     * is unchecked so that the maximum is not exceeded. If false,
     * additional buttons beyond the maximum are not allowed to be
     * checked. Default is true.
     */
    public void setUncheckLast(boolean uncheckLast) {
        this.uncheckLast = uncheckLast;
    }
}
