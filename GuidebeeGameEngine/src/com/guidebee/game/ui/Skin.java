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

import com.guidebee.game.GameEngine;
import com.guidebee.game.GameEngineRuntimeException;
import com.guidebee.game.engine.scene.Actor;
import com.guidebee.game.engine.utils.reflect.ClassReflection;
import com.guidebee.game.engine.utils.reflect.Method;
import com.guidebee.game.engine.utils.reflect.ReflectionException;
import com.guidebee.game.files.FileHandle;
import com.guidebee.game.graphics.*;
import com.guidebee.game.graphics.TextureAtlas.AtlasRegion;
import com.guidebee.game.ui.drawable.*;
import com.guidebee.utils.Disposable;
import com.guidebee.utils.Json;
import com.guidebee.utils.JsonValue;
import com.guidebee.utils.SerializationException;
import com.guidebee.utils.collections.Array;
import com.guidebee.utils.collections.ObjectMap;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * A skin stores resources for UI widgets to use (texture regions, ninepatches,
 * fonts, colors, etc). Resources are named and can
 * be looked up by name and type. Resources can be described in JSON. Skin provides
 * useful conversions, such as allowing access to
 * regions in the atlas as ninepatches, sprites, drawables, etc. The get* methods
 * return an instance of the object in the skin.
 * The new* methods return a copy of an instance in the skin.
 * <p/>
 * See the <a href="https://code.google.com/p/libgameengine/wiki/Skin">documentation</a>
 * for more.
 *
 * @author Nathan Sweet
 */
public class Skin implements Disposable {
    ObjectMap<Class, ObjectMap<String, Object>> resources = new ObjectMap();
    TextureAtlas atlas;

    /**
     * Creates an empty skin.
     */
    public Skin() {
    }

    /**
     * Creates a skin containing the resources in the specified skin JSON file.
     * If a file in the same directory with a ".atlas"
     * extension exists, it is loaded as a {@link TextureAtlas} and the texture
     * regions added to the skin. The atlas is
     * automatically disposed when the skin is disposed.
     */
    public Skin(FileHandle skinFile) {
        FileHandle atlasFile
                = skinFile.sibling(skinFile.nameWithoutExtension() + ".atlas");
        if (atlasFile.exists()) {
            atlas = new TextureAtlas(atlasFile);
            addRegions(atlas);
        }

        load(skinFile);
    }

    /**
     * Creates a skin containing the resources in the specified skin JSON file and
     * the texture regions from the specified atlas.
     * The atlas is automatically disposed when the skin is disposed.
     */
    public Skin(FileHandle skinFile, TextureAtlas atlas) {
        this.atlas = atlas;
        addRegions(atlas);
        load(skinFile);
    }

    /**
     * Creates a skin containing the texture regions from the specified atlas.
     * The atlas is automatically disposed when the skin is
     * disposed.
     */
    public Skin(TextureAtlas atlas) {
        this.atlas = atlas;
        addRegions(atlas);
    }

    /**
     * Adds all resources in the specified skin JSON file.
     */
    public void load(FileHandle skinFile) {
        try {
            getJsonLoader(skinFile).fromJson(Skin.class, skinFile);
        } catch (SerializationException ex) {
            throw new SerializationException("Error reading file: "
                    + skinFile, ex);
        }
    }

    /**
     * Adds all named texture regions from the atlas. The atlas will not
     * be automatically disposed when the skin is disposed.
     */
    public void addRegions(TextureAtlas atlas) {
        Array<AtlasRegion> regions = atlas.getRegions();
        for (int i = 0, n = regions.size; i < n; i++) {
            TextureAtlas.AtlasRegion region = regions.get(i);
            add(region.name, region, TextureRegion.class);
        }
    }

    public void add(String name, Object resource) {
        add(name, resource, resource.getClass());
    }

    public void add(String name, Object resource, Class type) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null.");
        if (resource == null)
            throw new IllegalArgumentException("resource cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(type);
        if (typeResources == null) {
            typeResources = new ObjectMap();
            resources.put(type, typeResources);
        }
        typeResources.put(name, resource);
    }

    public void remove(String name, Class type) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(type);
        typeResources.remove(name);
    }


    public <T> T get(Class<T> type) {
        return get("default", type);
    }

    public <T> T get(String name, Class<T> type) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null.");
        if (type == null)
            throw new IllegalArgumentException("type cannot be null.");

        if (type == Drawable.class) return (T) getDrawable(name);
        if (type == TextureRegion.class) return (T) getRegion(name);
        if (type == NinePatch.class) return (T) getPatch(name);
        if (type == Sprite.class) return (T) getSprite(name);

        ObjectMap<String, Object> typeResources = resources.get(type);
        if (typeResources == null)
            throw new GameEngineRuntimeException("No "
                    + type.getName() + " registered with name: " + name);
        Object resource = typeResources.get(name);
        if (resource == null)
            throw new GameEngineRuntimeException("No "
                    + type.getName() + " registered with name: " + name);
        return (T) resource;
    }

    public <T> T optional(String name, Class<T> type) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null.");
        if (type == null)
            throw new IllegalArgumentException("type cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(type);
        if (typeResources == null) return null;
        return (T) typeResources.get(name);
    }

    public boolean has(String name, Class type) {
        ObjectMap<String, Object> typeResources = resources.get(type);
        if (typeResources == null) return false;
        return typeResources.containsKey(name);
    }

    /**
     * Returns the name to resource mapping for the specified type, or null
     * if no resources of that type exist.
     */
    public <T> ObjectMap<String, T> getAll(Class<T> type) {
        return (ObjectMap<String, T>) resources.get(type);
    }

    public Color getColor(String name) {
        return get(name, Color.class);
    }

    public BitmapFont getFont(String name) {
        return get(name, BitmapFont.class);
    }

    /**
     * Returns a registered texture region. If no region is found but a texture
     * exists with the name, a region is created from the
     * texture and stored in the skin.
     */
    public TextureRegion getRegion(String name) {
        TextureRegion region = optional(name, TextureRegion.class);
        if (region != null) return region;

        Texture texture = optional(name, Texture.class);
        if (texture == null)
            throw new GameEngineRuntimeException("No TextureRegion or Texture" +
                    " registered with name: " + name);
        region = new TextureRegion(texture);
        add(name, region, TextureRegion.class);
        return region;
    }

    /**
     * Returns a registered tiled drawable. If no tiled drawable is found but a
     * region exists with the name, a tiled drawable is
     * created from the region and stored in the skin.
     */
    public TiledDrawable getTiledDrawable(String name) {
        TiledDrawable tiled = optional(name, TiledDrawable.class);
        if (tiled != null) return tiled;

        Drawable drawable = optional(name, Drawable.class);
        if (drawable != null) {
            if (!(drawable instanceof TiledDrawable)) {
                throw new GameEngineRuntimeException("Drawable found but is not a TiledDrawable: "
                        + name + ", "
                        + drawable.getClass().getName());
            }
            return (TiledDrawable) drawable;
        }

        tiled = new TiledDrawable(getRegion(name));
        add(name, tiled, TiledDrawable.class);
        return tiled;
    }

    /**
     * Returns a registered ninepatch. If no ninepatch is found but a region exists
     * with the name, a ninepatch is created from the
     * region and stored in the skin. If the region is an
     * {@link com.guidebee.game.graphics.TextureAtlas.AtlasRegion} then
     * the {@link com.guidebee.game.graphics.TextureAtlas.AtlasRegion#splits} are used,
     * otherwise the ninepatch will have the region as the center patch.
     */
    public NinePatch getPatch(String name) {
        NinePatch patch = optional(name, NinePatch.class);
        if (patch != null) return patch;

        try {
            TextureRegion region = getRegion(name);
            if (region instanceof TextureAtlas.AtlasRegion) {
                int[] splits = ((TextureAtlas.AtlasRegion) region).splits;
                if (splits != null) {
                    patch = new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
                    int[] pads = ((TextureAtlas.AtlasRegion) region).pads;
                    if (pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
                }
            }
            if (patch == null) patch = new NinePatch(region);
            add(name, patch, NinePatch.class);
            return patch;
        } catch (GameEngineRuntimeException ex) {
            throw new GameEngineRuntimeException("No NinePatch, TextureRegion, or " +
                    "Texture registered with name: " + name);
        }
    }

    /**
     * Returns a registered sprite. If no sprite is found but a region exists with
     * the name, a sprite is created from the region
     * and stored in the skin. If the region is an
     * {@link com.guidebee.game.graphics.TextureAtlas.AtlasRegion} then an
     * {@link com.guidebee.game.graphics.TextureAtlas.AtlasSprite} is used
     * if the region has been
     * whitespace stripped or packed rotated 90 degrees.
     */
    public Sprite getSprite(String name) {
        Sprite sprite = optional(name, Sprite.class);
        if (sprite != null) return sprite;

        try {
            TextureRegion textureRegion = getRegion(name);
            if (textureRegion instanceof TextureAtlas.AtlasRegion) {
                TextureAtlas.AtlasRegion region = (TextureAtlas.AtlasRegion) textureRegion;
                if (region.rotate || region.packedWidth != region.originalWidth
                        || region.packedHeight != region.originalHeight)
                    sprite = new TextureAtlas.AtlasSprite(region);
            }
            if (sprite == null) sprite = new Sprite(textureRegion);
            add(name, sprite, NinePatch.class);
            return sprite;
        } catch (GameEngineRuntimeException ex) {
            throw new GameEngineRuntimeException("No NinePatch, TextureRegion, " +
                    "or Texture registered with name: " + name);
        }
    }

    /**
     * Returns a registered drawable. If no drawable is found but a region, ninepatch,
     * or sprite exists with the name, then the
     * appropriate drawable is created and stored in the skin.
     */
    public Drawable getDrawable(String name) {
        Drawable drawable = optional(name, Drawable.class);
        if (drawable != null) return drawable;

        drawable = optional(name, TiledDrawable.class);
        if (drawable != null) return drawable;

        // Use texture or texture region. If it has splits, use ninepatch.
        // If it has rotation or whitespace stripping, use sprite.
        try {
            TextureRegion textureRegion = getRegion(name);
            if (textureRegion instanceof TextureAtlas.AtlasRegion) {
                TextureAtlas.AtlasRegion region = (TextureAtlas.AtlasRegion) textureRegion;
                if (region.splits != null)
                    drawable = new NinePatchDrawable(getPatch(name));
                else if (region.rotate || region.packedWidth != region.originalWidth
                        || region.packedHeight != region.originalHeight)
                    drawable = new SpriteDrawable(getSprite(name));
            }
            if (drawable == null) drawable = new TextureRegionDrawable(textureRegion);
        } catch (GameEngineRuntimeException ignored) {
        }

        // Check for explicit registration of ninepatch, sprite, or tiled drawable.
        if (drawable == null) {
            NinePatch patch = optional(name, NinePatch.class);
            if (patch != null)
                drawable = new NinePatchDrawable(patch);
            else {
                Sprite sprite = optional(name, Sprite.class);
                if (sprite != null)
                    drawable = new SpriteDrawable(sprite);
                else
                    throw new GameEngineRuntimeException("No Drawable, NinePatch, " +
                            "TextureRegion, Texture, or Sprite registered with name: "
                            + name);
            }
        }

        add(name, drawable, Drawable.class);
        return drawable;
    }

    /**
     * Returns the name of the specified style object, or null if it is not in the
     * skin. This compares potentially every style
     * object in the skin of the same type as the specified style, which may be a
     * somewhat expensive operation.
     */
    public String find(Object resource) {
        if (resource == null)
            throw new IllegalArgumentException("style cannot be null.");
        ObjectMap<String, Object> typeResources = resources.get(resource.getClass());
        if (typeResources == null) return null;
        return typeResources.findKey(resource, true);
    }

    /**
     * Returns a copy of a drawable found in the skin via {@link #getDrawable(String)}.
     */
    public Drawable newDrawable(String name) {
        return newDrawable(getDrawable(name));
    }

    /**
     * Returns a tinted copy of a drawable found in the skin via {@link #getDrawable(String)}.
     */
    public Drawable newDrawable(String name, float r, float g, float b, float a) {
        return newDrawable(getDrawable(name), new Color(r, g, b, a));
    }

    /**
     * Returns a tinted copy of a drawable found in the skin via {@link #getDrawable(String)}.
     */
    public Drawable newDrawable(String name, Color tint) {
        return newDrawable(getDrawable(name), tint);
    }

    /**
     * Returns a copy of the specified drawable.
     */
    public Drawable newDrawable(Drawable drawable) {
        if (drawable instanceof TextureRegionDrawable)
            return new TextureRegionDrawable((TextureRegionDrawable) drawable);
        if (drawable instanceof NinePatchDrawable)
            return new NinePatchDrawable((NinePatchDrawable) drawable);
        if (drawable instanceof SpriteDrawable)
            return new SpriteDrawable((SpriteDrawable) drawable);
        throw new GameEngineRuntimeException("Unable to copy, unknown drawable type: "
                + drawable.getClass());
    }

    /**
     * Returns a tinted copy of a drawable found in the skin via {@link #getDrawable(String)}.
     */
    public Drawable newDrawable(Drawable drawable, float r, float g, float b, float a) {
        return newDrawable(drawable, new Color(r, g, b, a));
    }

    /**
     * Returns a tinted copy of a drawable found in the skin via {@link #getDrawable(String)}.
     */
    public Drawable newDrawable(Drawable drawable, Color tint) {
        if (drawable instanceof TextureRegionDrawable) {
            TextureRegion region = ((TextureRegionDrawable) drawable).getRegion();
            Sprite sprite;
            if (region instanceof TextureAtlas.AtlasRegion)
                sprite = new TextureAtlas.AtlasSprite((TextureAtlas.AtlasRegion) region);
            else
                sprite = new Sprite(region);
            sprite.setColor(tint);
            return new SpriteDrawable(sprite);
        }
        if (drawable instanceof NinePatchDrawable) {
            NinePatchDrawable patchDrawable = new NinePatchDrawable((NinePatchDrawable) drawable);
            patchDrawable.setPatch(new NinePatch(patchDrawable.getPatch(), tint));
            return patchDrawable;
        }
        if (drawable instanceof SpriteDrawable) {
            SpriteDrawable spriteDrawable = new SpriteDrawable((SpriteDrawable) drawable);
            Sprite sprite = spriteDrawable.getSprite();
            if (sprite instanceof TextureAtlas.AtlasSprite)
                sprite = new TextureAtlas.AtlasSprite((TextureAtlas.AtlasSprite) sprite);
            else
                sprite = new Sprite(sprite);
            sprite.setColor(tint);
            spriteDrawable.setSprite(sprite);
            return spriteDrawable;
        }
        throw new GameEngineRuntimeException("Unable to copy, unknown drawable type: "
                + drawable.getClass());
    }

    /**
     * Sets the style on the actor to disabled or enabled. This is done by appending
     * "-disabled" to the style name when enabled is
     * false, and removing "-disabled" from the style name when enabled is true.
     * A method named "getStyle" is called the actor via
     * reflection and the name of that style is found in the skin. If the actor
     * doesn't have a "getStyle" method or the style was
     * not found in the skin, no exception is thrown and the actor is left unchanged.
     */
    public void setEnabled(Actor actor, boolean enabled) {
        // Get current style.
        Method method = findMethod(actor.getClass(), "getStyle");
        if (method == null) return;
        Object style;
        try {
            style = method.invoke(actor);
        } catch (Exception ignored) {
            return;
        }
        // Determine new style.
        String name = find(style);
        if (name == null) return;
        name = name.replace("-disabled", "") + (enabled ? "" : "-disabled");
        style = get(name, style.getClass());
        // Set new style.
        method = findMethod(actor.getClass(), "setStyle");
        if (method == null) return;
        try {
            method.invoke(actor, style);
        } catch (Exception ignored) {
        }
    }

    /**
     * Returns the {@link TextureAtlas} that resources in this skin reference, or null.
     */
    public TextureAtlas getAtlas() {
        return atlas;
    }

    /**
     * Disposes the {@link TextureAtlas} and all {@link Disposable} resources in the skin.
     */
    public void dispose() {
        if (atlas != null) atlas.dispose();
        for (ObjectMap<String, Object> entry : resources.values()) {
            for (Object resource : entry.values())
                if (resource instanceof Disposable) ((Disposable) resource).dispose();
        }
    }

    protected Json getJsonLoader(final FileHandle skinFile) {
        final Skin skin = this;

        final Json json = new Json() {
            public <T> T readValue(Class<T> type, Class elementType, JsonValue jsonData) {
                // If the JSON is a string but the type is not, look up the actual value by name.
                if (jsonData.isString()
                        && !ClassReflection.isAssignableFrom(CharSequence.class, type))
                    return get(jsonData.asString(), type);
                return super.readValue(type, elementType, jsonData);
            }
        };
        json.setTypeName(null);
        json.setUsePrototypes(false);

        json.setSerializer(Skin.class, new Json.ReadOnlySerializer<Skin>() {
            public Skin read(Json json, JsonValue typeToValueMap, Class ignored) {
                for (JsonValue valueMap = typeToValueMap.child;
                     valueMap != null; valueMap = valueMap.next) {
                    try {
                        readNamedObjects(json, ClassReflection.forName(valueMap.name()), valueMap);
                    } catch (ReflectionException ex) {
                        throw new SerializationException(ex);
                    }
                }
                return skin;
            }

            private void readNamedObjects(Json json, Class type, JsonValue valueMap) {
                Class addType = type == TintedDrawable.class ? Drawable.class : type;
                for (JsonValue valueEntry = valueMap.child; valueEntry != null;
                     valueEntry = valueEntry.next) {
                    Object object = json.readValue(type, valueEntry);
                    if (object == null) continue;
                    try {
                        add(valueEntry.name(), object, addType);
                    } catch (Exception ex) {
                        throw new SerializationException("Error reading "
                                + ClassReflection.getSimpleName(type) + ": "
                                + valueEntry.name(), ex);
                    }
                }
            }
        });

        json.setSerializer(BitmapFont.class, new Json.ReadOnlySerializer<BitmapFont>() {
            public BitmapFont read(Json json, JsonValue jsonData, Class type) {
                String path = json.readValue("file", String.class, jsonData);
                int scaledSize = json.readValue("scaledSize", int.class, -1, jsonData);
                Boolean flip = json.readValue("flip", Boolean.class, false, jsonData);

                FileHandle fontFile = skinFile.parent().child(path);
                if (!fontFile.exists()) fontFile = GameEngine.files.internal(path);
                if (!fontFile.exists())
                    throw new SerializationException("Font file not found: " + fontFile);

                // Use a region with the same name as the font, else use a PNG
                // file in the same directory as the FNT file.
                String regionName = fontFile.nameWithoutExtension();
                try {
                    BitmapFont font;
                    TextureRegion region = skin.optional(regionName, TextureRegion.class);
                    if (region != null)
                        font = new BitmapFont(fontFile, region, flip);
                    else {
                        FileHandle imageFile = fontFile.parent().child(regionName + ".png");
                        if (imageFile.exists())
                            font = new BitmapFont(fontFile, imageFile, flip);
                        else
                            font = new BitmapFont(fontFile, flip);
                    }
                    // Scaled size is the desired cap height to scale the font to.
                    if (scaledSize != -1) font.setScale(scaledSize / font.getCapHeight());
                    return font;
                } catch (RuntimeException ex) {
                    throw new SerializationException("Error loading bitmap font: " + fontFile, ex);
                }
            }
        });

        json.setSerializer(Color.class, new Json.ReadOnlySerializer<Color>() {
            public Color read(Json json, JsonValue jsonData, Class type) {
                if (jsonData.isString()) return get(jsonData.asString(), Color.class);
                String hex = json.readValue("hex", String.class, (String) null, jsonData);
                if (hex != null) return Color.valueOf(hex);
                float r = json.readValue("r", float.class, 0f, jsonData);
                float g = json.readValue("g", float.class, 0f, jsonData);
                float b = json.readValue("b", float.class, 0f, jsonData);
                float a = json.readValue("a", float.class, 1f, jsonData);
                return new Color(r, g, b, a);
            }
        });

        json.setSerializer(TintedDrawable.class, new Json.ReadOnlySerializer() {
            public Object read(Json json, JsonValue jsonData, Class type) {
                String name = json.readValue("name", String.class, jsonData);
                Color color = json.readValue("color", Color.class, jsonData);
                return newDrawable(name, color);
            }
        });

        return json;
    }

    static private Method findMethod(Class type, String name) {
        Method[] methods = ClassReflection.getMethods(type);
        for (int i = 0, n = methods.length; i < n; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) return method;
        }
        return null;
    }

    /**
     * Tinted Drawable.
     * @author Nathan Sweet
     */
    static public class TintedDrawable {
        public String name;
        public Color color;
    }
}
