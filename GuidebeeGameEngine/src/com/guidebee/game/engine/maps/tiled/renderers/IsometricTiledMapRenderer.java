/*******************************************************************************
 * Copyright 2013 See AUTHORS file.
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
package com.guidebee.game.engine.maps.tiled.renderers;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.graphics.Batch;
import com.guidebee.game.graphics.Color;
import com.guidebee.game.graphics.TextureRegion;
import com.guidebee.game.maps.MapObject;
import com.guidebee.game.maps.tiled.TiledMap;
import com.guidebee.game.maps.tiled.TiledMapTile;
import com.guidebee.game.maps.tiled.TiledMapTileLayer;
import com.guidebee.game.maps.tiled.TiledMapTileLayer.Cell;
import com.guidebee.math.Matrix4;
import com.guidebee.math.Vector2;
import com.guidebee.math.Vector3;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Isometric Tiled Map Renderer.
 */
public class IsometricTiledMapRenderer extends BatchTiledMapRenderer {

    private Matrix4 isoTransform;
    private Matrix4 invIsotransform;
    private Vector3 screenPos = new Vector3();

    private Vector2 topRight = new Vector2();
    private Vector2 bottomLeft = new Vector2();
    private Vector2 topLeft = new Vector2();
    private Vector2 bottomRight = new Vector2();

    public IsometricTiledMapRenderer(TiledMap map) {
        super(map);
        init();
    }

    public IsometricTiledMapRenderer(TiledMap map, Batch batch) {
        super(map, batch);
        init();
    }

    public IsometricTiledMapRenderer(TiledMap map, float unitScale) {
        super(map, unitScale);
        init();
    }

    public IsometricTiledMapRenderer(TiledMap map, float unitScale, Batch batch) {
        super(map, unitScale, batch);
        init();
    }

    private void init() {
        // create the isometric transform
        isoTransform = new Matrix4();
        isoTransform.idt();

        // isoTransform.translate(0, 32, 0);
        isoTransform.scale((float) (Math.sqrt(2.0) / 2.0),
                (float) (Math.sqrt(2.0) / 4.0), 1.0f);
        isoTransform.rotate(0.0f, 0.0f, 1.0f, -45);

        // ... and the inverse matrix
        invIsotransform = new Matrix4(isoTransform);
        invIsotransform.inv();
    }

    @Override
    public void renderObject(MapObject object) {

    }

    private Vector3 translateScreenToIso(Vector2 vec) {
        screenPos.set(vec.x, vec.y, 0);
        screenPos.mul(invIsotransform);

        return screenPos;
    }

    @Override
    public void renderTileLayer(TiledMapTileLayer layer) {
        final Color batchColor = spriteBatch.getColor();
        final float color = Color.toFloatBits(batchColor.r, batchColor.g,
                batchColor.b, batchColor.a * layer.getOpacity());

        float tileWidth = layer.getTileWidth() * unitScale;
        float tileHeight = layer.getTileHeight() * unitScale;
        float halfTileWidth = tileWidth * 0.5f;
        float halfTileHeight = tileHeight * 0.5f;

        // setting up the screen points
        // COL1
        topRight.set(viewBounds.x + viewBounds.width, viewBounds.y);
        // COL2
        bottomLeft.set(viewBounds.x, viewBounds.y + viewBounds.height);
        // ROW1
        topLeft.set(viewBounds.x, viewBounds.y);
        // ROW2
        bottomRight.set(viewBounds.x + viewBounds.width, viewBounds.y + viewBounds.height);

        // transforming screen coordinates to iso coordinates
        int row1 = (int) (translateScreenToIso(topLeft).y / tileWidth) - 2;
        int row2 = (int) (translateScreenToIso(bottomRight).y / tileWidth) + 2;

        int col1 = (int) (translateScreenToIso(bottomLeft).x / tileWidth) - 2;
        int col2 = (int) (translateScreenToIso(topRight).x / tileWidth) + 2;

        for (int row = row2; row >= row1; row--) {
            for (int col = col1; col <= col2; col++) {
                float x = (col * halfTileWidth) + (row * halfTileWidth);
                float y = (row * halfTileHeight) - (col * halfTileHeight);

                final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
                if (cell == null) continue;
                final TiledMapTile tile = cell.getTile();

                if (tile != null) {
                    final boolean flipX = cell.getFlipHorizontally();
                    final boolean flipY = cell.getFlipVertically();
                    final int rotations = cell.getRotation();

                    TextureRegion region = tile.getTextureRegion();

                    float x1 = x + tile.getOffsetX() * unitScale;
                    float y1 = y + tile.getOffsetY() * unitScale;
                    float x2 = x1 + region.getRegionWidth() * unitScale;
                    float y2 = y1 + region.getRegionHeight() * unitScale;

                    float u1 = region.getU();
                    float v1 = region.getV2();
                    float u2 = region.getU2();
                    float v2 = region.getV();

                    vertices[Batch.X1] = x1;
                    vertices[Batch.Y1] = y1;
                    vertices[Batch.C1] = color;
                    vertices[Batch.U1] = u1;
                    vertices[Batch.V1] = v1;

                    vertices[Batch.X2] = x1;
                    vertices[Batch.Y2] = y2;
                    vertices[Batch.C2] = color;
                    vertices[Batch.U2] = u1;
                    vertices[Batch.V2] = v2;

                    vertices[Batch.X3] = x2;
                    vertices[Batch.Y3] = y2;
                    vertices[Batch.C3] = color;
                    vertices[Batch.U3] = u2;
                    vertices[Batch.V3] = v2;

                    vertices[Batch.X4] = x2;
                    vertices[Batch.Y4] = y1;
                    vertices[Batch.C4] = color;
                    vertices[Batch.U4] = u2;
                    vertices[Batch.V4] = v1;

                    if (flipX) {
                        float temp = vertices[Batch.U1];
                        vertices[Batch.U1] = vertices[Batch.U3];
                        vertices[Batch.U3] = temp;
                        temp = vertices[Batch.U2];
                        vertices[Batch.U2] = vertices[Batch.U4];
                        vertices[Batch.U4] = temp;
                    }
                    if (flipY) {
                        float temp = vertices[Batch.V1];
                        vertices[Batch.V1] = vertices[Batch.V3];
                        vertices[Batch.V3] = temp;
                        temp = vertices[Batch.V2];
                        vertices[Batch.V2] = vertices[Batch.V4];
                        vertices[Batch.V4] = temp;
                    }
                    if (rotations != 0) {
                        switch (rotations) {
                            case Cell.ROTATE_90: {
                                float tempV = vertices[Batch.V1];
                                vertices[Batch.V1] = vertices[Batch.V2];
                                vertices[Batch.V2] = vertices[Batch.V3];
                                vertices[Batch.V3] = vertices[Batch.V4];
                                vertices[Batch.V4] = tempV;

                                float tempU = vertices[Batch.U1];
                                vertices[Batch.U1] = vertices[Batch.U2];
                                vertices[Batch.U2] = vertices[Batch.U3];
                                vertices[Batch.U3] = vertices[Batch.U4];
                                vertices[Batch.U4] = tempU;
                                break;
                            }
                            case Cell.ROTATE_180: {
                                float tempU = vertices[Batch.U1];
                                vertices[Batch.U1] = vertices[Batch.U3];
                                vertices[Batch.U3] = tempU;
                                tempU = vertices[Batch.U2];
                                vertices[Batch.U2] = vertices[Batch.U4];
                                vertices[Batch.U4] = tempU;
                                float tempV = vertices[Batch.V1];
                                vertices[Batch.V1] = vertices[Batch.V3];
                                vertices[Batch.V3] = tempV;
                                tempV = vertices[Batch.V2];
                                vertices[Batch.V2] = vertices[Batch.V4];
                                vertices[Batch.V4] = tempV;
                                break;
                            }
                            case Cell.ROTATE_270: {
                                float tempV = vertices[Batch.V1];
                                vertices[Batch.V1] = vertices[Batch.V4];
                                vertices[Batch.V4] = vertices[Batch.V3];
                                vertices[Batch.V3] = vertices[Batch.V2];
                                vertices[Batch.V2] = tempV;

                                float tempU = vertices[Batch.U1];
                                vertices[Batch.U1] = vertices[Batch.U4];
                                vertices[Batch.U4] = vertices[Batch.U3];
                                vertices[Batch.U3] = vertices[Batch.U2];
                                vertices[Batch.U2] = tempU;
                                break;
                            }
                        }
                    }
                    spriteBatch.draw(region.getTexture(), vertices, 0, 20);
                }
            }
        }
    }
}
