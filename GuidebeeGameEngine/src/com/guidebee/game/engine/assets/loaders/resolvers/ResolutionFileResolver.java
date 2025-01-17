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
package com.guidebee.game.engine.assets.loaders.resolvers;

//--------------------------------- IMPORTS ------------------------------------
import com.guidebee.game.GameEngine;
import com.guidebee.game.engine.assets.loaders.FileHandleResolver;
import com.guidebee.game.files.FileHandle;

//[------------------------------ MAIN CLASS ----------------------------------]
public class ResolutionFileResolver implements FileHandleResolver {
    public static class Resolution {
        public final int portraitWidth;
        public final int portraitHeight;
        public final String suffix;

        public Resolution(int portraitWidth, int portraitHeight, String suffix) {
            this.portraitWidth = portraitWidth;
            this.portraitHeight = portraitHeight;
            this.suffix = suffix;
        }
    }

    protected final FileHandleResolver baseResolver;
    protected final Resolution[] descriptors;

    public ResolutionFileResolver(FileHandleResolver baseResolver,
                                  Resolution... descriptors) {
        this.baseResolver = baseResolver;
        this.descriptors = descriptors;
    }

    @Override
    public FileHandle resolve(String fileName) {
        Resolution bestDesc = choose(descriptors);
        FileHandle originalHandle = new FileHandle(fileName);
        FileHandle handle = baseResolver.resolve(resolve(originalHandle,
                bestDesc.suffix));
        if (!handle.exists()) handle = baseResolver.resolve(fileName);
        return handle;
    }

    protected String resolve(FileHandle originalHandle, String suffix) {
        String parentString = "";
        FileHandle parent = originalHandle.parent();
        if (parent != null && !parent.name().equals("")) {
            parentString = parent + "/";
        }
        return parentString + suffix + "/" + originalHandle.name();
    }

    static public Resolution choose(Resolution... descriptors) {
        if (descriptors == null)
            throw new IllegalArgumentException("descriptors cannot be null.");
        int w = GameEngine.graphics.getWidth(),
                h = GameEngine.graphics.getHeight();

        // Prefer the shortest side.
        Resolution best = descriptors[0];
        if (w < h) {
            for (int i = 0, n = descriptors.length; i < n; i++) {
                Resolution other = descriptors[i];
                if (w >= other.portraitWidth && other.portraitWidth >= best.portraitWidth
                        && h >= other.portraitHeight
                        && other.portraitHeight >= best.portraitHeight)
                    best = descriptors[i];
            }
        } else {
            for (int i = 0, n = descriptors.length; i < n; i++) {
                Resolution other = descriptors[i];
                if (w >= other.portraitHeight && other.portraitHeight >= best.portraitHeight
                        && h >= other.portraitWidth
                        && other.portraitWidth >= best.portraitWidth)
                    best = descriptors[i];
            }
        }
        return best;
    }
}
