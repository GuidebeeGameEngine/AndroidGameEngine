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
package com.guidebee.game.engine.utils.compression.lz;

//--------------------------------- IMPORTS ------------------------------------
import java.io.IOException;

//[------------------------------ MAIN CLASS ----------------------------------]
public class OutWindow {
    byte[] _buffer;
    int _pos;
    int _windowSize = 0;
    int _streamPos;
    java.io.OutputStream _stream;

    public void Create(int windowSize) {
        if (_buffer == null || _windowSize != windowSize) _buffer = new byte[windowSize];
        _windowSize = windowSize;
        _pos = 0;
        _streamPos = 0;
    }

    public void SetStream(java.io.OutputStream stream) throws IOException {
        ReleaseStream();
        _stream = stream;
    }

    public void ReleaseStream() throws IOException {
        Flush();
        _stream = null;
    }

    public void Init(boolean solid) {
        if (!solid) {
            _streamPos = 0;
            _pos = 0;
        }
    }

    public void Flush() throws IOException {
        int size = _pos - _streamPos;
        if (size == 0) return;
        _stream.write(_buffer, _streamPos, size);
        if (_pos >= _windowSize) _pos = 0;
        _streamPos = _pos;
    }

    public void CopyBlock(int distance, int len) throws IOException {
        int pos = _pos - distance - 1;
        if (pos < 0) pos += _windowSize;
        for (; len != 0; len--) {
            if (pos >= _windowSize) pos = 0;
            _buffer[_pos++] = _buffer[pos++];
            if (_pos >= _windowSize) Flush();
        }
    }

    public void PutByte(byte b) throws IOException {
        _buffer[_pos++] = b;
        if (_pos >= _windowSize) Flush();
    }

    public byte GetByte(int distance) {
        int pos = _pos - distance - 1;
        if (pos < 0) pos += _windowSize;
        return _buffer[pos];
    }
}
