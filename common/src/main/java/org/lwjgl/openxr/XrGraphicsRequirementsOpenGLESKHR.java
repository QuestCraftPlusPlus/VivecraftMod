package org.lwjgl.openxr;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

public class XrGraphicsRequirementsOpenGLESKHR extends Struct<XrGraphicsRequirementsOpenGLESKHR> implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TYPE;
    public static final int NEXT;
    public static final int MINAPIVERSIONSUPPORTED;
    public static final int MAXAPIVERSIONSUPPORTED;

    protected XrGraphicsRequirementsOpenGLESKHR(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    protected XrGraphicsRequirementsOpenGLESKHR create(long address, @Nullable ByteBuffer container) {
        return new XrGraphicsRequirementsOpenGLESKHR(address, container);
    }

    public XrGraphicsRequirementsOpenGLESKHR(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), __checkContainer(container, SIZEOF));
    }

    public int sizeof() {
        return SIZEOF;
    }

    @NativeType("XrStructureType")
    public int type() {
        return ntype(this.address());
    }

    @NativeType("void *")
    public long next() {
        return nnext(this.address());
    }

    @NativeType("XrVersion")
    public long minApiVersionSupported() {
        return nminApiVersionSupported(this.address());
    }

    @NativeType("XrVersion")
    public long maxApiVersionSupported() {
        return nmaxApiVersionSupported(this.address());
    }

    public XrGraphicsRequirementsOpenGLESKHR type(@NativeType("XrStructureType") int value) {
        ntype(this.address(), value);
        return this;
    }

    public XrGraphicsRequirementsOpenGLESKHR type$Default() {
        return this.type(1000024003);
    }

    public XrGraphicsRequirementsOpenGLESKHR next(@NativeType("void *") long value) {
        nnext(this.address(), value);
        return this;
    }

    public XrGraphicsRequirementsOpenGLESKHR minApiVersionSupported(@NativeType("XrVersion") long value) {
        nminApiVersionSupported(this.address(), value);
        return this;
    }

    public XrGraphicsRequirementsOpenGLESKHR maxApiVersionSupported(@NativeType("XrVersion") long value) {
        nmaxApiVersionSupported(this.address(), value);
        return this;
    }

    public XrGraphicsRequirementsOpenGLESKHR set(int type, long next, long minApiVersionSupported, long maxApiVersionSupported) {
        this.type(type);
        this.next(next);
        this.minApiVersionSupported(minApiVersionSupported);
        this.maxApiVersionSupported(maxApiVersionSupported);
        return this;
    }

    public XrGraphicsRequirementsOpenGLESKHR set(XrGraphicsRequirementsOpenGLESKHR src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static XrGraphicsRequirementsOpenGLESKHR malloc() {
        return new XrGraphicsRequirementsOpenGLESKHR(MemoryUtil.nmemAllocChecked((long)SIZEOF), (ByteBuffer)null);
    }

    public static XrGraphicsRequirementsOpenGLESKHR calloc() {
        return new XrGraphicsRequirementsOpenGLESKHR(MemoryUtil.nmemCallocChecked(1L, (long)SIZEOF), (ByteBuffer)null);
    }

    public static XrGraphicsRequirementsOpenGLESKHR create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new XrGraphicsRequirementsOpenGLESKHR(MemoryUtil.memAddress(container), container);
    }

    public static XrGraphicsRequirementsOpenGLESKHR create(long address) {
        return new XrGraphicsRequirementsOpenGLESKHR(address, (ByteBuffer)null);
    }

    @Nullable
    public static XrGraphicsRequirementsOpenGLESKHR createSafe(long address) {
        return address == 0L ? null : new XrGraphicsRequirementsOpenGLESKHR(address, (ByteBuffer)null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked((long)capacity, (long)SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = __create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static XrGraphicsRequirementsOpenGLESKHR malloc(MemoryStack stack) {
        return new XrGraphicsRequirementsOpenGLESKHR(stack.nmalloc(ALIGNOF, SIZEOF), (ByteBuffer)null);
    }

    public static XrGraphicsRequirementsOpenGLESKHR calloc(MemoryStack stack) {
        return new XrGraphicsRequirementsOpenGLESKHR(stack.ncalloc(ALIGNOF, 1, SIZEOF), (ByteBuffer)null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int ntype(long struct) {
        return UNSAFE.getInt((Object)null, struct + (long)TYPE);
    }

    public static long nnext(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)NEXT);
    }

    public static long nminApiVersionSupported(long struct) {
        return UNSAFE.getLong((Object)null, struct + (long)MINAPIVERSIONSUPPORTED);
    }

    public static long nmaxApiVersionSupported(long struct) {
        return UNSAFE.getLong((Object)null, struct + (long)MAXAPIVERSIONSUPPORTED);
    }

    public static void ntype(long struct, int value) {
        UNSAFE.putInt((Object)null, struct + (long)TYPE, value);
    }

    public static void nnext(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)NEXT, value);
    }

    public static void nminApiVersionSupported(long struct, long value) {
        UNSAFE.putLong((Object)null, struct + (long)MINAPIVERSIONSUPPORTED, value);
    }

    public static void nmaxApiVersionSupported(long struct, long value) {
        UNSAFE.putLong((Object)null, struct + (long)MAXAPIVERSIONSUPPORTED, value);
    }

    static {
        Struct.Layout layout = __struct(new Struct.Member[]{__member(4), __member(POINTER_SIZE), __member(8), __member(8)});
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TYPE = layout.offsetof(0);
        NEXT = layout.offsetof(1);
        MINAPIVERSIONSUPPORTED = layout.offsetof(2);
        MAXAPIVERSIONSUPPORTED = layout.offsetof(3);
    }

    public static class Buffer extends StructBuffer<XrGraphicsRequirementsOpenGLESKHR, Buffer> implements NativeResource {
        private static final XrGraphicsRequirementsOpenGLESKHR ELEMENT_FACTORY = XrGraphicsRequirementsOpenGLESKHR.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / XrGraphicsRequirementsOpenGLESKHR.SIZEOF);
        }

        public Buffer(long address, int cap) {
            super(address, (ByteBuffer)null, -1, 0, cap, cap);
        }

        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {
            super(address, container, mark, pos, lim, cap);
        }

        protected Buffer self() {
            return this;
        }

        protected XrGraphicsRequirementsOpenGLESKHR getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType("XrStructureType")
        public int type() {
            return XrGraphicsRequirementsOpenGLESKHR.ntype(this.address());
        }

        @NativeType("void *")
        public long next() {
            return XrGraphicsRequirementsOpenGLESKHR.nnext(this.address());
        }

        @NativeType("XrVersion")
        public long minApiVersionSupported() {
            return XrGraphicsRequirementsOpenGLESKHR.nminApiVersionSupported(this.address());
        }

        @NativeType("XrVersion")
        public long maxApiVersionSupported() {
            return XrGraphicsRequirementsOpenGLESKHR.nmaxApiVersionSupported(this.address());
        }

        public Buffer type(@NativeType("XrStructureType") int value) {
            XrGraphicsRequirementsOpenGLESKHR.ntype(this.address(), value);
            return this;
        }

        public Buffer type$Default() {
            return this.type(1000024003);
        }

        public Buffer next(@NativeType("void *") long value) {
            XrGraphicsRequirementsOpenGLESKHR.nnext(this.address(), value);
            return this;
        }

        public Buffer minApiVersionSupported(@NativeType("XrVersion") long value) {
            XrGraphicsRequirementsOpenGLESKHR.nminApiVersionSupported(this.address(), value);
            return this;
        }

        public Buffer maxApiVersionSupported(@NativeType("XrVersion") long value) {
            XrGraphicsRequirementsOpenGLESKHR.nmaxApiVersionSupported(this.address(), value);
            return this;
        }
    }
}
