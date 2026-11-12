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

import static org.lwjgl.system.MemoryUtil.*;

/**
 * <pre>{@code
 * struct XrGraphicsRequirementsOpenGLESKHR {
 *     XrStructureType type;
 *     void * next;
 *     XrVersion minApiVersionSupported;
 *     XrVersion maxApiVersionSupported;
 * }}</pre>
 */
public class XrGraphicsRequirementsOpenGLESKHR extends Struct<XrGraphicsRequirementsOpenGLESKHR> implements NativeResource {

    /** The struct size in bytes. */
    public static final int SIZEOF;

    /** The struct alignment in bytes. */
    public static final int ALIGNOF;

    /** The struct member offsets. */
    public static final int
        TYPE,
        NEXT,
        MINAPIVERSIONSUPPORTED,
        MAXAPIVERSIONSUPPORTED;

    static {
        Layout layout = __struct(
            __member(4),
            __member(POINTER_SIZE),
            __member(8),
            __member(8)
        );

        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();

        TYPE = layout.offsetof(0);
        NEXT = layout.offsetof(1);
        MINAPIVERSIONSUPPORTED = layout.offsetof(2);
        MAXAPIVERSIONSUPPORTED = layout.offsetof(3);
    }

    protected XrGraphicsRequirementsOpenGLESKHR(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected XrGraphicsRequirementsOpenGLESKHR create(long address, @Nullable ByteBuffer container) {
        return new XrGraphicsRequirementsOpenGLESKHR(address, container);
    }

    /**
     * Creates a {@code XrGraphicsRequirementsOpenGLESKHR} instance at the current position of the specified {@link ByteBuffer} container. Changes to the buffer's content will be
     * visible to the struct instance and vice versa.
     *
     * <p>The created instance holds a strong reference to the container object.</p>
     */
    public XrGraphicsRequirementsOpenGLESKHR(ByteBuffer container) {
        super(memAddress(container), __checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() { return SIZEOF; }

    /** @return the value of the {@code type} field. */
    @NativeType("XrStructureType")
    public int type() { return ntype(address()); }
    /** @return the value of the {@code next} field. */
    @NativeType("void *")
    public long next() { return nnext(address()); }
    /** @return the value of the {@code minApiVersionSupported} field. */
    @NativeType("XrVersion")
    public long minApiVersionSupported() { return nminApiVersionSupported(address()); }
    /** @return the value of the {@code maxApiVersionSupported} field. */
    @NativeType("XrVersion")
    public long maxApiVersionSupported() { return nmaxApiVersionSupported(address()); }

    /** Sets the specified value to the {@code type} field. */
    public XrGraphicsRequirementsOpenGLESKHR type(@NativeType("XrStructureType") int value) { ntype(address(), value); return this; }
    /** Sets the {@link KHROpenGLESEnable#XR_TYPE_GRAPHICS_REQUIREMENTS_OPENGL_ES_KHR TYPE_GRAPHICS_REQUIREMENTS_OPENGL_ES_KHR} value to the {@code type} field. */
    public XrGraphicsRequirementsOpenGLESKHR type$Default() { return type(KHROpenGLESEnable.XR_TYPE_GRAPHICS_REQUIREMENTS_OPENGL_ES_KHR); }
    /** Sets the specified value to the {@code next} field. */
    public XrGraphicsRequirementsOpenGLESKHR next(@NativeType("void *") long value) { nnext(address(), value); return this; }
    /** Sets the specified value to the {@code minApiVersionSupported} field. */
    public XrGraphicsRequirementsOpenGLESKHR minApiVersionSupported(@NativeType("XrVersion") long value) { nminApiVersionSupported(address(), value); return this; }
    /** Sets the specified value to the {@code maxApiVersionSupported} field. */
    public XrGraphicsRequirementsOpenGLESKHR maxApiVersionSupported(@NativeType("XrVersion") long value) { nmaxApiVersionSupported(address(), value); return this; }

    /** Initializes this struct with the specified values. */
    public XrGraphicsRequirementsOpenGLESKHR set(
        int type,
        long next,
        long minApiVersionSupported,
        long maxApiVersionSupported
    ) {
        type(type);
        next(next);
        minApiVersionSupported(minApiVersionSupported);
        maxApiVersionSupported(maxApiVersionSupported);

        return this;
    }

    /**
     * Copies the specified struct data to this struct.
     *
     * @param src the source struct
     *
     * @return this struct
     */
    public XrGraphicsRequirementsOpenGLESKHR set(XrGraphicsRequirementsOpenGLESKHR src) {
        memCopy(src.address(), address(), SIZEOF);
        return this;
    }

    // -----------------------------------

    /** Returns a new {@code XrGraphicsRequirementsOpenGLESKHR} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed. */
    public static XrGraphicsRequirementsOpenGLESKHR malloc() {
        return new XrGraphicsRequirementsOpenGLESKHR(nmemAllocChecked(SIZEOF), null);
    }

    /** Returns a new {@code XrGraphicsRequirementsOpenGLESKHR} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed. */
    public static XrGraphicsRequirementsOpenGLESKHR calloc() {
        return new XrGraphicsRequirementsOpenGLESKHR(nmemCallocChecked(1, SIZEOF), null);
    }

    /** Returns a new {@code XrGraphicsRequirementsOpenGLESKHR} instance allocated with {@link BufferUtils}. */
    public static XrGraphicsRequirementsOpenGLESKHR create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new XrGraphicsRequirementsOpenGLESKHR(memAddress(container), container);
    }

    /** Returns a new {@code XrGraphicsRequirementsOpenGLESKHR} instance for the specified memory address. */
    public static XrGraphicsRequirementsOpenGLESKHR create(long address) {
        return new XrGraphicsRequirementsOpenGLESKHR(address, null);
    }

    /** Like {@link #create(long) create}, but returns {@code null} if {@code address} is {@code NULL}. */
    public static @Nullable XrGraphicsRequirementsOpenGLESKHR createSafe(long address) {
        return address == NULL ? null : new XrGraphicsRequirementsOpenGLESKHR(address, null);
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
     * Returns a new {@code XrGraphicsRequirementsOpenGLESKHR} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static XrGraphicsRequirementsOpenGLESKHR malloc(MemoryStack stack) {
        return new XrGraphicsRequirementsOpenGLESKHR(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    /**
     * Returns a new {@code XrGraphicsRequirementsOpenGLESKHR} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static XrGraphicsRequirementsOpenGLESKHR calloc(MemoryStack stack) {
        return new XrGraphicsRequirementsOpenGLESKHR(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
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
    public static int ntype(long struct) { return memGetInt(struct + XrGraphicsRequirementsOpenGLESKHR.TYPE); }
    /** Unsafe version of {@link #next}. */
    public static long nnext(long struct) { return memGetAddress(struct + XrGraphicsRequirementsOpenGLESKHR.NEXT); }
    /** Unsafe version of {@link #minApiVersionSupported}. */
    public static long nminApiVersionSupported(long struct) { return memGetLong(struct + XrGraphicsRequirementsOpenGLESKHR.MINAPIVERSIONSUPPORTED); }
    /** Unsafe version of {@link #maxApiVersionSupported}. */
    public static long nmaxApiVersionSupported(long struct) { return memGetLong(struct + XrGraphicsRequirementsOpenGLESKHR.MAXAPIVERSIONSUPPORTED); }

    /** Unsafe version of {@link #type(int) type}. */
    public static void ntype(long struct, int value) { memPutInt(struct + XrGraphicsRequirementsOpenGLESKHR.TYPE, value); }
    /** Unsafe version of {@link #next(long) next}. */
    public static void nnext(long struct, long value) { memPutAddress(struct + XrGraphicsRequirementsOpenGLESKHR.NEXT, value); }
    /** Unsafe version of {@link #minApiVersionSupported(long) minApiVersionSupported}. */
    public static void nminApiVersionSupported(long struct, long value) { memPutLong(struct + XrGraphicsRequirementsOpenGLESKHR.MINAPIVERSIONSUPPORTED, value); }
    /** Unsafe version of {@link #maxApiVersionSupported(long) maxApiVersionSupported}. */
    public static void nmaxApiVersionSupported(long struct, long value) { memPutLong(struct + XrGraphicsRequirementsOpenGLESKHR.MAXAPIVERSIONSUPPORTED, value); }

    // -----------------------------------

    /** An array of {@link XrGraphicsRequirementsOpenGLESKHR} structs. */
    public static class Buffer extends StructBuffer<XrGraphicsRequirementsOpenGLESKHR, Buffer> implements NativeResource {

        private static final XrGraphicsRequirementsOpenGLESKHR ELEMENT_FACTORY = XrGraphicsRequirementsOpenGLESKHR.create(-1L);

        /**
         * Creates a new {@code XrGraphicsRequirementsOpenGLESKHR.Buffer} instance backed by the specified container.
         *
         * <p>Changes to the container's content will be visible to the struct buffer instance and vice versa. The two buffers' position, limit, and mark values
         * will be independent. The new buffer's position will be zero, its capacity and its limit will be the number of bytes remaining in this buffer divided
         * by {@link XrGraphicsRequirementsOpenGLESKHR#SIZEOF}, and its mark will be undefined.</p>
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
        protected XrGraphicsRequirementsOpenGLESKHR getElementFactory() {
            return ELEMENT_FACTORY;
        }

        /** @return the value of the {@code type} field. */
        @NativeType("XrStructureType")
        public int type() { return XrGraphicsRequirementsOpenGLESKHR.ntype(address()); }
        /** @return the value of the {@code next} field. */
        @NativeType("void *")
        public long next() { return XrGraphicsRequirementsOpenGLESKHR.nnext(address()); }
        /** @return the value of the {@code minApiVersionSupported} field. */
        @NativeType("XrVersion")
        public long minApiVersionSupported() { return XrGraphicsRequirementsOpenGLESKHR.nminApiVersionSupported(address()); }
        /** @return the value of the {@code maxApiVersionSupported} field. */
        @NativeType("XrVersion")
        public long maxApiVersionSupported() { return XrGraphicsRequirementsOpenGLESKHR.nmaxApiVersionSupported(address()); }

        /** Sets the specified value to the {@code type} field. */
        public Buffer type(@NativeType("XrStructureType") int value) { XrGraphicsRequirementsOpenGLESKHR.ntype(address(), value); return this; }
        /** Sets the {@link KHROpenGLESEnable#XR_TYPE_GRAPHICS_REQUIREMENTS_OPENGL_ES_KHR TYPE_GRAPHICS_REQUIREMENTS_OPENGL_ES_KHR} value to the {@code type} field. */
        public Buffer type$Default() { return type(KHROpenGLESEnable.XR_TYPE_GRAPHICS_REQUIREMENTS_OPENGL_ES_KHR); }
        /** Sets the specified value to the {@code next} field. */
        public Buffer next(@NativeType("void *") long value) { XrGraphicsRequirementsOpenGLESKHR.nnext(address(), value); return this; }
        /** Sets the specified value to the {@code minApiVersionSupported} field. */
        public Buffer minApiVersionSupported(@NativeType("XrVersion") long value) { XrGraphicsRequirementsOpenGLESKHR.nminApiVersionSupported(address(), value); return this; }
        /** Sets the specified value to the {@code maxApiVersionSupported} field. */
        public Buffer maxApiVersionSupported(@NativeType("XrVersion") long value) { XrGraphicsRequirementsOpenGLESKHR.nmaxApiVersionSupported(address(), value); return this; }

    }

}
