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
package com.guidebee.game.engine.graphics.opengles;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.GameEngineRuntimeException;
import com.guidebee.game.graphics.Pixmap;
import com.guidebee.game.graphics.TextureData;

//[------------------------------ MAIN CLASS ----------------------------------]
public class PixmapTextureData implements TextureData {
    final Pixmap pixmap;
    final Pixmap.Format format;
    final boolean useMipMaps;
    final boolean disposePixmap;
    final boolean managed;

    public PixmapTextureData(Pixmap pixmap, Pixmap.Format format,
                             boolean useMipMaps, boolean disposePixmap) {
        this(pixmap, format, useMipMaps, disposePixmap, false);
    }

    public PixmapTextureData(Pixmap pixmap, Pixmap.Format format,
                             boolean useMipMaps, boolean disposePixmap,
                             boolean managed) {
        this.pixmap = pixmap;
        this.format = format == null ? pixmap.getFormat() : format;
        this.useMipMaps = useMipMaps;
        this.disposePixmap = disposePixmap;
        this.managed = managed;
    }

    @Override
    public boolean disposePixmap() {
        return disposePixmap;
    }

    @Override
    public Pixmap consumePixmap() {
        return pixmap;
    }

    @Override
    public int getWidth() {
        return pixmap.getWidth();
    }

    @Override
    public int getHeight() {
        return pixmap.getHeight();
    }

    @Override
    public Pixmap.Format getFormat() {
        return format;
    }

    @Override
    public boolean useMipMaps() {
        return useMipMaps;
    }

    @Override
    public boolean isManaged() {
        return managed;
    }

    @Override
    public TextureDataType getType() {
        return TextureDataType.Pixmap;
    }

    @Override
    public void consumeCustomData(int target) {
        throw new GameEngineRuntimeException(
                "This TextureData implementation does not upload data itself");
    }

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void prepare() {
        throw new GameEngineRuntimeException(
                "prepare() must not be called on a PixmapTextureData instance as it" +
                        " is already prepared.");
    }
}
