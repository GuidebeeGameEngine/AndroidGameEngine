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
package com.guidebee.game.engine.assets.loaders;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.engine.assets.AssetDescriptor;
import com.guidebee.game.engine.assets.AssetLoaderParameters;
import com.guidebee.game.engine.assets.AssetManager;
import com.guidebee.game.engine.graphics.opengles.ETC1TextureData;
import com.guidebee.game.engine.graphics.opengles.FileTextureData;
import com.guidebee.game.files.FileHandle;
import com.guidebee.game.graphics.Pixmap;
import com.guidebee.game.graphics.Pixmap.Format;
import com.guidebee.game.graphics.Texture;
import com.guidebee.game.graphics.Texture.TextureFilter;
import com.guidebee.game.graphics.Texture.TextureWrap;
import com.guidebee.game.graphics.TextureData;
import com.guidebee.utils.collections.Array;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * {@link AssetLoader} for {@link Texture} instances. The pixel data is loaded
 * asynchronously. The texture is then created on the
 * rendering thread, synchronously. Passing a {@link TextureParameter} to
 * {@link AssetManager#load(String, Class,
 * com.guidebee.game.engine.assets.AssetLoaderParameters)} allows one to specify
 * parameters as can be passed to the
 * various Texture constructors, e.g. filtering, whether to generate mipmaps
 * and so on.
 *
 * @author mzechner
 */
public class TextureLoader extends AsynchronousAssetLoader<Texture,
        TextureLoader.TextureParameter> {
    static public class TextureLoaderInfo {
        String filename;
        TextureData data;
        Texture texture;
    }

    ;

    TextureLoaderInfo info = new TextureLoaderInfo();

    public TextureLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName,
                          FileHandle file, TextureParameter parameter) {
        info.filename = fileName;
        if (parameter == null || parameter.textureData == null) {
            Pixmap pixmap = null;
            Format format = null;
            boolean genMipMaps = false;
            info.texture = null;

            if (parameter != null) {
                format = parameter.format;
                genMipMaps = parameter.genMipMaps;
                info.texture = parameter.texture;
            }

            if (!fileName.contains(".etc1")) {
                if (fileName.contains(".cim"))
                    pixmap = Pixmap.readCIM(file);
                else
                    pixmap = new Pixmap(file);
                info.data = new FileTextureData(file, pixmap, format, genMipMaps);
            } else {
                info.data = new ETC1TextureData(file, genMipMaps);
            }
        } else {
            info.data = parameter.textureData;
            info.texture = parameter.texture;
        }
        if (!info.data.isPrepared()) info.data.prepare();
    }

    @Override
    public Texture loadSync(AssetManager manager, String fileName,
                            FileHandle file, TextureParameter parameter) {
        if (info == null) return null;
        Texture texture = info.texture;
        if (texture != null) {
            texture.load(info.data);
        } else {
            texture = new Texture(info.data);
        }
        if (parameter != null) {
            texture.setFilter(parameter.minFilter, parameter.magFilter);
            texture.setWrap(parameter.wrapU, parameter.wrapV);
        }
        return texture;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName,
                                                  FileHandle file,
                                                  TextureParameter parameter) {
        return null;
    }

    static public class TextureParameter extends AssetLoaderParameters<Texture> {
        /**
         * the format of the final Texture. Uses the source images format if null *
         */
        public Format format = null;
        /**
         * whether to generate mipmaps *
         */
        public boolean genMipMaps = false;
        /**
         * The texture to put the {@link TextureData} in, optional. *
         */
        public Texture texture = null;
        /**
         * TextureData for textures created on the fly, optional. When set,
         * all format and genMipMaps are ignored
         */
        public TextureData textureData = null;
        public TextureFilter minFilter = TextureFilter.Nearest;
        public TextureFilter magFilter = TextureFilter.Nearest;
        public TextureWrap wrapU = TextureWrap.ClampToEdge;
        public TextureWrap wrapV = TextureWrap.ClampToEdge;
    }
}
