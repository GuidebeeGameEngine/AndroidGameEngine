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
import com.guidebee.game.engine.scene.Actor;
import com.guidebee.utils.Pool;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * A cell for a {@link Table}.
 *
 * @author Nathan Sweet
 */
public class Cell<T extends Actor> implements Pool.Poolable {
    Value minWidth, minHeight;
    Value prefWidth, prefHeight;
    Value maxWidth, maxHeight;
    Value spaceTop, spaceLeft, spaceBottom, spaceRight;
    Value padTop, padLeft, padBottom, padRight;
    Float fillX, fillY;
    Integer align;
    Integer expandX, expandY;
    Integer colspan;
    Boolean uniformX, uniformY;

    Actor actor;
    float actorX, actorY;
    float actorWidth, actorHeight;

    private Table table;
    boolean endRow;
    int column, row;
    int cellAboveIndex = -1;
    float computedPadTop, computedPadLeft, computedPadBottom, computedPadRight;

    public void setLayout(Table table) {
        this.table = table;
    }

    void set(Cell defaults) {
        minWidth = defaults.minWidth;
        minHeight = defaults.minHeight;
        prefWidth = defaults.prefWidth;
        prefHeight = defaults.prefHeight;
        maxWidth = defaults.maxWidth;
        maxHeight = defaults.maxHeight;
        spaceTop = defaults.spaceTop;
        spaceLeft = defaults.spaceLeft;
        spaceBottom = defaults.spaceBottom;
        spaceRight = defaults.spaceRight;
        padTop = defaults.padTop;
        padLeft = defaults.padLeft;
        padBottom = defaults.padBottom;
        padRight = defaults.padRight;
        fillX = defaults.fillX;
        fillY = defaults.fillY;
        align = defaults.align;
        expandX = defaults.expandX;
        expandY = defaults.expandY;
        colspan = defaults.colspan;
        uniformX = defaults.uniformX;
        uniformY = defaults.uniformY;
    }

    void merge(Cell cell) {
        if (cell == null) return;
        if (cell.minWidth != null) minWidth = cell.minWidth;
        if (cell.minHeight != null) minHeight = cell.minHeight;
        if (cell.prefWidth != null) prefWidth = cell.prefWidth;
        if (cell.prefHeight != null) prefHeight = cell.prefHeight;
        if (cell.maxWidth != null) maxWidth = cell.maxWidth;
        if (cell.maxHeight != null) maxHeight = cell.maxHeight;
        if (cell.spaceTop != null) spaceTop = cell.spaceTop;
        if (cell.spaceLeft != null) spaceLeft = cell.spaceLeft;
        if (cell.spaceBottom != null) spaceBottom = cell.spaceBottom;
        if (cell.spaceRight != null) spaceRight = cell.spaceRight;
        if (cell.padTop != null) padTop = cell.padTop;
        if (cell.padLeft != null) padLeft = cell.padLeft;
        if (cell.padBottom != null) padBottom = cell.padBottom;
        if (cell.padRight != null) padRight = cell.padRight;
        if (cell.fillX != null) fillX = cell.fillX;
        if (cell.fillY != null) fillY = cell.fillY;
        if (cell.align != null) align = cell.align;
        if (cell.expandX != null) expandX = cell.expandX;
        if (cell.expandY != null) expandY = cell.expandY;
        if (cell.colspan != null) colspan = cell.colspan;
        if (cell.uniformX != null) uniformX = cell.uniformX;
        if (cell.uniformY != null) uniformY = cell.uniformY;
    }

    /**
     * Sets the actor in this cell and adds the actor to the cell's table.
     * If null, removes any current actor.
     */
    public <A extends Actor> Cell<A> setActor(A newActor) {
        if (actor != null) actor.remove();
        if (actor != newActor) {
            actor = newActor;
            if (newActor != null) table.addActor(newActor);
        }
        return (Cell<A>) this;
    }

    /**
     * Returns the actor for this cell, or null.
     */
    public T getActor() {
        return (T) actor;
    }

    /**
     * Returns true if the cell's actor is not null.
     */
    public boolean hasActor() {
        return actor != null;
    }

    /**
     * Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight,
     * and maxHeight to the specified value.
     */
    public Cell<T> size(Value size) {
        if (size == null) throw new IllegalArgumentException("size cannot be null.");
        minWidth = size;
        minHeight = size;
        prefWidth = size;
        prefHeight = size;
        maxWidth = size;
        maxHeight = size;
        return this;
    }

    /**
     * Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight,
     * and maxHeight to the specified values.
     */
    public Cell<T> size(Value width, Value height) {
        if (width == null) throw new IllegalArgumentException("width cannot be null.");
        if (height == null) throw new IllegalArgumentException("height cannot be null.");
        minWidth = width;
        minHeight = height;
        prefWidth = width;
        prefHeight = height;
        maxWidth = width;
        maxHeight = height;
        return this;
    }

    /**
     * Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight,
     * and maxHeight to the specified value.
     */
    public Cell<T> size(float size) {
        size(new Value.Fixed(size));
        return this;
    }

    /**
     * Sets the minWidth, prefWidth, maxWidth, minHeight, prefHeight,
     * and maxHeight to the specified values.
     */
    public Cell<T> size(float width, float height) {
        size(new Value.Fixed(width), new Value.Fixed(height));
        return this;
    }

    /**
     * Sets the minWidth, prefWidth, and maxWidth to the specified value.
     */
    public Cell<T> width(Value width) {
        if (width == null) throw new IllegalArgumentException("width cannot be null.");
        minWidth = width;
        prefWidth = width;
        maxWidth = width;
        return this;
    }

    /**
     * Sets the minWidth, prefWidth, and maxWidth to the specified value.
     */
    public Cell<T> width(float width) {
        width(new Value.Fixed(width));
        return this;
    }

    /**
     * Sets the minHeight, prefHeight, and maxHeight to the specified value.
     */
    public Cell<T> height(Value height) {
        if (height == null) throw new IllegalArgumentException("height cannot be null.");
        minHeight = height;
        prefHeight = height;
        maxHeight = height;
        return this;
    }

    /**
     * Sets the minHeight, prefHeight, and maxHeight to the specified value.
     */
    public Cell<T> height(float height) {
        height(new Value.Fixed(height));
        return this;
    }

    /**
     * Sets the minWidth and minHeight to the specified value.
     */
    public Cell<T> minSize(Value size) {
        if (size == null) throw new IllegalArgumentException("size cannot be null.");
        minWidth = size;
        minHeight = size;
        return this;
    }

    /**
     * Sets the minWidth and minHeight to the specified values.
     */
    public Cell<T> minSize(Value width, Value height) {
        if (width == null) throw new IllegalArgumentException("width cannot be null.");
        if (height == null) throw new IllegalArgumentException("height cannot be null.");
        minWidth = width;
        minHeight = height;
        return this;
    }

    public Cell<T> minWidth(Value minWidth) {
        if (minWidth == null) throw new IllegalArgumentException("minWidth cannot be null.");
        this.minWidth = minWidth;
        return this;
    }

    public Cell<T> minHeight(Value minHeight) {
        if (minHeight == null) throw new IllegalArgumentException("minHeight cannot be null.");
        this.minHeight = minHeight;
        return this;
    }

    /**
     * Sets the minWidth and minHeight to the specified value.
     */
    public Cell<T> minSize(float size) {
        minSize(new Value.Fixed(size));
        return this;
    }

    /**
     * Sets the minWidth and minHeight to the specified values.
     */
    public Cell<T> minSize(float width, float height) {
        minSize(new Value.Fixed(width));
        return this;
    }

    public Cell<T> minWidth(float minWidth) {
        this.minWidth = new Value.Fixed(minWidth);
        return this;
    }

    public Cell<T> minHeight(float minHeight) {
        this.minHeight = new Value.Fixed(minHeight);
        return this;
    }

    /**
     * Sets the prefWidth and prefHeight to the specified value.
     */
    public Cell<T> prefSize(Value size) {
        if (size == null) throw new IllegalArgumentException("size cannot be null.");
        prefWidth = size;
        prefHeight = size;
        return this;
    }

    /**
     * Sets the prefWidth and prefHeight to the specified values.
     */
    public Cell<T> prefSize(Value width, Value height) {
        if (width == null) throw new IllegalArgumentException("width cannot be null.");
        if (height == null) throw new IllegalArgumentException("height cannot be null.");
        prefWidth = width;
        prefHeight = height;
        return this;
    }

    public Cell<T> prefWidth(Value prefWidth) {
        if (prefWidth == null) throw new IllegalArgumentException("prefWidth cannot be null.");
        this.prefWidth = prefWidth;
        return this;
    }

    public Cell<T> prefHeight(Value prefHeight) {
        if (prefHeight == null) throw new IllegalArgumentException("prefHeight cannot be null.");
        this.prefHeight = prefHeight;
        return this;
    }

    /**
     * Sets the prefWidth and prefHeight to the specified value.
     */
    public Cell<T> prefSize(float width, float height) {
        prefSize(new Value.Fixed(width), new Value.Fixed(height));
        return this;
    }

    /**
     * Sets the prefWidth and prefHeight to the specified values.
     */
    public Cell<T> prefSize(float size) {
        prefSize(new Value.Fixed(size));
        return this;
    }

    public Cell<T> prefWidth(float prefWidth) {
        this.prefWidth = new Value.Fixed(prefWidth);
        return this;
    }

    public Cell<T> prefHeight(float prefHeight) {
        this.prefHeight = new Value.Fixed(prefHeight);
        return this;
    }

    /**
     * Sets the maxWidth and maxHeight to the specified value.
     */
    public Cell<T> maxSize(Value size) {
        if (size == null) throw new IllegalArgumentException("size cannot be null.");
        maxWidth = size;
        maxHeight = size;
        return this;
    }

    /**
     * Sets the maxWidth and maxHeight to the specified values.
     */
    public Cell<T> maxSize(Value width, Value height) {
        if (width == null) throw new IllegalArgumentException("width cannot be null.");
        if (height == null) throw new IllegalArgumentException("height cannot be null.");
        maxWidth = width;
        maxHeight = height;
        return this;
    }

    public Cell<T> maxWidth(Value maxWidth) {
        if (maxWidth == null) throw new IllegalArgumentException("maxWidth cannot be null.");
        this.maxWidth = maxWidth;
        return this;
    }

    public Cell<T> maxHeight(Value maxHeight) {
        if (maxHeight == null) throw new IllegalArgumentException("maxHeight cannot be null.");
        this.maxHeight = maxHeight;
        return this;
    }

    /**
     * Sets the maxWidth and maxHeight to the specified value.
     */
    public Cell<T> maxSize(float size) {
        maxSize(new Value.Fixed(size));
        return this;
    }

    /**
     * Sets the maxWidth and maxHeight to the specified values.
     */
    public Cell<T> maxSize(float width, float height) {
        maxSize(new Value.Fixed(width));
        return this;
    }

    public Cell<T> maxWidth(float maxWidth) {
        this.maxWidth = new Value.Fixed(maxWidth);
        return this;
    }

    public Cell<T> maxHeight(float maxHeight) {
        this.maxHeight = new Value.Fixed(maxHeight);
        return this;
    }

    /**
     * Sets the spaceTop, spaceLeft, spaceBottom, and spaceRight to the specified value.
     */
    public Cell<T> space(Value space) {
        if (space == null) throw new IllegalArgumentException("space cannot be null.");
        spaceTop = space;
        spaceLeft = space;
        spaceBottom = space;
        spaceRight = space;
        return this;
    }

    public Cell<T> space(Value top, Value left, Value bottom, Value right) {
        if (top == null) throw new IllegalArgumentException("top cannot be null.");
        if (left == null) throw new IllegalArgumentException("left cannot be null.");
        if (bottom == null) throw new IllegalArgumentException("bottom cannot be null.");
        if (right == null) throw new IllegalArgumentException("right cannot be null.");
        spaceTop = top;
        spaceLeft = left;
        spaceBottom = bottom;
        spaceRight = right;
        return this;
    }

    public Cell<T> spaceTop(Value spaceTop) {
        if (spaceTop == null) throw new IllegalArgumentException("spaceTop cannot be null.");
        this.spaceTop = spaceTop;
        return this;
    }

    public Cell<T> spaceLeft(Value spaceLeft) {
        if (spaceLeft == null) throw new IllegalArgumentException("spaceLeft cannot be null.");
        this.spaceLeft = spaceLeft;
        return this;
    }

    public Cell<T> spaceBottom(Value spaceBottom) {
        if (spaceBottom == null) throw new IllegalArgumentException("spaceBottom cannot be null.");
        this.spaceBottom = spaceBottom;
        return this;
    }

    public Cell<T> spaceRight(Value spaceRight) {
        if (spaceRight == null) throw new IllegalArgumentException("spaceRight cannot be null.");
        this.spaceRight = spaceRight;
        return this;
    }

    /**
     * Sets the spaceTop, spaceLeft, spaceBottom, and spaceRight to the specified value.
     */
    public Cell<T> space(float space) {
        if (space < 0) throw new IllegalArgumentException("space cannot be < 0.");
        Value value = new Value.Fixed(space);
        spaceTop = value;
        spaceLeft = value;
        spaceBottom = value;
        spaceRight = value;
        return this;
    }

    public Cell<T> space(float top, float left, float bottom, float right) {
        if (top < 0) throw new IllegalArgumentException("top cannot be < 0.");
        if (left < 0) throw new IllegalArgumentException("left cannot be < 0.");
        if (bottom < 0) throw new IllegalArgumentException("bottom cannot be < 0.");
        if (right < 0) throw new IllegalArgumentException("right cannot be < 0.");
        spaceTop = new Value.Fixed(top);
        spaceLeft = new Value.Fixed(left);
        spaceBottom = new Value.Fixed(bottom);
        spaceRight = new Value.Fixed(right);
        return this;
    }

    public Cell<T> spaceTop(float spaceTop) {
        if (spaceTop < 0) throw new IllegalArgumentException("spaceTop cannot be < 0.");
        this.spaceTop = new Value.Fixed(spaceTop);
        return this;
    }

    public Cell<T> spaceLeft(float spaceLeft) {
        if (spaceLeft < 0) throw new IllegalArgumentException("spaceLeft cannot be < 0.");
        this.spaceLeft = new Value.Fixed(spaceLeft);
        return this;
    }

    public Cell<T> spaceBottom(float spaceBottom) {
        if (spaceBottom < 0) throw new IllegalArgumentException("spaceBottom cannot be < 0.");
        this.spaceBottom = new Value.Fixed(spaceBottom);
        return this;
    }

    public Cell<T> spaceRight(float spaceRight) {
        if (spaceRight < 0) throw new IllegalArgumentException("spaceRight cannot be < 0.");
        this.spaceRight = new Value.Fixed(spaceRight);
        return this;
    }

    /**
     * Sets the padTop, padLeft, padBottom, and padRight to the specified value.
     */
    public Cell<T> pad(Value pad) {
        if (pad == null) throw new IllegalArgumentException("pad cannot be null.");
        padTop = pad;
        padLeft = pad;
        padBottom = pad;
        padRight = pad;
        return this;
    }

    public Cell<T> pad(Value top, Value left, Value bottom, Value right) {
        if (top == null) throw new IllegalArgumentException("top cannot be null.");
        if (left == null) throw new IllegalArgumentException("left cannot be null.");
        if (bottom == null) throw new IllegalArgumentException("bottom cannot be null.");
        if (right == null) throw new IllegalArgumentException("right cannot be null.");
        padTop = top;
        padLeft = left;
        padBottom = bottom;
        padRight = right;
        return this;
    }

    public Cell<T> padTop(Value padTop) {
        if (padTop == null) throw new IllegalArgumentException("padTop cannot be null.");
        this.padTop = padTop;
        return this;
    }

    public Cell<T> padLeft(Value padLeft) {
        if (padLeft == null) throw new IllegalArgumentException("padLeft cannot be null.");
        this.padLeft = padLeft;
        return this;
    }

    public Cell<T> padBottom(Value padBottom) {
        if (padBottom == null) throw new IllegalArgumentException("padBottom cannot be null.");
        this.padBottom = padBottom;
        return this;
    }

    public Cell<T> padRight(Value padRight) {
        if (padRight == null) throw new IllegalArgumentException("padRight cannot be null.");
        this.padRight = padRight;
        return this;
    }

    /**
     * Sets the padTop, padLeft, padBottom, and padRight to the specified value.
     */
    public Cell<T> pad(float pad) {
        Value value = new Value.Fixed(pad);
        padTop = value;
        padLeft = value;
        padBottom = value;
        padRight = value;
        return this;
    }

    public Cell<T> pad(float top, float left, float bottom, float right) {
        padTop = new Value.Fixed(top);
        padLeft = new Value.Fixed(left);
        padBottom = new Value.Fixed(bottom);
        padRight = new Value.Fixed(right);
        return this;
    }

    public Cell<T> padTop(float padTop) {
        this.padTop = new Value.Fixed(padTop);
        return this;
    }

    public Cell<T> padLeft(float padLeft) {
        this.padLeft = new Value.Fixed(padLeft);
        return this;
    }

    public Cell<T> padBottom(float padBottom) {
        this.padBottom = new Value.Fixed(padBottom);
        return this;
    }

    public Cell<T> padRight(float padRight) {
        this.padRight = new Value.Fixed(padRight);
        return this;
    }

    /**
     * Sets fillX and fillY to 1.
     */
    public Cell<T> fill() {
        fillX = 1f;
        fillY = 1f;
        return this;
    }

    /**
     * Sets fillX to 1.
     */
    public Cell<T> fillX() {
        fillX = 1f;
        return this;
    }

    /**
     * Sets fillY to 1.
     */
    public Cell<T> fillY() {
        fillY = 1f;
        return this;
    }

    public Cell<T> fill(Float x, Float y) {
        fillX = x;
        fillY = y;
        return this;
    }

    /**
     * Sets fillX and fillY to 1 if true, 0 if false.
     */
    public Cell<T> fill(boolean x, boolean y) {
        fillX = x ? 1f : 0;
        fillY = y ? 1f : 0;
        return this;
    }

    /**
     * Sets fillX and fillY to 1 if true, 0 if false.
     */
    public Cell<T> fill(boolean fill) {
        fillX = fill ? 1f : 0;
        fillY = fill ? 1f : 0;
        return this;
    }

    /**
     * Sets the alignment of the actor within the cell. Set to {@link Align#center},
     * {@link Align#top}, {@link Align#bottom},
     * {@link Align#left}, {@link Align#right}, or any combination of those.
     */
    public Cell<T> align(Integer align) {
        this.align = align;
        return this;
    }

    /**
     * Sets the alignment of the actor within the cell to {@link Align#center}.
     * This clears any other alignment.
     */
    public Cell<T> center() {
        align = Align.center;
        return this;
    }

    /**
     * Adds {@link Align#top} and clears {@link Align#bottom} for the alignment
     * of the actor within the cell.
     */
    public Cell<T> top() {
        if (align == null)
            align = Align.top;
        else {
            align |= Align.top;
            align &= ~Align.bottom;
        }
        return this;
    }

    /**
     * Adds {@link Align#left} and clears {@link Align#right} for the alignment
     * of the actor within the cell.
     */
    public Cell<T> left() {
        if (align == null)
            align = Align.left;
        else {
            align |= Align.left;
            align &= ~Align.right;
        }
        return this;
    }

    /**
     * Adds {@link Align#bottom} and clears {@link Align#top} for the alignment
     * of the actor within the cell.
     */
    public Cell<T> bottom() {
        if (align == null)
            align = Align.bottom;
        else {
            align |= Align.bottom;
            align &= ~Align.top;
        }
        return this;
    }

    /**
     * Adds {@link Align#right} and clears {@link Align#left} for the alignment
     * of the actor within the cell.
     */
    public Cell<T> right() {
        if (align == null)
            align = Align.right;
        else {
            align |= Align.right;
            align &= ~Align.left;
        }
        return this;
    }

    /**
     * Sets expandX and expandY to 1.
     */
    public Cell<T> expand() {
        expandX = 1;
        expandY = 1;
        return this;
    }

    /**
     * Sets expandX to 1.
     */
    public Cell<T> expandX() {
        expandX = 1;
        return this;
    }

    /**
     * Sets expandY to 1.
     */
    public Cell<T> expandY() {
        expandY = 1;
        return this;
    }

    public Cell<T> expand(Integer x, Integer y) {
        expandX = x;
        expandY = y;
        return this;
    }

    /**
     * Sets expandX and expandY to 1 if true, 0 if false.
     */
    public Cell<T> expand(boolean x, boolean y) {
        expandX = x ? 1 : 0;
        expandY = y ? 1 : 0;
        return this;
    }

    public Cell<T> colspan(Integer colspan) {
        this.colspan = colspan;
        return this;
    }

    /**
     * Sets uniformX and uniformY to true.
     */
    public Cell<T> uniform() {
        uniformX = true;
        uniformY = true;
        return this;
    }

    /**
     * Sets uniformX to true.
     */
    public Cell<T> uniformX() {
        uniformX = true;
        return this;
    }

    /**
     * Sets uniformY to true.
     */
    public Cell<T> uniformY() {
        uniformY = true;
        return this;
    }

    public Cell<T> uniform(Boolean x, Boolean y) {
        uniformX = x;
        uniformY = y;
        return this;
    }

    public void setActorBounds(float x, float y, float width, float height) {
        actorX = x;
        actorY = y;
        actorWidth = width;
        actorHeight = height;
    }

    public float getActorX() {
        return actorX;
    }

    public void setActorX(float actorX) {
        this.actorX = actorX;
    }

    public float getActorY() {
        return actorY;
    }

    public void setActorY(float actorY) {
        this.actorY = actorY;
    }

    public float getActorWidth() {
        return actorWidth;
    }

    public void setActorWidth(float actorWidth) {
        this.actorWidth = actorWidth;
    }

    public float getActorHeight() {
        return actorHeight;
    }

    public void setActorHeight(float actorHeight) {
        this.actorHeight = actorHeight;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    /**
     * @return May be null if this cell is row defaults.
     */
    public Value getMinWidthValue() {
        return minWidth;
    }

    public float getMinWidth() {
        return minWidth.get(actor);
    }

    /**
     * @return May be null if this cell is row defaults.
     */
    public Value getMinHeightValue() {
        return minHeight;
    }

    public float getMinHeight() {
        return minHeight.get(actor);
    }

    /**
     * @return May be null if this cell is row defaults.
     */
    public Value getPrefWidthValue() {
        return prefWidth;
    }

    public float getPrefWidth() {
        return prefWidth.get(actor);
    }

    /**
     * @return May be null if this cell is row defaults.
     */
    public Value getPrefHeightValue() {
        return prefHeight;
    }

    public float getPrefHeight() {
        return prefHeight.get(actor);
    }

    /**
     * @return May be null if this cell is row defaults.
     */
    public Value getMaxWidthValue() {
        return maxWidth;
    }

    public float getMaxWidth() {
        return maxWidth.get(actor);
    }

    /**
     * @return May be null if this cell is row defaults.
     */
    public Value getMaxHeightValue() {
        return maxHeight;
    }

    public float getMaxHeight() {
        return maxHeight.get(actor);
    }

    /**
     * @return May be null if this value is not set.
     */
    public Value getSpaceTopValue() {
        return spaceTop;
    }

    public float getSpaceTop() {
        return spaceTop.get(actor);
    }

    /**
     * @return May be null if this value is not set.
     */
    public Value getSpaceLeftValue() {
        return spaceLeft;
    }

    public float getSpaceLeft() {
        return spaceLeft.get(actor);
    }

    /**
     * @return May be null if this value is not set.
     */
    public Value getSpaceBottomValue() {
        return spaceBottom;
    }

    public float getSpaceBottom() {
        return spaceBottom.get(actor);
    }

    /**
     * @return May be null if this value is not set.
     */
    public Value getSpaceRightValue() {
        return spaceRight;
    }

    public float getSpaceRight() {
        return spaceRight.get(actor);
    }

    /**
     * @return May be null if this value is not set.
     */
    public Value getPadTopValue() {
        return padTop;
    }

    public float getPadTop() {
        return padTop.get(actor);
    }

    /**
     * @return May be null if this value is not set.
     */
    public Value getPadLeftValue() {
        return padLeft;
    }

    public float getPadLeft() {
        return padLeft.get(actor);
    }

    /**
     * @return May be null if this value is not set.
     */
    public Value getPadBottomValue() {
        return padBottom;
    }

    public float getPadBottom() {
        return padBottom.get(actor);
    }

    /**
     * @return May be null if this value is not set.
     */
    public Value getPadRightValue() {
        return padRight;
    }

    public float getPadRight() {
        return padRight.get(actor);
    }

    /**
     * Returns {@link #getPadLeft()} plus {@link #getPadRight()}.
     */
    public float getPadX() {
        return padLeft.get(actor) + padRight.get(actor);
    }

    /**
     * Returns {@link #getPadTop()} plus {@link #getPadBottom()}.
     */
    public float getPadY() {
        return padTop.get(actor) + padBottom.get(actor);
    }

    /**
     * @return May be null if this value is not set.
     */
    public Float getFillX() {
        return fillX;
    }

    /**
     * @return May be null.
     */
    public Float getFillY() {
        return fillY;
    }

    /**
     * @return May be null.
     */
    public Integer getAlign() {
        return align;
    }

    /**
     * @return May be null.
     */
    public Integer getExpandX() {
        return expandX;
    }

    /**
     * @return May be null.
     */
    public Integer getExpandY() {
        return expandY;
    }

    /**
     * @return May be null.
     */
    public Integer getColspan() {
        return colspan;
    }

    /**
     * @return May be null.
     */
    public Boolean getUniformX() {
        return uniformX;
    }

    /**
     * @return May be null.
     */
    public Boolean getUniformY() {
        return uniformY;
    }

    /**
     * Returns true if this cell is the last cell in the row.
     */
    public boolean isEndRow() {
        return endRow;
    }

    /**
     * The actual amount of combined padding and spacing from the last layout.
     */
    public float getComputedPadTop() {
        return computedPadTop;
    }

    /**
     * The actual amount of combined padding and spacing from the last layout.
     */
    public float getComputedPadLeft() {
        return computedPadLeft;
    }

    /**
     * The actual amount of combined padding and spacing from the last layout.
     */
    public float getComputedPadBottom() {
        return computedPadBottom;
    }

    /**
     * The actual amount of combined padding and spacing from the last layout.
     */
    public float getComputedPadRight() {
        return computedPadRight;
    }

    public Cell<T> row() {
        return table.row();
    }

    public Table getTable() {
        return table;
    }

    /**
     * Sets all constraint fields to null.
     */
    public void clear() {
        minWidth = null;
        minHeight = null;
        prefWidth = null;
        prefHeight = null;
        maxWidth = null;
        maxHeight = null;
        spaceTop = null;
        spaceLeft = null;
        spaceBottom = null;
        spaceRight = null;
        padTop = null;
        padLeft = null;
        padBottom = null;
        padRight = null;
        fillX = null;
        fillY = null;
        align = null;
        expandX = null;
        expandY = null;
        colspan = null;
        uniformX = null;
        uniformY = null;
    }

    /**
     * Reset state so the cell can be reused. Doesn't reset the constraint fields.
     */
    public void reset() {
        actor = null;
        table = null;
        endRow = false;
        cellAboveIndex = -1;
    }

    /**
     * Set all constraints to cell default values.
     */
    void defaults() {
        minWidth = Value.minWidth;
        minHeight = Value.minHeight;
        prefWidth = Value.prefWidth;
        prefHeight = Value.prefHeight;
        maxWidth = Value.maxWidth;
        maxHeight = Value.maxHeight;
        spaceTop = Value.zero;
        spaceLeft = Value.zero;
        spaceBottom = Value.zero;
        spaceRight = Value.zero;
        padTop = Value.zero;
        padLeft = Value.zero;
        padBottom = Value.zero;
        padRight = Value.zero;
        fillX = 0f;
        fillY = 0f;
        align = Align.center;
        expandX = 0;
        expandY = 0;
        colspan = 1;
        uniformX = null;
        uniformY = null;
    }
}
