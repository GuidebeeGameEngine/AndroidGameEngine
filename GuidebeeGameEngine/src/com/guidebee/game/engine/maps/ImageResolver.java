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
package com.guidebee.game.engine.maps;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.engine.assets.AssetManager;
import com.guidebee.game.graphics.Texture;
import com.guidebee.game.graphics.TextureAtlas;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.utils.collections.ObjectMap;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Resolves an image by a string, wrapper around a Map or AssetManager to
 * load maps either directly or via AssetManager.
 *
 * @author mzechner
 */
public interface ImageResolver {
    /**
     * @param name
     * @return the Texture for the given image name or null.
     */
    public TextureRegion getImage(String name);

    /**
     * Direct Image Resolver.
     */
    public static class DirectImageResolver implements ImageResolver {
        private final ObjectMap<String, Texture> images;

        public DirectImageResolver(ObjectMap<String, Texture> images) {
            this.images = images;
        }

        @Override
        public TextureRegion getImage(String name) {
            return new TextureRegion(images.get(name));
        }
    }

    /**
     * AssetManager Image Resolver.
     */
    public static class AssetManagerImageResolver implements ImageResolver {
        private final AssetManager assetManager;

        public AssetManagerImageResolver(AssetManager assetManager) {
            this.assetManager = assetManager;
        }

        @Override
        public TextureRegion getImage(String name) {
            return new TextureRegion(assetManager.get(name, Texture.class));
        }
    }

    /**
     * TextureAtlas Image Resolver.
     */
    public static class TextureAtlasImageResolver implements ImageResolver {
        private final TextureAtlas atlas;

        public TextureAtlasImageResolver(TextureAtlas atlas) {
            this.atlas = atlas;
        }

        @Override
        public TextureRegion getImage(String name) {
            return atlas.findRegion(name);
        }
    }
}
