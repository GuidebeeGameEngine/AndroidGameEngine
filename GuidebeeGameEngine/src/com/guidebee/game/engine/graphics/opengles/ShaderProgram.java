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

import com.guidebee.game.Application;
import com.guidebee.game.GameEngine;
import com.guidebee.game.files.FileHandle;
import com.guidebee.game.graphics.Color;
import com.guidebee.math.Matrix3;
import com.guidebee.math.Matrix4;
import com.guidebee.math.Vector2;
import com.guidebee.math.Vector3;
import com.guidebee.utils.Disposable;
import com.guidebee.utils.collections.Array;
import com.guidebee.utils.collections.BufferUtils;
import com.guidebee.utils.collections.ObjectIntMap;
import com.guidebee.utils.collections.ObjectMap;

import java.nio.*;

//[------------------------------ MAIN CLASS ----------------------------------]
/**
 * <p>
 * A shader program encapsulates a vertex and fragment shader pair linked to
 * form a shader program useable with OpenGL ES 2.0.
 * </p>
 * <p>
 * <p>
 * After construction a ShaderProgram can be used to draw
 * {@link com.guidebee.game.engine.graphics.Mesh}. To make the GPU use a
 * specific ShaderProgram the programs
 * {@link ShaderProgram#begin()} method must be used which effectively
 * binds the program.
 * </p>
 * <p>
 * <p>
 * When a ShaderProgram is bound one can set uniforms, vertex attributes and
 * attributes as needed via the respective methods.
 * </p>
 * <p>
 * <p>
 * A ShaderProgram can be unbound with a call to {@link ShaderProgram#end()}
 * </p>
 * <p>
 * <p>
 * A ShaderProgram must be disposed via a call to {@link ShaderProgram#dispose()}
 * when it is no longer needed
 * </p>
 * <p>
 * <p>
 * ShaderPrograms are managed. In case the OpenGL context is lost all shaders
 * get invalidated and have to be reloaded. This
 * happens on Android when a user switches to another application or receives
 * an incoming call. Managed ShaderPrograms are
 * automatically reloaded when the OpenGL context is recreated so you don't
 * have to do this manually.
 * </p>
 *
 * @author mzechner
 */
public class ShaderProgram implements Disposable {
    /**
     * default name for position attributes *
     */
    public static final String POSITION_ATTRIBUTE = "a_position";
    /**
     * default name for normal attributes *
     */
    public static final String NORMAL_ATTRIBUTE = "a_normal";
    /**
     * default name for color attributes *
     */
    public static final String COLOR_ATTRIBUTE = "a_color";
    /**
     * default name for texcoords attributes, append texture unit number *
     */
    public static final String TEXCOORD_ATTRIBUTE = "a_texCoord";
    /**
     * default name for tangent attribute *
     */
    public static final String TANGENT_ATTRIBUTE = "a_tangent";
    /**
     * default name for binormal attribute *
     */
    public static final String BINORMAL_ATTRIBUTE = "a_binormal";

    /**
     * flag indicating whether attributes & uniforms must be present at all times *
     */
    public static boolean pedantic = true;

    /**
     * the list of currently available shaders *
     */
    private final static ObjectMap<Application, Array<ShaderProgram>>
            shaders = new ObjectMap<Application, Array<ShaderProgram>>();

    /**
     * the log *
     */
    private String log = "";

    /**
     * whether this program compiled successfully *
     */
    private boolean isCompiled;

    /**
     * uniform lookup *
     */
    private final ObjectIntMap<String> uniforms = new ObjectIntMap<String>();

    /**
     * uniform types *
     */
    private final ObjectIntMap<String> uniformTypes = new ObjectIntMap<String>();

    /**
     * uniform sizes *
     */
    private final ObjectIntMap<String> uniformSizes = new ObjectIntMap<String>();

    /**
     * uniform names *
     */
    private String[] uniformNames;

    /**
     * attribute lookup *
     */
    private final ObjectIntMap<String> attributes = new ObjectIntMap<String>();

    /**
     * attribute types *
     */
    private final ObjectIntMap<String> attributeTypes = new ObjectIntMap<String>();

    /**
     * attribute sizes *
     */
    private final ObjectIntMap<String> attributeSizes = new ObjectIntMap<String>();

    /**
     * attribute names *
     */
    private String[] attributeNames;

    /**
     * program handle *
     */
    private int program;

    /**
     * vertex shader handle *
     */
    private int vertexShaderHandle;

    /**
     * fragment shader handle *
     */
    private int fragmentShaderHandle;

    /**
     * matrix float buffer *
     */
    private final FloatBuffer matrix;

    /**
     * vertex shader source *
     */
    private final String vertexShaderSource;

    /**
     * fragment shader source *
     */
    private final String fragmentShaderSource;

    /**
     * whether this shader was invalidated *
     */
    private boolean invalidated;

    /**
     * direct buffer for passing float and int uniform arrays *
     */
    private ByteBuffer buffer = null;
    private FloatBuffer floatBuffer = null;
    private IntBuffer intBuffer = null;

    /**
     * reference count *
     */
    private int refCount = 0;

    /**
     * Constructs a new ShaderProgram and immediately compiles it.
     *
     * @param vertexShader   the vertex shader
     * @param fragmentShader the fragment shader
     */

    public ShaderProgram(String vertexShader, String fragmentShader) {
        if (vertexShader == null)
            throw new IllegalArgumentException("vertex shader must not be null");
        if (fragmentShader == null)
            throw new IllegalArgumentException("fragment shader must not be null");

        this.vertexShaderSource = vertexShader;
        this.fragmentShaderSource = fragmentShader;
        this.matrix = BufferUtils.newFloatBuffer(16);

        compileShaders(vertexShader, fragmentShader);
        if (isCompiled()) {
            fetchAttributes();
            fetchUniforms();
            addManagedShader(GameEngine.app, this);
        }
    }

    public ShaderProgram(FileHandle vertexShader, FileHandle fragmentShader) {
        this(vertexShader.readString(), fragmentShader.readString());
    }

    /**
     * Loads and compiles the shaders, creates a new program and links the shaders.
     *
     * @param vertexShader
     * @param fragmentShader
     */
    private void compileShaders(String vertexShader, String fragmentShader) {
        vertexShaderHandle = loadShader(IGL20.GL_VERTEX_SHADER, vertexShader);
        fragmentShaderHandle = loadShader(IGL20.GL_FRAGMENT_SHADER, fragmentShader);

        if (vertexShaderHandle == -1 || fragmentShaderHandle == -1) {
            isCompiled = false;
            return;
        }

        program = linkProgram();
        if (program == -1) {
            isCompiled = false;
            return;
        }

        isCompiled = true;
    }

    private int loadShader(int type, String source) {
        IGL20 gl = GameEngine.gl20;
        IntBuffer intbuf = BufferUtils.newIntBuffer(1);

        int shader = gl.glCreateShader(type);
        if (shader == 0) return -1;

        gl.glShaderSource(shader, source);
        gl.glCompileShader(shader);
        gl.glGetShaderiv(shader, IGL20.GL_COMPILE_STATUS, intbuf);

        int compiled = intbuf.get(0);
        if (compiled == 0) {
// gl.glGetShaderiv(shader, IGL20.GL_INFO_LOG_LENGTH, intbuf);
// int infoLogLength = intbuf.get(0);
// if (infoLogLength > 1) {
            String infoLog = gl.glGetShaderInfoLog(shader);
            log += infoLog;
// }
            return -1;
        }

        return shader;
    }

    private int linkProgram() {
        IGL20 gl = GameEngine.gl20;
        int program = gl.glCreateProgram();
        if (program == 0) return -1;

        gl.glAttachShader(program, vertexShaderHandle);
        gl.glAttachShader(program, fragmentShaderHandle);
        gl.glLinkProgram(program);

        ByteBuffer tmp = ByteBuffer.allocateDirect(4);
        tmp.order(ByteOrder.nativeOrder());
        IntBuffer intbuf = tmp.asIntBuffer();

        gl.glGetProgramiv(program, IGL20.GL_LINK_STATUS, intbuf);
        int linked = intbuf.get(0);
        if (linked == 0) {
// GameEngine.gl20.glGetProgramiv(program, IGL20.GL_INFO_LOG_LENGTH, intbuf);
// int infoLogLength = intbuf.get(0);
// if (infoLogLength > 1) {
            log = GameEngine.gl20.glGetProgramInfoLog(program);
// }
            return -1;
        }

        return program;
    }

    final static IntBuffer intbuf = BufferUtils.newIntBuffer(1);

    /**
     * @return the log info for the shader compilation and program
     * linking stage. The shader needs to be bound for this method to
     * have an effect.
     */
    public String getLog() {
        if (isCompiled) {
// GameEngine.gl20.glGetProgramiv(program, IGL20.GL_INFO_LOG_LENGTH, intbuf);
// int infoLogLength = intbuf.get(0);
// if (infoLogLength > 1) {
            log = GameEngine.gl20.glGetProgramInfoLog(program);
// }
            return log;
        } else {
            return log;
        }
    }

    /**
     * @return whether this ShaderProgram compiled successfully.
     */
    public boolean isCompiled() {
        return isCompiled;
    }

    private int fetchAttributeLocation(String name) {
        IGL20 gl = GameEngine.gl20;
        // -2 == not yet cached
        // -1 == cached but not found
        int location;
        if ((location = attributes.get(name, -2)) == -2) {
            location = gl.glGetAttribLocation(program, name);
            attributes.put(name, location);
        }
        return location;
    }

    private int fetchUniformLocation(String name) {
        return fetchUniformLocation(name, pedantic);
    }

    public int fetchUniformLocation(String name, boolean pedantic) {
        IGL20 gl = GameEngine.gl20;
        // -2 == not yet cached
        // -1 == cached but not found
        int location;
        if ((location = uniforms.get(name, -2)) == -2) {
            location = gl.glGetUniformLocation(program, name);
            if (location == -1 && pedantic)
                throw new IllegalArgumentException("no uniform with name '"
                        + name + "' in shader");
            uniforms.put(name, location);
        }
        return location;
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name  the name of the uniform
     * @param value the value
     */
    public void setUniformi(String name, int value) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        gl.glUniform1i(location, value);
    }

    public void setUniformi(int location, int value) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glUniform1i(location, value);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     */
    public void setUniformi(String name, int value1, int value2) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        gl.glUniform2i(location, value1, value2);
    }

    public void setUniformi(int location, int value1, int value2) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glUniform2i(location, value1, value2);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     */
    public void setUniformi(String name, int value1, int value2, int value3) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        gl.glUniform3i(location, value1, value2, value3);
    }

    public void setUniformi(int location, int value1, int value2, int value3) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glUniform3i(location, value1, value2, value3);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    public void setUniformi(String name, int value1, int value2, int value3, int value4) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        gl.glUniform4i(location, value1, value2, value3, value4);
    }

    public void setUniformi(int location, int value1, int value2, int value3, int value4) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glUniform4i(location, value1, value2, value3, value4);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name  the name of the uniform
     * @param value the value
     */
    public void setUniformf(String name, float value) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        gl.glUniform1f(location, value);
    }

    public void setUniformf(int location, float value) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glUniform1f(location, value);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     */
    public void setUniformf(String name, float value1, float value2) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        gl.glUniform2f(location, value1, value2);
    }

    public void setUniformf(int location, float value1, float value2) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glUniform2f(location, value1, value2);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     */
    public void setUniformf(String name, float value1, float value2, float value3) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        gl.glUniform3f(location, value1, value2, value3);
    }

    public void setUniformf(int location, float value1, float value2, float value3) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glUniform3f(location, value1, value2, value3);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    public void setUniformf(String name, float value1, float value2, float value3, float value4) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        gl.glUniform4f(location, value1, value2, value3, value4);
    }

    public void setUniformf(int location, float value1, float value2, float value3, float value4) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glUniform4f(location, value1, value2, value3, value4);
    }

    public void setUniform1fv(String name, float[] values, int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        gl.glUniform1fv(location, length, floatBuffer);
    }

    public void setUniform1fv(int location, float[] values, int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        gl.glUniform1fv(location, length, floatBuffer);
    }

    public void setUniform2fv(String name, float[] values, int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        gl.glUniform2fv(location, length / 2, floatBuffer);
    }

    public void setUniform2fv(int location, float[] values, int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        gl.glUniform2fv(location, length / 2, floatBuffer);
    }

    public void setUniform3fv(String name, float[] values, int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        gl.glUniform3fv(location, length / 3, floatBuffer);
    }

    public void setUniform3fv(int location, float[] values, int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        gl.glUniform3fv(location, length / 3, floatBuffer);
    }

    public void setUniform4fv(String name, float[] values, int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        gl.glUniform4fv(location, length / 4, floatBuffer);
    }

    public void setUniform4fv(int location, float[] values, int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        gl.glUniform4fv(location, length / 4, floatBuffer);
    }

    /**
     * Sets the uniform matrix with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param matrix the matrix
     */
    public void setUniformMatrix(String name, Matrix4 matrix) {
        setUniformMatrix(name, matrix, false);
    }

    /**
     * Sets the uniform matrix with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name      the name of the uniform
     * @param matrix    the matrix
     * @param transpose whether the matrix should be transposed
     */
    public void setUniformMatrix(String name, Matrix4 matrix, boolean transpose) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        this.matrix.clear();
        BufferUtils.copy(matrix.val, this.matrix, matrix.val.length, 0);
        gl.glUniformMatrix4fv(location, 1, transpose, this.matrix);
    }

    public void setUniformMatrix(int location, Matrix4 matrix) {
        setUniformMatrix(location, matrix, false);
    }

    public void setUniformMatrix(int location, Matrix4 matrix, boolean transpose) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        this.matrix.clear();
        BufferUtils.copy(matrix.val, this.matrix, matrix.val.length, 0);
        gl.glUniformMatrix4fv(location, 1, transpose, this.matrix);
    }

    /**
     * Sets the uniform matrix with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param matrix the matrix
     */
    public void setUniformMatrix(String name, Matrix3 matrix) {
        setUniformMatrix(name, matrix, false);
    }

    /**
     * Sets the uniform matrix with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name      the name of the uniform
     * @param matrix    the matrix
     * @param transpose whether the uniform matrix should be transposed
     */
    public void setUniformMatrix(String name, Matrix3 matrix, boolean transpose) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchUniformLocation(name);
        float[] vals = matrix.getValues();
        this.matrix.clear();
        BufferUtils.copy(vals, this.matrix, vals.length, 0);
        gl.glUniformMatrix3fv(location, 1, transpose, this.matrix);
    }

    public void setUniformMatrix(int location, Matrix3 matrix) {
        setUniformMatrix(location, matrix, false);
    }

    public void setUniformMatrix(int location, Matrix3 matrix, boolean transpose) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        float[] vals = matrix.getValues();
        this.matrix.clear();
        BufferUtils.copy(vals, this.matrix, vals.length, 0);
        gl.glUniformMatrix3fv(location, 1, transpose, this.matrix);
    }

    /**
     * Sets an array of uniform matrices with the given name. Throws an
     * IllegalArgumentException in case it is not called in
     * between a {@link #begin()}/{@link #end()} block.
     *
     * @param name      the name of the uniform
     * @param buffer    buffer containing the matrix data
     * @param transpose whether the uniform matrix should be transposed
     */
    public void setUniformMatrix3fv(String name, FloatBuffer buffer, int count,
                                    boolean transpose) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        buffer.position(0);
        int location = fetchUniformLocation(name);
        gl.glUniformMatrix3fv(location, count, transpose, buffer);
    }

    /**
     * Sets an array of uniform matrices with the given name. Throws an
     * IllegalArgumentException in case it is not called in
     * between a {@link #begin()}/{@link #end()} block.
     *
     * @param name      the name of the uniform
     * @param buffer    buffer containing the matrix data
     * @param transpose whether the uniform matrix should be transposed
     */
    public void setUniformMatrix4fv(String name, FloatBuffer buffer,
                                    int count, boolean transpose) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        buffer.position(0);
        int location = fetchUniformLocation(name);
        gl.glUniformMatrix4fv(location, count, transpose, buffer);
    }

    public void setUniformMatrix4fv(int location, float[] values,
                                    int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        gl.glUniformMatrix4fv(location, length / 16, false, floatBuffer);
    }

    public void setUniformMatrix4fv(String name, float[] values,
                                    int offset, int length) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        ensureBufferCapacity(length << 2);
        floatBuffer.clear();
        BufferUtils.copy(values, floatBuffer, length, offset);
        int location = fetchUniformLocation(name);
        gl.glUniformMatrix4fv(location, length / 16, false, floatBuffer);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param values x and y as the first and second values respectively
     */
    public void setUniformf(String name, Vector2 values) {
        setUniformf(name, values.x, values.y);
    }

    public void setUniformf(int location, Vector2 values) {
        setUniformf(location, values.x, values.y);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param values x, y and z as the first, second and third values respectively
     */
    public void setUniformf(String name, Vector3 values) {
        setUniformf(name, values.x, values.y, values.z);
    }

    public void setUniformf(int location, Vector3 values) {
        setUniformf(location, values.x, values.y, values.z);
    }

    /**
     * Sets the uniform with the given name. Throws an IllegalArgumentException
     * in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name   the name of the uniform
     * @param values r, g, b and a as the first through fourth values respectively
     */
    public void setUniformf(String name, Color values) {
        setUniformf(name, values.r, values.g, values.b, values.a);
    }

    public void setUniformf(int location, Color values) {
        setUniformf(location, values.r, values.g, values.b, values.a);
    }

    /**
     * Sets the vertex attribute with the given name. Throws an
     * IllegalArgumentException in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name      the attribute name
     * @param size      the number of components, must be >= 1 and <= 4
     * @param type      the type, must be one of IGL20.GL_BYTE, IGL20.GL_UNSIGNED_BYTE,
     *                  IGL20.GL_SHORT,
     *                  IGL20.GL_UNSIGNED_SHORT,IGL20.GL_FIXED, or IGL20.GL_FLOAT.
     *                  GL_FIXED will not work on the desktop
     * @param normalize whether fixed point data should be normalized. Will not
     *                  work on the desktop
     * @param stride    the stride in bytes between successive attributes
     * @param buffer    the buffer containing the vertex attributes.
     */
    public void setVertexAttribute(String name, int size, int type, boolean normalize,
                                   int stride, Buffer buffer) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchAttributeLocation(name);
        if (location == -1) return;
        gl.glVertexAttribPointer(location, size, type, normalize, stride, buffer);
    }

    public void setVertexAttribute(int location, int size, int type, boolean normalize,
                                   int stride, Buffer buffer) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glVertexAttribPointer(location, size, type, normalize, stride, buffer);
    }

    /**
     * Sets the vertex attribute with the given name. Throws an
     * IllegalArgumentException in case it is not called in between a
     * {@link #begin()}/{@link #end()} block.
     *
     * @param name      the attribute name
     * @param size      the number of components, must be >= 1 and <= 4
     * @param type      the type, must be one of IGL20.GL_BYTE, IGL20.GL_UNSIGNED_BYTE,
     *                  IGL20.GL_SHORT,
     *                  IGL20.GL_UNSIGNED_SHORT,IGL20.GL_FIXED, or IGL20.GL_FLOAT.
     *                  GL_FIXED will not work on the desktop
     * @param normalize whether fixed point data should be normalized.
     *                  Will not work on the desktop
     * @param stride    the stride in bytes between successive attributes
     * @param offset    byte offset into the vertex buffer object bound to IGL20.GL_ARRAY_BUFFER.
     */
    public void setVertexAttribute(String name, int size, int type, boolean normalize,
                                   int stride, int offset) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchAttributeLocation(name);
        if (location == -1) return;
        gl.glVertexAttribPointer(location, size, type, normalize, stride, offset);
    }

    public void setVertexAttribute(int location, int size, int type, boolean normalize,
                                   int stride, int offset) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glVertexAttribPointer(location, size, type, normalize, stride, offset);
    }

    /**
     * Makes OpenGL ES 2.0 use this vertex and fragment shader pair. When you are done
     * with this shader you have to call
     * {@link ShaderProgram#end()}.
     */
    public void begin() {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glUseProgram(program);
    }

    /**
     * Disables this shader. Must be called when one is done with the shader. Don't
     * mix it with dispose, that will release the
     * shader resources.
     */
    public void end() {
        IGL20 gl = GameEngine.gl20;
        gl.glUseProgram(0);
    }

    /**
     * Disposes all resources associated with this shader. Must be called when the
     * shader is no longer used.
     */
    public void dispose() {
        IGL20 gl = GameEngine.gl20;
        gl.glUseProgram(0);
        gl.glDeleteShader(vertexShaderHandle);
        gl.glDeleteShader(fragmentShaderHandle);
        gl.glDeleteProgram(program);
        if (shaders.get(GameEngine.app) != null)
            shaders.get(GameEngine.app).removeValue(this, true);
    }

    /**
     * Disables the vertex attribute with the given name
     *
     * @param name the vertex attribute name
     */
    public void disableVertexAttribute(String name) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchAttributeLocation(name);
        if (location == -1) return;
        gl.glDisableVertexAttribArray(location);
    }

    public void disableVertexAttribute(int location) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glDisableVertexAttribArray(location);
    }

    /**
     * Enables the vertex attribute with the given name
     *
     * @param name the vertex attribute name
     */
    public void enableVertexAttribute(String name) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        int location = fetchAttributeLocation(name);
        if (location == -1) return;
        gl.glEnableVertexAttribArray(location);
    }

    public void enableVertexAttribute(int location) {
        IGL20 gl = GameEngine.gl20;
        checkManaged();
        gl.glEnableVertexAttribArray(location);
    }

    private void checkManaged() {
        if (invalidated) {
            compileShaders(vertexShaderSource, fragmentShaderSource);
            invalidated = false;
        }
    }

    private void addManagedShader(Application app, ShaderProgram shaderProgram) {
        Array<ShaderProgram> managedResources = shaders.get(app);
        if (managedResources == null) managedResources = new Array<ShaderProgram>();
        managedResources.add(shaderProgram);
        shaders.put(app, managedResources);
    }

    /**
     * Invalidates all shaders so the next time they are used new handles are generated
     *
     * @param app
     */
    public static void invalidateAllShaderPrograms(Application app) {
        if (GameEngine.gl20 == null) return;

        Array<ShaderProgram> shaderArray = shaders.get(app);
        if (shaderArray == null) return;

        for (int i = 0; i < shaderArray.size; i++) {
            shaderArray.get(i).invalidated = true;
            shaderArray.get(i).checkManaged();
        }
    }

    public static void clearAllShaderPrograms(Application app) {
        shaders.remove(app);
    }

    public static String getManagedStatus() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        builder.append("Managed shaders/app: { ");
        for (Application app : shaders.keys()) {
            builder.append(shaders.get(app).size);
            builder.append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * Sets the given attribute
     *
     * @param name   the name of the attribute
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    public void setAttributef(String name, float value1, float value2,
                              float value3, float value4) {
        IGL20 gl = GameEngine.gl20;
        int location = fetchAttributeLocation(name);
        gl.glVertexAttrib4f(location, value1, value2, value3, value4);
    }

    private void ensureBufferCapacity(int numBytes) {
        if (buffer == null || buffer.capacity() < numBytes) {
            buffer = BufferUtils.newByteBuffer(numBytes);
            floatBuffer = buffer.asFloatBuffer();
            intBuffer = buffer.asIntBuffer();
        }
    }

    IntBuffer params = BufferUtils.newIntBuffer(1);
    IntBuffer type = BufferUtils.newIntBuffer(1);

    private void fetchUniforms() {
        params.clear();
        GameEngine.gl20.glGetProgramiv(program, IGL20.GL_ACTIVE_UNIFORMS, params);
        int numUniforms = params.get(0);

        uniformNames = new String[numUniforms];

        for (int i = 0; i < numUniforms; i++) {
            params.clear();
            params.put(0, 1);
            type.clear();
            String name = GameEngine.gl20.glGetActiveUniform(program, i, params, type);
            int location = GameEngine.gl20.glGetUniformLocation(program, name);
            uniforms.put(name, location);
            uniformTypes.put(name, type.get(0));
            uniformSizes.put(name, params.get(0));
            uniformNames[i] = name;
        }
    }

    private void fetchAttributes() {
        params.clear();
        GameEngine.gl20.glGetProgramiv(program, IGL20.GL_ACTIVE_ATTRIBUTES, params);
        int numAttributes = params.get(0);

        attributeNames = new String[numAttributes];

        for (int i = 0; i < numAttributes; i++) {
            params.clear();
            params.put(0, 1);
            type.clear();
            String name = GameEngine.gl20.glGetActiveAttrib(program, i, params, type);
            int location = GameEngine.gl20.glGetAttribLocation(program, name);
            attributes.put(name, location);
            attributeTypes.put(name, type.get(0));
            attributeSizes.put(name, params.get(0));
            attributeNames[i] = name;
        }
    }

    /**
     * @param name the name of the attribute
     * @return whether the attribute is available in the shader
     */
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * @param name the name of the attribute
     * @return the type of the attribute, one of {@link IGL20#GL_FLOAT},
     * {@link IGL20#GL_FLOAT_VEC2} etc.
     */
    public int getAttributeType(String name) {
        return attributeTypes.get(name, 0);
    }

    /**
     * @param name the name of the attribute
     * @return the location of the attribute or -1.
     */
    public int getAttributeLocation(String name) {
        return attributes.get(name, -1);
    }

    /**
     * @param name the name of the attribute
     * @return the size of the attribute or 0.
     */
    public int getAttributeSize(String name) {
        return attributeSizes.get(name, 0);
    }

    /**
     * @param name the name of the uniform
     * @return whether the uniform is available in the shader
     */
    public boolean hasUniform(String name) {
        return uniforms.containsKey(name);
    }

    /**
     * @param name the name of the uniform
     * @return the type of the uniform, one of {@link IGL20#GL_FLOAT},
     * {@link IGL20#GL_FLOAT_VEC2} etc.
     */
    public int getUniformType(String name) {
        return uniformTypes.get(name, 0);
    }

    /**
     * @param name the name of the uniform
     * @return the location of the uniform or -1.
     */
    public int getUniformLocation(String name) {
        return uniforms.get(name, -1);
    }

    /**
     * @param name the name of the uniform
     * @return the size of the uniform or 0.
     */
    public int getUniformSize(String name) {
        return uniformSizes.get(name, 0);
    }

    /**
     * @return the attributes
     */
    public String[] getAttributes() {
        return attributeNames;
    }

    /**
     * @return the uniforms
     */
    public String[] getUniforms() {
        return uniformNames;
    }

    /**
     * @return the source of the vertex shader
     */
    public String getVertexShaderSource() {
        return vertexShaderSource;
    }

    /**
     * @return the source of the fragment shader
     */
    public String getFragmentShaderSource() {
        return fragmentShaderSource;
    }
}
