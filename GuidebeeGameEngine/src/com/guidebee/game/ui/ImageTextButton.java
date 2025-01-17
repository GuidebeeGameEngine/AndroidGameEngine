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

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.BitmapFont;
import com.guidebee.game.graphics.Color;
import com.guidebee.game.ui.drawable.Drawable;
import com.guidebee.utils.Scaling;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * A button with a child {@link Image} and {@link Label}.
 *
 * @author Nathan Sweet
 * @see ImageButton
 * @see TextButton
 * @see Button
 */
public class ImageTextButton extends Button {
    private final Image image;
    private final Label label;
    private ImageTextButtonStyle style;

    public ImageTextButton(String text, Skin skin) {
        this(text, skin.get(ImageTextButtonStyle.class));
        setSkin(skin);
    }

    public ImageTextButton(String text, Skin skin, String styleName) {
        this(text, skin.get(styleName, ImageTextButtonStyle.class));
        setSkin(skin);
    }

    public ImageTextButton(String text, ImageTextButtonStyle style) {
        super(style);
        this.style = style;

        defaults().space(3);

        image = new Image();
        image.setScaling(Scaling.fit);
        add(image);

        label = new Label(text, new Label.LabelStyle(style.font, style.fontColor));
        label.setAlignment(Align.center);
        add(label);

        setStyle(style);

        setSize(getPrefWidth(), getPrefHeight());
    }

    public void setStyle(ButtonStyle style) {
        if (!(style instanceof ImageTextButtonStyle))
            throw new IllegalArgumentException("style must be a ImageTextButtonStyle.");
        super.setStyle(style);
        this.style = (ImageTextButtonStyle) style;
        if (image != null) updateImage();
        if (label != null) {
            ImageTextButtonStyle textButtonStyle = (ImageTextButtonStyle) style;
            Label.LabelStyle labelStyle = label.getStyle();
            labelStyle.font = textButtonStyle.font;
            labelStyle.fontColor = textButtonStyle.fontColor;
            label.setStyle(labelStyle);
        }
    }

    public ImageTextButtonStyle getStyle() {
        return style;
    }

    private void updateImage() {
        boolean isPressed = isPressed();
        if (isDisabled && style.imageDisabled != null)
            image.setDrawable(style.imageDisabled);
        else if (isPressed && style.imageDown != null)
            image.setDrawable(style.imageDown);
        else if (isChecked && style.imageChecked != null)
            image.setDrawable((style.imageCheckedOver != null
                    && isOver()) ? style.imageCheckedOver : style.imageChecked);
        else if (isOver() && style.imageOver != null)
            image.setDrawable(style.imageOver);
        else if (style.imageUp != null) //
            image.setDrawable(style.imageUp);
    }

    public void draw(Batch batch, float parentAlpha) {
        updateImage();
        Color fontColor;
        if (isDisabled && style.disabledFontColor != null)
            fontColor = style.disabledFontColor;
        else if (isPressed() && style.downFontColor != null)
            fontColor = style.downFontColor;
        else if (isChecked && style.checkedFontColor != null)
            fontColor = (isOver() && style.checkedOverFontColor != null)
                    ? style.checkedOverFontColor : style.checkedFontColor;
        else if (isOver() && style.overFontColor != null)
            fontColor = style.overFontColor;
        else
            fontColor = style.fontColor;
        if (fontColor != null) label.getStyle().fontColor = fontColor;
        super.draw(batch, parentAlpha);
    }

    public Image getImage() {
        return image;
    }

    public Cell getImageCell() {
        return getCell(image);
    }

    public Label getLabel() {
        return label;
    }

    public Cell getLabelCell() {
        return getCell(label);
    }

    public void setText(CharSequence text) {
        label.setText(text);
    }

    public CharSequence getText() {
        return label.getText();
    }

    /**
     * The style for an image text button, see {@link ImageTextButton}.
     *
     * @author Nathan Sweet
     */
    static public class ImageTextButtonStyle extends TextButton.TextButtonStyle {
        /**
         * Optional.
         */
        public Drawable imageUp, imageDown, imageOver, imageChecked,
                imageCheckedOver, imageDisabled;

        public ImageTextButtonStyle() {
        }

        public ImageTextButtonStyle(Drawable up, Drawable down,
                                    Drawable checked, BitmapFont font) {
            super(up, down, checked, font);
        }

        public ImageTextButtonStyle(ImageTextButtonStyle style) {
            super(style);
            if (style.imageUp != null) this.imageUp = style.imageUp;
            if (style.imageDown != null) this.imageDown = style.imageDown;
            if (style.imageOver != null) this.imageOver = style.imageOver;
            if (style.imageChecked != null) this.imageChecked = style.imageChecked;
            if (style.imageCheckedOver != null) this.imageCheckedOver = style.imageCheckedOver;
            if (style.imageDisabled != null) this.imageDisabled = style.imageDisabled;
        }

        public ImageTextButtonStyle(TextButton.TextButtonStyle style) {
            super(style);
        }
    }
}
