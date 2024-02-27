package com.sun.scenario.effect.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public class BufferUtil {
   public static final int SIZEOF_BYTE = 1;
   public static final int SIZEOF_SHORT = 2;
   public static final int SIZEOF_INT = 4;
   public static final int SIZEOF_FLOAT = 4;
   public static final int SIZEOF_LONG = 8;
   public static final int SIZEOF_DOUBLE = 8;

   private BufferUtil() {
   }

   public static ByteBuffer newByteBuffer(int numElements) {
      ByteBuffer bb = ByteBuffer.allocateDirect(numElements);
      bb.order(ByteOrder.nativeOrder());
      return bb;
   }

   public static DoubleBuffer newDoubleBuffer(int numElements) {
      ByteBuffer bb = newByteBuffer(numElements * 8);
      return bb.asDoubleBuffer();
   }

   public static FloatBuffer newFloatBuffer(int numElements) {
      ByteBuffer bb = newByteBuffer(numElements * 4);
      return bb.asFloatBuffer();
   }

   public static IntBuffer newIntBuffer(int numElements) {
      ByteBuffer bb = newByteBuffer(numElements * 4);
      return bb.asIntBuffer();
   }

   public static LongBuffer newLongBuffer(int numElements) {
      ByteBuffer bb = newByteBuffer(numElements * 8);
      return bb.asLongBuffer();
   }

   public static ShortBuffer newShortBuffer(int numElements) {
      ByteBuffer bb = newByteBuffer(numElements * 2);
      return bb.asShortBuffer();
   }
}
