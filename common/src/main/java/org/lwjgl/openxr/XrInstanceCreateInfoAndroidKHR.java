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
 * struct XrInstanceCreateInfoAndroidKHR {
 *     XrStructureType type;
 *     void const * next;
 *     void * applicationVM;
 *     void * applicationActivity;
 * }}</pre>
 */
public class XrInstanceCreateInfoAndroidKHR extends Struct<XrInstanceCreateInfoAndroidKHR> implements NativeResource {

    /** The struct size in bytes. */
    public static final int SIZEOF;

    /** The struct alignment in bytes. */
    public static final int ALIGNOF;

    /** The struct member offsets. */
    public static final int
        TYPE,
        NEXT,
        APPLICATIONVM,
        APPLICATIONACTIVITY;

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
        APPLICATIONACTIVITY = layout.offsetof(3);
    }

    protected XrInstanceCreateInfoAndroidKHR(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected XrInstanceCreateInfoAndroidKHR create(long address, @Nullable ByteBuffer container) {
        return new XrInstanceCreateInfoAndroidKHR(address, container);
    }

    /**
     * Creates a {@code XrInstanceCreateInfoAndroidKHR} instance at the current position of the specified {@link ByteBuffer} container. Changes to the buffer's content will be
     * visible to the struct instance and vice versa.
     *
     * <p>The created instance holds a strong reference to the container object.</p>
     */
    public XrInstanceCreateInfoAndroidKHR(ByteBuffer container) {
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
    /** @return the value of the {@code applicationActivity} field. */
    @NativeType("void *")
    public long applicationActivity() { return napplicationActivity(address()); }

    /** Sets the specified value to the {@code type} field. */
    public XrInstanceCreateInfoAndroidKHR type(@NativeType("XrStructureType") int value) { ntype(address(), value); return this; }
    /** Sets the {@link KHRAndroidCreateInstance#XR_TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR} value to the {@code type} field. */
    public XrInstanceCreateInfoAndroidKHR type$Default() { return type(KHRAndroidCreateInstance.XR_TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR); }
    /** Sets the specified value to the {@code next} field. */
    public XrInstanceCreateInfoAndroidKHR next(@NativeType("void const *") long value) { nnext(address(), value); return this; }
    /** Sets the specified value to the {@code applicationVM} field. */
    public XrInstanceCreateInfoAndroidKHR applicationVM(@NativeType("void *") long value) { napplicationVM(address(), value); return this; }
    /** Sets the specified value to the {@code applicationActivity} field. */
    public XrInstanceCreateInfoAndroidKHR applicationActivity(@NativeType("void *") long value) { napplicationActivity(address(), value); return this; }

    /** Initializes this struct with the specified values. */
    public XrInstanceCreateInfoAndroidKHR set(
        int type,
        long next,
        long applicationVM,
        long applicationActivity
    ) {
        type(type);
        next(next);
        applicationVM(applicationVM);
        applicationActivity(applicationActivity);

        return this;
    }

    /**
     * Copies the specified struct data to this struct.
     *
     * @param src the source struct
     *
     * @return this struct
     */
    public XrInstanceCreateInfoAndroidKHR set(XrInstanceCreateInfoAndroidKHR src) {
        memCopy(src.address(), address(), SIZEOF);
        return this;
    }

    // -----------------------------------

    /** Returns a new {@code XrInstanceCreateInfoAndroidKHR} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed. */
    public static XrInstanceCreateInfoAndroidKHR malloc() {
        return new XrInstanceCreateInfoAndroidKHR(nmemAllocChecked(SIZEOF), null);
    }

    /** Returns a new {@code XrInstanceCreateInfoAndroidKHR} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed. */
    public static XrInstanceCreateInfoAndroidKHR calloc() {
        return new XrInstanceCreateInfoAndroidKHR(nmemCallocChecked(1, SIZEOF), null);
    }

    /** Returns a new {@code XrInstanceCreateInfoAndroidKHR} instance allocated with {@link BufferUtils}. */
    public static XrInstanceCreateInfoAndroidKHR create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new XrInstanceCreateInfoAndroidKHR(memAddress(container), container);
    }

    /** Returns a new {@code XrInstanceCreateInfoAndroidKHR} instance for the specified memory address. */
    public static XrInstanceCreateInfoAndroidKHR create(long address) {
        return new XrInstanceCreateInfoAndroidKHR(address, null);
    }

    /** Like {@link #create(long) create}, but returns {@code null} if {@code address} is {@code NULL}. */
    public static @Nullable XrInstanceCreateInfoAndroidKHR createSafe(long address) {
        return address == NULL ? null : new XrInstanceCreateInfoAndroidKHR(address, null);
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

    /**
     * Returns a new {@code XrInstanceCreateInfoAndroidKHR} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static XrInstanceCreateInfoAndroidKHR malloc(MemoryStack stack) {
        return new XrInstanceCreateInfoAndroidKHR(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    /**
     * Returns a new {@code XrInstanceCreateInfoAndroidKHR} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static XrInstanceCreateInfoAndroidKHR calloc(MemoryStack stack) {
        return new XrInstanceCreateInfoAndroidKHR(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
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
    public static int ntype(long struct) { return memGetInt(struct + XrInstanceCreateInfoAndroidKHR.TYPE); }
    /** Unsafe version of {@link #next}. */
    public static long nnext(long struct) { return memGetAddress(struct + XrInstanceCreateInfoAndroidKHR.NEXT); }
    /** Unsafe version of {@link #applicationVM}. */
    public static long napplicationVM(long struct) { return memGetAddress(struct + XrInstanceCreateInfoAndroidKHR.APPLICATIONVM); }
    /** Unsafe version of {@link #applicationActivity}. */
    public static long napplicationActivity(long struct) { return memGetAddress(struct + XrInstanceCreateInfoAndroidKHR.APPLICATIONACTIVITY); }

    /** Unsafe version of {@link #type(int) type}. */
    public static void ntype(long struct, int value) { memPutInt(struct + XrInstanceCreateInfoAndroidKHR.TYPE, value); }
    /** Unsafe version of {@link #next(long) next}. */
    public static void nnext(long struct, long value) { memPutAddress(struct + XrInstanceCreateInfoAndroidKHR.NEXT, value); }
    /** Unsafe version of {@link #applicationVM(long) applicationVM}. */
    public static void napplicationVM(long struct, long value) { memPutAddress(struct + XrInstanceCreateInfoAndroidKHR.APPLICATIONVM, check(value)); }
    /** Unsafe version of {@link #applicationActivity(long) applicationActivity}. */
    public static void napplicationActivity(long struct, long value) { memPutAddress(struct + XrInstanceCreateInfoAndroidKHR.APPLICATIONACTIVITY, check(value)); }

    /**
     * Validates pointer members that should not be {@code NULL}.
     *
     * @param struct the struct to validate
     */
    public static void validate(long struct) {
        check(memGetAddress(struct + XrInstanceCreateInfoAndroidKHR.APPLICATIONVM));
        check(memGetAddress(struct + XrInstanceCreateInfoAndroidKHR.APPLICATIONACTIVITY));
    }

    // -----------------------------------

    /** An array of {@link XrInstanceCreateInfoAndroidKHR} structs. */
    public static class Buffer extends StructBuffer<XrInstanceCreateInfoAndroidKHR, Buffer> implements NativeResource {

        private static final XrInstanceCreateInfoAndroidKHR ELEMENT_FACTORY = XrInstanceCreateInfoAndroidKHR.create(-1L);

        /**
         * Creates a new {@code XrInstanceCreateInfoAndroidKHR.Buffer} instance backed by the specified container.
         *
         * <p>Changes to the container's content will be visible to the struct buffer instance and vice versa. The two buffers' position, limit, and mark values
         * will be independent. The new buffer's position will be zero, its capacity and its limit will be the number of bytes remaining in this buffer divided
         * by {@link XrInstanceCreateInfoAndroidKHR#SIZEOF}, and its mark will be undefined.</p>
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
        protected XrInstanceCreateInfoAndroidKHR getElementFactory() {
            return ELEMENT_FACTORY;
        }

        /** @return the value of the {@code type} field. */
        @NativeType("XrStructureType")
        public int type() { return XrInstanceCreateInfoAndroidKHR.ntype(address()); }
        /** @return the value of the {@code next} field. */
        @NativeType("void const *")
        public long next() { return XrInstanceCreateInfoAndroidKHR.nnext(address()); }
        /** @return the value of the {@code applicationVM} field. */
        @NativeType("void *")
        public long applicationVM() { return XrInstanceCreateInfoAndroidKHR.napplicationVM(address()); }
        /** @return the value of the {@code applicationActivity} field. */
        @NativeType("void *")
        public long applicationActivity() { return XrInstanceCreateInfoAndroidKHR.napplicationActivity(address()); }

        /** Sets the specified value to the {@code type} field. */
        public Buffer type(@NativeType("XrStructureType") int value) { XrInstanceCreateInfoAndroidKHR.ntype(address(), value); return this; }
        /** Sets the {@link KHRAndroidCreateInstance#XR_TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR} value to the {@code type} field. */
        public Buffer type$Default() { return type(KHRAndroidCreateInstance.XR_TYPE_INSTANCE_CREATE_INFO_ANDROID_KHR); }
        /** Sets the specified value to the {@code next} field. */
        public Buffer next(@NativeType("void const *") long value) { XrInstanceCreateInfoAndroidKHR.nnext(address(), value); return this; }
        /** Sets the specified value to the {@code applicationVM} field. */
        public Buffer applicationVM(@NativeType("void *") long value) { XrInstanceCreateInfoAndroidKHR.napplicationVM(address(), value); return this; }
        /** Sets the specified value to the {@code applicationActivity} field. */
        public Buffer applicationActivity(@NativeType("void *") long value) { XrInstanceCreateInfoAndroidKHR.napplicationActivity(address(), value); return this; }

    }

}
