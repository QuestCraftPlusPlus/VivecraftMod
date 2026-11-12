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
 * struct XrGraphicsBindingOpenGLESAndroidKHR {
 *     XrStructureType type;
 *     void const * next;
 *     EGLDisplay display;
 *     EGLConfig config;
 *     EGLContext context;
 * }}</pre>
 */
public class XrGraphicsBindingOpenGLESAndroidKHR extends Struct<XrGraphicsBindingOpenGLESAndroidKHR> implements NativeResource {

    /** The struct size in bytes. */
    public static final int SIZEOF;

    /** The struct alignment in bytes. */
    public static final int ALIGNOF;

    /** The struct member offsets. */
    public static final int
        TYPE,
        NEXT,
        DISPLAY,
        CONFIG,
        CONTEXT;

    static {
        Layout layout = __struct(
            __member(4),
            __member(POINTER_SIZE),
            __member(POINTER_SIZE),
            __member(POINTER_SIZE),
            __member(POINTER_SIZE)
        );

        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();

        TYPE = layout.offsetof(0);
        NEXT = layout.offsetof(1);
        DISPLAY = layout.offsetof(2);
        CONFIG = layout.offsetof(3);
        CONTEXT = layout.offsetof(4);
    }

    protected XrGraphicsBindingOpenGLESAndroidKHR(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected XrGraphicsBindingOpenGLESAndroidKHR create(long address, @Nullable ByteBuffer container) {
        return new XrGraphicsBindingOpenGLESAndroidKHR(address, container);
    }

    /**
     * Creates a {@code XrGraphicsBindingOpenGLESAndroidKHR} instance at the current position of the specified {@link ByteBuffer} container. Changes to the buffer's content will be
     * visible to the struct instance and vice versa.
     *
     * <p>The created instance holds a strong reference to the container object.</p>
     */
    public XrGraphicsBindingOpenGLESAndroidKHR(ByteBuffer container) {
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
    /** @return the value of the {@code display} field. */
    @NativeType("EGLDisplay")
    public long display() { return ndisplay(address()); }
    /** @return the value of the {@code config} field. */
    @NativeType("EGLConfig")
    public long config() { return nconfig(address()); }
    /** @return the value of the {@code context} field. */
    @NativeType("EGLContext")
    public long context() { return ncontext(address()); }

    /** Sets the specified value to the {@code type} field. */
    public XrGraphicsBindingOpenGLESAndroidKHR type(@NativeType("XrStructureType") int value) { ntype(address(), value); return this; }
    /** Sets the {@link KHROpenGLESEnable#XR_TYPE_GRAPHICS_BINDING_OPENGL_ES_ANDROID_KHR TYPE_GRAPHICS_BINDING_OPENGL_ES_ANDROID_KHR} value to the {@code type} field. */
    public XrGraphicsBindingOpenGLESAndroidKHR type$Default() { return type(KHROpenGLESEnable.XR_TYPE_GRAPHICS_BINDING_OPENGL_ES_ANDROID_KHR); }
    /** Sets the specified value to the {@code next} field. */
    public XrGraphicsBindingOpenGLESAndroidKHR next(@NativeType("void const *") long value) { nnext(address(), value); return this; }
    /** Sets the specified value to the {@code display} field. */
    public XrGraphicsBindingOpenGLESAndroidKHR display(@NativeType("EGLDisplay") long value) { ndisplay(address(), value); return this; }
    /** Sets the specified value to the {@code config} field. */
    public XrGraphicsBindingOpenGLESAndroidKHR config(@NativeType("EGLConfig") long value) { nconfig(address(), value); return this; }
    /** Sets the specified value to the {@code context} field. */
    public XrGraphicsBindingOpenGLESAndroidKHR context(@NativeType("EGLContext") long value) { ncontext(address(), value); return this; }

    /** Initializes this struct with the specified values. */
    public XrGraphicsBindingOpenGLESAndroidKHR set(
        int type,
        long next,
        long display,
        long config,
        long context
    ) {
        type(type);
        next(next);
        display(display);
        config(config);
        context(context);

        return this;
    }

    /**
     * Copies the specified struct data to this struct.
     *
     * @param src the source struct
     *
     * @return this struct
     */
    public XrGraphicsBindingOpenGLESAndroidKHR set(XrGraphicsBindingOpenGLESAndroidKHR src) {
        memCopy(src.address(), address(), SIZEOF);
        return this;
    }

    // -----------------------------------

    /** Returns a new {@code XrGraphicsBindingOpenGLESAndroidKHR} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed. */
    public static XrGraphicsBindingOpenGLESAndroidKHR malloc() {
        return new XrGraphicsBindingOpenGLESAndroidKHR(nmemAllocChecked(SIZEOF), null);
    }

    /** Returns a new {@code XrGraphicsBindingOpenGLESAndroidKHR} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed. */
    public static XrGraphicsBindingOpenGLESAndroidKHR calloc() {
        return new XrGraphicsBindingOpenGLESAndroidKHR(nmemCallocChecked(1, SIZEOF), null);
    }

    /** Returns a new {@code XrGraphicsBindingOpenGLESAndroidKHR} instance allocated with {@link BufferUtils}. */
    public static XrGraphicsBindingOpenGLESAndroidKHR create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new XrGraphicsBindingOpenGLESAndroidKHR(memAddress(container), container);
    }

    /** Returns a new {@code XrGraphicsBindingOpenGLESAndroidKHR} instance for the specified memory address. */
    public static XrGraphicsBindingOpenGLESAndroidKHR create(long address) {
        return new XrGraphicsBindingOpenGLESAndroidKHR(address, null);
    }

    /** Like {@link #create(long) create}, but returns {@code null} if {@code address} is {@code NULL}. */
    public static @Nullable XrGraphicsBindingOpenGLESAndroidKHR createSafe(long address) {
        return address == NULL ? null : new XrGraphicsBindingOpenGLESAndroidKHR(address, null);
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
     * Returns a new {@code XrGraphicsBindingOpenGLESAndroidKHR} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static XrGraphicsBindingOpenGLESAndroidKHR malloc(MemoryStack stack) {
        return new XrGraphicsBindingOpenGLESAndroidKHR(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    /**
     * Returns a new {@code XrGraphicsBindingOpenGLESAndroidKHR} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static XrGraphicsBindingOpenGLESAndroidKHR calloc(MemoryStack stack) {
        return new XrGraphicsBindingOpenGLESAndroidKHR(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
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
    public static int ntype(long struct) { return memGetInt(struct + XrGraphicsBindingOpenGLESAndroidKHR.TYPE); }
    /** Unsafe version of {@link #next}. */
    public static long nnext(long struct) { return memGetAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.NEXT); }
    /** Unsafe version of {@link #display}. */
    public static long ndisplay(long struct) { return memGetAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.DISPLAY); }
    /** Unsafe version of {@link #config}. */
    public static long nconfig(long struct) { return memGetAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.CONFIG); }
    /** Unsafe version of {@link #context}. */
    public static long ncontext(long struct) { return memGetAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.CONTEXT); }

    /** Unsafe version of {@link #type(int) type}. */
    public static void ntype(long struct, int value) { memPutInt(struct + XrGraphicsBindingOpenGLESAndroidKHR.TYPE, value); }
    /** Unsafe version of {@link #next(long) next}. */
    public static void nnext(long struct, long value) { memPutAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.NEXT, value); }
    /** Unsafe version of {@link #display(long) display}. */
    public static void ndisplay(long struct, long value) { memPutAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.DISPLAY, check(value)); }
    /** Unsafe version of {@link #config(long) config}. */
    public static void nconfig(long struct, long value) { memPutAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.CONFIG, check(value)); }
    /** Unsafe version of {@link #context(long) context}. */
    public static void ncontext(long struct, long value) { memPutAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.CONTEXT, check(value)); }

    /**
     * Validates pointer members that should not be {@code NULL}.
     *
     * @param struct the struct to validate
     */
    public static void validate(long struct) {
        check(memGetAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.DISPLAY));
        check(memGetAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.CONFIG));
        check(memGetAddress(struct + XrGraphicsBindingOpenGLESAndroidKHR.CONTEXT));
    }

    // -----------------------------------

    /** An array of {@link XrGraphicsBindingOpenGLESAndroidKHR} structs. */
    public static class Buffer extends StructBuffer<XrGraphicsBindingOpenGLESAndroidKHR, Buffer> implements NativeResource {

        private static final XrGraphicsBindingOpenGLESAndroidKHR ELEMENT_FACTORY = XrGraphicsBindingOpenGLESAndroidKHR.create(-1L);

        /**
         * Creates a new {@code XrGraphicsBindingOpenGLESAndroidKHR.Buffer} instance backed by the specified container.
         *
         * <p>Changes to the container's content will be visible to the struct buffer instance and vice versa. The two buffers' position, limit, and mark values
         * will be independent. The new buffer's position will be zero, its capacity and its limit will be the number of bytes remaining in this buffer divided
         * by {@link XrGraphicsBindingOpenGLESAndroidKHR#SIZEOF}, and its mark will be undefined.</p>
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
        protected XrGraphicsBindingOpenGLESAndroidKHR getElementFactory() {
            return ELEMENT_FACTORY;
        }

        /** @return the value of the {@code type} field. */
        @NativeType("XrStructureType")
        public int type() { return XrGraphicsBindingOpenGLESAndroidKHR.ntype(address()); }
        /** @return the value of the {@code next} field. */
        @NativeType("void const *")
        public long next() { return XrGraphicsBindingOpenGLESAndroidKHR.nnext(address()); }
        /** @return the value of the {@code display} field. */
        @NativeType("EGLDisplay")
        public long display() { return XrGraphicsBindingOpenGLESAndroidKHR.ndisplay(address()); }
        /** @return the value of the {@code config} field. */
        @NativeType("EGLConfig")
        public long config() { return XrGraphicsBindingOpenGLESAndroidKHR.nconfig(address()); }
        /** @return the value of the {@code context} field. */
        @NativeType("EGLContext")
        public long context() { return XrGraphicsBindingOpenGLESAndroidKHR.ncontext(address()); }

        /** Sets the specified value to the {@code type} field. */
        public Buffer type(@NativeType("XrStructureType") int value) { XrGraphicsBindingOpenGLESAndroidKHR.ntype(address(), value); return this; }
        /** Sets the {@link KHROpenGLESEnable#XR_TYPE_GRAPHICS_BINDING_OPENGL_ES_ANDROID_KHR TYPE_GRAPHICS_BINDING_OPENGL_ES_ANDROID_KHR} value to the {@code type} field. */
        public Buffer type$Default() { return type(KHROpenGLESEnable.XR_TYPE_GRAPHICS_BINDING_OPENGL_ES_ANDROID_KHR); }
        /** Sets the specified value to the {@code next} field. */
        public Buffer next(@NativeType("void const *") long value) { XrGraphicsBindingOpenGLESAndroidKHR.nnext(address(), value); return this; }
        /** Sets the specified value to the {@code display} field. */
        public Buffer display(@NativeType("EGLDisplay") long value) { XrGraphicsBindingOpenGLESAndroidKHR.ndisplay(address(), value); return this; }
        /** Sets the specified value to the {@code config} field. */
        public Buffer config(@NativeType("EGLConfig") long value) { XrGraphicsBindingOpenGLESAndroidKHR.nconfig(address(), value); return this; }
        /** Sets the specified value to the {@code context} field. */
        public Buffer context(@NativeType("EGLContext") long value) { XrGraphicsBindingOpenGLESAndroidKHR.ncontext(address(), value); return this; }

    }

}
