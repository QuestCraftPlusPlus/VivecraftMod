/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * MACHINE GENERATED FILE, DO NOT EDIT
 */
package org.lwjgl.openxr;

import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;

import static org.lwjgl.system.Checks.check;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * <pre>{@code
 * struct XrLoaderInitInfoAndroidKHR {
 *     XrStructureType type;
 *     void const * next;
 *     void * applicationVM;
 *     void * applicationContext;
 * }}</pre>
 */
public class XrLoaderInitInfoAndroidKHR extends Struct<XrLoaderInitInfoAndroidKHR> implements NativeResource {

    /** The struct size in bytes. */
    public static final int SIZEOF;

    /** The struct alignment in bytes. */
    public static final int ALIGNOF;

    /** The struct member offsets. */
    public static final int
        TYPE,
        NEXT,
        APPLICATIONVM,
        APPLICATIONCONTEXT;

    static {
        Layout layout = __struct(
            __member(4),
            __member(POINTER_SIZE),
            __member(POINTER_SIZE),
            __member(POINTER_SIZE)
        );

        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();

        TYPE = layout.offsetof(0);
        NEXT = layout.offsetof(1);
        APPLICATIONVM = layout.offsetof(2);
        APPLICATIONCONTEXT = layout.offsetof(3);
    }

    protected XrLoaderInitInfoAndroidKHR(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected XrLoaderInitInfoAndroidKHR create(long address, @Nullable ByteBuffer container) {
        return new XrLoaderInitInfoAndroidKHR(address, container);
    }

    /**
     * Creates a {@code XrLoaderInitInfoAndroidKHR} instance at the current position of the specified {@link ByteBuffer} container. Changes to the buffer's content will be
     * visible to the struct instance and vice versa.
     *
     * <p>The created instance holds a strong reference to the container object.</p>
     */
    public XrLoaderInitInfoAndroidKHR(ByteBuffer container) {
        super(memAddress(container), __checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() { return SIZEOF; }

    /** @return the value of the {@code type} field. */
    @NativeType("XrStructureType")
    public int type() { return ntype(address()); }
    /** @return the value of the {@code next} field. */
    @NativeType("void const *")
    public long next() { return nnext(address()); }
    /** @return the value of the {@code applicationVM} field. */
    @NativeType("void *")
    public long applicationVM() { return napplicationVM(address()); }
    /** @return the value of the {@code applicationContext} field. */
    @NativeType("void *")
    public long applicationContext() { return napplicationContext(address()); }

    /** Sets the specified value to the {@code type} field. */
    public XrLoaderInitInfoAndroidKHR type(@NativeType("XrStructureType") int value) { ntype(address(), value); return this; }
    /** Sets the {@link KHRLoaderInitAndroid#XR_TYPE_LOADER_INIT_INFO_ANDROID_KHR TYPE_LOADER_INIT_INFO_ANDROID_KHR} value to the {@code type} field. */
    public XrLoaderInitInfoAndroidKHR type$Default() { return type(KHRLoaderInitAndroid.XR_TYPE_LOADER_INIT_INFO_ANDROID_KHR); }
    /** Sets the specified value to the {@code next} field. */
    public XrLoaderInitInfoAndroidKHR next(@NativeType("void const *") long value) { nnext(address(), value); return this; }
    /** Sets the specified value to the {@code applicationVM} field. */
    public XrLoaderInitInfoAndroidKHR applicationVM(@NativeType("void *") long value) { napplicationVM(address(), value); return this; }
    /** Sets the specified value to the {@code applicationContext} field. */
    public XrLoaderInitInfoAndroidKHR applicationContext(@NativeType("void *") long value) { napplicationContext(address(), value); return this; }

    /** Initializes this struct with the specified values. */
    public XrLoaderInitInfoAndroidKHR set(
        int type,
        long next,
        long applicationVM,
        long applicationContext
    ) {
        type(type);
        next(next);
        applicationVM(applicationVM);
        applicationContext(applicationContext);

        return this;
    }

    /**
     * Copies the specified struct data to this struct.
     *
     * @param src the source struct
     *
     * @return this struct
     */
    public XrLoaderInitInfoAndroidKHR set(XrLoaderInitInfoAndroidKHR src) {
        memCopy(src.address(), address(), SIZEOF);
        return this;
    }

    // -----------------------------------

    /** Returns a new {@code XrLoaderInitInfoAndroidKHR} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed. */
    public static XrLoaderInitInfoAndroidKHR malloc() {
        return new XrLoaderInitInfoAndroidKHR(nmemAllocChecked(SIZEOF), null);
    }

    /** Returns a new {@code XrLoaderInitInfoAndroidKHR} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed. */
    public static XrLoaderInitInfoAndroidKHR calloc() {
        return new XrLoaderInitInfoAndroidKHR(nmemCallocChecked(1, SIZEOF), null);
    }

    /** Returns a new {@code XrLoaderInitInfoAndroidKHR} instance allocated with {@link BufferUtils}. */
    public static XrLoaderInitInfoAndroidKHR create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new XrLoaderInitInfoAndroidKHR(memAddress(container), container);
    }

    /** Returns a new {@code XrLoaderInitInfoAndroidKHR} instance for the specified memory address. */
    public static XrLoaderInitInfoAndroidKHR create(long address) {
        return new XrLoaderInitInfoAndroidKHR(address, null);
    }

    /** Like {@link #create(long) create}, but returns {@code null} if {@code address} is {@code NULL}. */
    public static @Nullable XrLoaderInitInfoAndroidKHR createSafe(long address) {
        return address == NULL ? null : new XrLoaderInitInfoAndroidKHR(address, null);
    }

    /** Downcasts the specified {@code XrLoaderInitInfoBaseHeaderKHR} instance to {@code XrLoaderInitInfoAndroidKHR}. */
    public static XrLoaderInitInfoAndroidKHR create(XrLoaderInitInfoBaseHeaderKHR value) {
        return new XrLoaderInitInfoAndroidKHR(value.address(), __getContainer(value));
    }

    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static Buffer malloc(int capacity) {
        return new Buffer(nmemAllocChecked(__checkMalloc(capacity, SIZEOF)), capacity);
    }

    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static Buffer calloc(int capacity) {
        return new Buffer(nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param capacity the buffer capacity
     */
    public static Buffer create(int capacity) {
        ByteBuffer container = __create(capacity, SIZEOF);
        return new Buffer(memAddress(container), container, -1, 0, capacity, capacity);
    }

    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    /** Like {@link #create(long, int) create}, but returns {@code null} if {@code address} is {@code NULL}. */
    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == NULL ? null : new Buffer(address, capacity);
    }

    /** Downcasts the specified {@code XrLoaderInitInfoBaseHeaderKHR.Buffer} instance to {@code XrLoaderInitInfoAndroidKHR.Buffer}. */
    public static Buffer create(XrLoaderInitInfoBaseHeaderKHR.Buffer value) {
        return new Buffer(value.address(), __getContainer(value), -1, 0, value.remaining(), value.remaining());
    }

    /**
     * Returns a new {@code XrLoaderInitInfoAndroidKHR} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static XrLoaderInitInfoAndroidKHR malloc(MemoryStack stack) {
        return new XrLoaderInitInfoAndroidKHR(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    /**
     * Returns a new {@code XrLoaderInitInfoAndroidKHR} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static XrLoaderInitInfoAndroidKHR calloc(MemoryStack stack) {
        return new XrLoaderInitInfoAndroidKHR(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    // -----------------------------------

    /** Unsafe version of {@link #type}. */
    public static int ntype(long struct) { return memGetInt(struct + XrLoaderInitInfoAndroidKHR.TYPE); }
    /** Unsafe version of {@link #next}. */
    public static long nnext(long struct) { return memGetAddress(struct + XrLoaderInitInfoAndroidKHR.NEXT); }
    /** Unsafe version of {@link #applicationVM}. */
    public static long napplicationVM(long struct) { return memGetAddress(struct + XrLoaderInitInfoAndroidKHR.APPLICATIONVM); }
    /** Unsafe version of {@link #applicationContext}. */
    public static long napplicationContext(long struct) { return memGetAddress(struct + XrLoaderInitInfoAndroidKHR.APPLICATIONCONTEXT); }

    /** Unsafe version of {@link #type(int) type}. */
    public static void ntype(long struct, int value) { memPutInt(struct + XrLoaderInitInfoAndroidKHR.TYPE, value); }
    /** Unsafe version of {@link #next(long) next}. */
    public static void nnext(long struct, long value) { memPutAddress(struct + XrLoaderInitInfoAndroidKHR.NEXT, value); }
    /** Unsafe version of {@link #applicationVM(long) applicationVM}. */
    public static void napplicationVM(long struct, long value) { memPutAddress(struct + XrLoaderInitInfoAndroidKHR.APPLICATIONVM, check(value)); }
    /** Unsafe version of {@link #applicationContext(long) applicationContext}. */
    public static void napplicationContext(long struct, long value) { memPutAddress(struct + XrLoaderInitInfoAndroidKHR.APPLICATIONCONTEXT, check(value)); }

    /**
     * Validates pointer members that should not be {@code NULL}.
     *
     * @param struct the struct to validate
     */
    public static void validate(long struct) {
        check(memGetAddress(struct + XrLoaderInitInfoAndroidKHR.APPLICATIONVM));
        check(memGetAddress(struct + XrLoaderInitInfoAndroidKHR.APPLICATIONCONTEXT));
    }

    // -----------------------------------

    /** An array of {@link XrLoaderInitInfoAndroidKHR} structs. */
    public static class Buffer extends StructBuffer<XrLoaderInitInfoAndroidKHR, Buffer> implements NativeResource {

        private static final XrLoaderInitInfoAndroidKHR ELEMENT_FACTORY = XrLoaderInitInfoAndroidKHR.create(-1L);

        /**
         * Creates a new {@code XrLoaderInitInfoAndroidKHR.Buffer} instance backed by the specified container.
         *
         * <p>Changes to the container's content will be visible to the struct buffer instance and vice versa. The two buffers' position, limit, and mark values
         * will be independent. The new buffer's position will be zero, its capacity and its limit will be the number of bytes remaining in this buffer divided
         * by {@link XrLoaderInitInfoAndroidKHR#SIZEOF}, and its mark will be undefined.</p>
         *
         * <p>The created buffer instance holds a strong reference to the container object.</p>
         */
        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / SIZEOF);
        }

        public Buffer(long address, int cap) {
            super(address, null, -1, 0, cap, cap);
        }

        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {
            super(address, container, mark, pos, lim, cap);
        }

        @Override
        protected Buffer self() {
            return this;
        }

        @Override
        protected Buffer create(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity) {
            return new Buffer(address, container, mark, position, limit, capacity);
        }

        @Override
        protected XrLoaderInitInfoAndroidKHR getElementFactory() {
            return ELEMENT_FACTORY;
        }

        /** @return the value of the {@code type} field. */
        @NativeType("XrStructureType")
        public int type() { return XrLoaderInitInfoAndroidKHR.ntype(address()); }
        /** @return the value of the {@code next} field. */
        @NativeType("void const *")
        public long next() { return XrLoaderInitInfoAndroidKHR.nnext(address()); }
        /** @return the value of the {@code applicationVM} field. */
        @NativeType("void *")
        public long applicationVM() { return XrLoaderInitInfoAndroidKHR.napplicationVM(address()); }
        /** @return the value of the {@code applicationContext} field. */
        @NativeType("void *")
        public long applicationContext() { return XrLoaderInitInfoAndroidKHR.napplicationContext(address()); }

        /** Sets the specified value to the {@code type} field. */
        public Buffer type(@NativeType("XrStructureType") int value) { XrLoaderInitInfoAndroidKHR.ntype(address(), value); return this; }
        /** Sets the {@link KHRLoaderInitAndroid#XR_TYPE_LOADER_INIT_INFO_ANDROID_KHR TYPE_LOADER_INIT_INFO_ANDROID_KHR} value to the {@code type} field. */
        public Buffer type$Default() { return type(KHRLoaderInitAndroid.XR_TYPE_LOADER_INIT_INFO_ANDROID_KHR); }
        /** Sets the specified value to the {@code next} field. */
        public Buffer next(@NativeType("void const *") long value) { XrLoaderInitInfoAndroidKHR.nnext(address(), value); return this; }
        /** Sets the specified value to the {@code applicationVM} field. */
        public Buffer applicationVM(@NativeType("void *") long value) { XrLoaderInitInfoAndroidKHR.napplicationVM(address(), value); return this; }
        /** Sets the specified value to the {@code applicationContext} field. */
        public Buffer applicationContext(@NativeType("void *") long value) { XrLoaderInitInfoAndroidKHR.napplicationContext(address(), value); return this; }

    }

}
