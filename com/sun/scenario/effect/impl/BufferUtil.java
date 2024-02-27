/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.scenario.effect.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * Utility routines for dealing with direct buffers.
 */
public class BufferUtil {

    public static final int SIZEOF_BYTE = 1;
    public static final int SIZEOF_SHORT = 2;
    public static final int SIZEOF_INT = 4;
    public static final int SIZEOF_FLOAT = 4;
    public static final int SIZEOF_LONG = 8;
    public static final int SIZEOF_DOUBLE = 8;

    private BufferUtil() {
    }

    /**
     * Allocates a new direct ByteBuffer with the specified number of
     * elements. The returned buffer will have its byte order set to
     * the host platform's native byte order.
     */
    public static ByteBuffer newByteBuffer(int numElements) {
        ByteBuffer bb = ByteBuffer.allocateDirect(numElements);
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }

    /**
     * Allocates a new direct DoubleBuffer with the specified number of
     * elements. The returned buffer will have its byte order set to
     * the host platform's native byte order.
     */
    public static DoubleBuffer newDoubleBuffer(int numElements) {
        ByteBuffer bb = newByteBuffer(numElements * SIZEOF_DOUBLE);
        return bb.asDoubleBuffer();
    }

    /**
     * Allocates a new direct FloatBuffer with the specified number of
     * elements. The returned buffer will have its byte order set to
     * the host platform's native byte order.
     */
    public static FloatBuffer newFloatBuffer(int numElements) {
        ByteBuffer bb = newByteBuffer(numElements * SIZEOF_FLOAT);
        return bb.asFloatBuffer();
    }

    /**
     * Allocates a new direct IntBuffer with the specified number of
     * elements. The returned buffer will have its byte order set to
     * the host platform's native byte order.
     */
    public static IntBuffer newIntBuffer(int numElements) {
        ByteBuffer bb = newByteBuffer(numElements * SIZEOF_INT);
        return bb.asIntBuffer();
    }

    /**
     * Allocates a new direct LongBuffer with the specified number of
     * elements. The returned buffer will have its byte order set to
     * the host platform's native byte order.
     */
    public static LongBuffer newLongBuffer(int numElements) {
        ByteBuffer bb = newByteBuffer(numElements * SIZEOF_LONG);
        return bb.asLongBuffer();
    }

    /**
     * Allocates a new direct ShortBuffer with the specified number of
     * elements. The returned buffer will have its byte order set to
     * the host platform's native byte order.
     */
    public static ShortBuffer newShortBuffer(int numElements) {
        ByteBuffer bb = newByteBuffer(numElements * SIZEOF_SHORT);
        return bb.asShortBuffer();
    }
}
