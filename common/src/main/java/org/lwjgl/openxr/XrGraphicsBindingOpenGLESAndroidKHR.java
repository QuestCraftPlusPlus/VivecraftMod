package org.lwjgl.openxr;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

public class XrGraphicsBindingOpenGLESAndroidKHR extends Struct<XrGraphicsBindingOpenGLESAndroidKHR> implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TYPE;
    public static final int NEXT;
    public static final int DISPLAY;
    public static final int CONFIG;
    public static final int CONTEXT;

    protected XrGraphicsBindingOpenGLESAndroidKHR(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    protected XrGraphicsBindingOpenGLESAndroidKHR create(long address, @Nullable ByteBuffer container) {
        return new XrGraphicsBindingOpenGLESAndroidKHR(address, container);
    }

    public XrGraphicsBindingOpenGLESAndroidKHR(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), __checkContainer(container, SIZEOF));
    }

    public int sizeof() {
        return SIZEOF;
    }

    @NativeType("XrStructureType")
    public int type() {
        return ntype(this.address());
    }

    @NativeType("void const *")
    public long next() {
        return nnext(this.address());
    }

    @NativeType("EGLDisplay")
    public long display() {
        return ndisplay(this.address());
    }

    @NativeType("EGLConfig")
    public long config() {
        return nconfig(this.address());
    }

    @NativeType("EGLContext")
    public long context() {
        return ncontext(this.address());
    }

    public XrGraphicsBindingOpenGLESAndroidKHR type(@NativeType("XrStructureType") int value) {
        ntype(this.address(), value);
        return this;
    }

    public XrGraphicsBindingOpenGLESAndroidKHR type$Default() {
        return this.type(1000024001);
    }

    public XrGraphicsBindingOpenGLESAndroidKHR next(@NativeType("void const *") long value) {
        nnext(this.address(), value);
        return this;
    }

    public XrGraphicsBindingOpenGLESAndroidKHR display(@NativeType("EGLDisplay") long value) {
        ndisplay(this.address(), value);
        return this;
    }

    public XrGraphicsBindingOpenGLESAndroidKHR config(@NativeType("EGLConfig") long value) {
        nconfig(this.address(), value);
        return this;
    }

    public XrGraphicsBindingOpenGLESAndroidKHR context(@NativeType("EGLContext") long value) {
        ncontext(this.address(), value);
        return this;
    }

    public XrGraphicsBindingOpenGLESAndroidKHR set(int type, long next, long display, long config, long context) {
        this.type(type);
        this.next(next);
        this.display(display);
        this.config(config);
        this.context(context);
        return this;
    }

    public XrGraphicsBindingOpenGLESAndroidKHR set(XrGraphicsBindingOpenGLESAndroidKHR src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static XrGraphicsBindingOpenGLESAndroidKHR malloc() {
        return new XrGraphicsBindingOpenGLESAndroidKHR(MemoryUtil.nmemAllocChecked((long)SIZEOF), (ByteBuffer)null);
    }

    public static XrGraphicsBindingOpenGLESAndroidKHR calloc() {
        return new XrGraphicsBindingOpenGLESAndroidKHR(MemoryUtil.nmemCallocChecked(1L, (long)SIZEOF), (ByteBuffer)null);
    }

    public static XrGraphicsBindingOpenGLESAndroidKHR create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new XrGraphicsBindingOpenGLESAndroidKHR(MemoryUtil.memAddress(container), container);
    }

    public static XrGraphicsBindingOpenGLESAndroidKHR create(long address) {
        return new XrGraphicsBindingOpenGLESAndroidKHR(address, (ByteBuffer)null);
    }

    @Nullable
    public static XrGraphicsBindingOpenGLESAndroidKHR createSafe(long address) {
        return address == 0L ? null : new XrGraphicsBindingOpenGLESAndroidKHR(address, (ByteBuffer)null);
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

    public static XrGraphicsBindingOpenGLESAndroidKHR malloc(MemoryStack stack) {
        return new XrGraphicsBindingOpenGLESAndroidKHR(stack.nmalloc(ALIGNOF, SIZEOF), (ByteBuffer)null);
    }

    public static XrGraphicsBindingOpenGLESAndroidKHR calloc(MemoryStack stack) {
        return new XrGraphicsBindingOpenGLESAndroidKHR(stack.ncalloc(ALIGNOF, 1, SIZEOF), (ByteBuffer)null);
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

    public static long ndisplay(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)DISPLAY);
    }

    public static long nconfig(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)CONFIG);
    }

    public static long ncontext(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)CONTEXT);
    }

    public static void ntype(long struct, int value) {
        UNSAFE.putInt((Object)null, struct + (long)TYPE, value);
    }

    public static void nnext(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)NEXT, value);
    }

    public static void ndisplay(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)DISPLAY, Checks.check(value));
    }

    public static void nconfig(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)CONFIG, Checks.check(value));
    }

    public static void ncontext(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)CONTEXT, Checks.check(value));
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)DISPLAY));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)CONFIG));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)CONTEXT));
    }

    static {
        Struct.Layout layout = __struct(new Struct.Member[]{__member(4), __member(POINTER_SIZE), __member(POINTER_SIZE), __member(POINTER_SIZE), __member(POINTER_SIZE)});
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TYPE = layout.offsetof(0);
        NEXT = layout.offsetof(1);
        DISPLAY = layout.offsetof(2);
        CONFIG = layout.offsetof(3);
        CONTEXT = layout.offsetof(4);
    }

    public static class Buffer extends StructBuffer<XrGraphicsBindingOpenGLESAndroidKHR, Buffer> implements NativeResource {
        private static final XrGraphicsBindingOpenGLESAndroidKHR ELEMENT_FACTORY = XrGraphicsBindingOpenGLESAndroidKHR.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / XrGraphicsBindingOpenGLESAndroidKHR.SIZEOF);
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

        protected XrGraphicsBindingOpenGLESAndroidKHR getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType("XrStructureType")
        public int type() {
            return XrGraphicsBindingOpenGLESAndroidKHR.ntype(this.address());
        }

        @NativeType("void const *")
        public long next() {
            return XrGraphicsBindingOpenGLESAndroidKHR.nnext(this.address());
        }

        @NativeType("EGLDisplay")
        public long display() {
            return XrGraphicsBindingOpenGLESAndroidKHR.ndisplay(this.address());
        }

        @NativeType("EGLConfig")
        public long config() {
            return XrGraphicsBindingOpenGLESAndroidKHR.nconfig(this.address());
        }

        @NativeType("EGLContext")
        public long context() {
            return XrGraphicsBindingOpenGLESAndroidKHR.ncontext(this.address());
        }

        public Buffer type(@NativeType("XrStructureType") int value) {
            XrGraphicsBindingOpenGLESAndroidKHR.ntype(this.address(), value);
            return this;
        }

        public Buffer type$Default() {
            return this.type(1000024001);
        }

        public Buffer next(@NativeType("void const *") long value) {
            XrGraphicsBindingOpenGLESAndroidKHR.nnext(this.address(), value);
            return this;
        }

        public Buffer display(@NativeType("EGLDisplay") long value) {
            XrGraphicsBindingOpenGLESAndroidKHR.ndisplay(this.address(), value);
            return this;
        }

        public Buffer config(@NativeType("EGLConfig") long value) {
            XrGraphicsBindingOpenGLESAndroidKHR.nconfig(this.address(), value);
            return this;
        }

        public Buffer context(@NativeType("EGLContext") long value) {
            XrGraphicsBindingOpenGLESAndroidKHR.ncontext(this.address(), value);
            return this;
        }
    }

}
