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
import com.guidebee.game.files.FileHandle;
import com.guidebee.game.graphics.ParticleEffect;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.utils.collections.Array;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * {@link AssetLoader} to load
 * {@link com.guidebee.game.graphics.ParticleEffect} instances.
 * Passing a {@link ParticleEffectParameter} to
 * {@link com.guidebee.game.engine.assets.AssetManager#load(String, Class,
 * com.guidebee.game.engine.assets.AssetLoaderParameters)} allows to specify an
 * atlas file or an image directory to be
 * used for the effect's images. Per default images are loaded from the directory
 * in which the effect file is found.
 */
public class ParticleEffectLoader extends SynchronousAssetLoader<ParticleEffect,
        ParticleEffectLoader.ParticleEffectParameter> {
    public ParticleEffectLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public ParticleEffect load(AssetManager am, String fileName, FileHandle file,
                               ParticleEffectParameter param) {
        ParticleEffect effect = new ParticleEffect();
        if (param != null && param.atlasFile != null)
            effect.load(file, am.get(param.atlasFile, TextureAtlas.class), param.atlasPrefix);
        else if (param != null && param.imagesDir != null)
            effect.load(file, param.imagesDir);
        else
            effect.load(file, file.parent());
        return effect;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file,
                                                  ParticleEffectParameter param) {
        Array<AssetDescriptor> deps = null;
        if (param != null && param.atlasFile != null) {
            deps = new Array();
            deps.add(new AssetDescriptor<TextureAtlas>(param.atlasFile, TextureAtlas.class));
        }
        return deps;
    }

    /**
     * Parameter to be passed to {@link AssetManager#load(String, Class,
     * com.guidebee.game.engine.assets.AssetLoaderParameters)} if additional configuration is
     * necessary for the {@link ParticleEffect}.
     */
    public static class ParticleEffectParameter extends AssetLoaderParameters<ParticleEffect> {
        /**
         * Atlas file name.
         */
        public String atlasFile;
        /**
         * Optional prefix to image names *
         */
        public String atlasPrefix;
        /**
         * Image directory.
         */
        public FileHandle imagesDir;
    }
}
