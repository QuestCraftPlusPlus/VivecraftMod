package org.lwjgl.openxr;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

public class XrLoaderInitInfoAndroidKHR extends Struct<XrLoaderInitInfoAndroidKHR> implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TYPE;
    public static final int NEXT;
    public static final int APPLICATIONVM;
    public static final int APPLICATIONCONTEXT;

    protected XrLoaderInitInfoAndroidKHR(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    protected XrLoaderInitInfoAndroidKHR create(long address, @Nullable ByteBuffer container) {
        return new XrLoaderInitInfoAndroidKHR(address, container);
    }

    public XrLoaderInitInfoAndroidKHR(ByteBuffer container) {
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

    @NativeType("void *")
    public long applicationVM() {
        return napplicationVM(this.address());
    }

    @NativeType("void *")
    public long applicationContext() {
        return napplicationContext(this.address());
    }

    public XrLoaderInitInfoAndroidKHR type(@NativeType("XrStructureType") int value) {
        ntype(this.address(), value);
        return this;
    }

    public XrLoaderInitInfoAndroidKHR type$Default() {
        return this.type(1000089000);
    }

    public XrLoaderInitInfoAndroidKHR next(@NativeType("void const *") long value) {
        nnext(this.address(), value);
        return this;
    }

    public XrLoaderInitInfoAndroidKHR applicationVM(@NativeType("void *") long value) {
        napplicationVM(this.address(), value);
        return this;
    }

    public XrLoaderInitInfoAndroidKHR applicationContext(@NativeType("void *") long value) {
        napplicationContext(this.address(), value);
        return this;
    }

    public XrLoaderInitInfoAndroidKHR set(int type, long next, long applicationVM, long applicationContext) {
        this.type(type);
        this.next(next);
        this.applicationVM(applicationVM);
        this.applicationContext(applicationContext);
        return this;
    }

    public XrLoaderInitInfoAndroidKHR set(XrLoaderInitInfoAndroidKHR src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static XrLoaderInitInfoAndroidKHR malloc() {
        return new XrLoaderInitInfoAndroidKHR(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static XrLoaderInitInfoAndroidKHR calloc() {
        return new XrLoaderInitInfoAndroidKHR(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static XrLoaderInitInfoAndroidKHR create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new XrLoaderInitInfoAndroidKHR(MemoryUtil.memAddress(container), container);
    }

    public static XrLoaderInitInfoAndroidKHR create(long address) {
        return new XrLoaderInitInfoAndroidKHR(address, null);
    }

    @Nullable
    public static XrLoaderInitInfoAndroidKHR createSafe(long address) {
        return address == 0L ? null : new XrLoaderInitInfoAndroidKHR(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
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

    public static XrLoaderInitInfoAndroidKHR malloc(MemoryStack stack) {
        return new XrLoaderInitInfoAndroidKHR(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static XrLoaderInitInfoAndroidKHR calloc(MemoryStack stack) {
        return new XrLoaderInitInfoAndroidKHR(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int ntype(long struct) {
        return UNSAFE.getInt(null, struct + (long)TYPE);
    }

    public static long nnext(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)NEXT);
    }

    public static long napplicationVM(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)APPLICATIONVM);
    }

    public static long napplicationContext(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)APPLICATIONCONTEXT);
    }

    public static void ntype(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)TYPE, value);
    }

    public static void nnext(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)NEXT, value);
    }

    public static void napplicationVM(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)APPLICATIONVM, Checks.check(value));
    }

    public static void napplicationContext(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)APPLICATIONCONTEXT, Checks.check(value));
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)APPLICATIONVM));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)APPLICATIONCONTEXT));
    }

    static {
        Struct.Layout layout = __struct(__member(4), __member(POINTER_SIZE), __member(POINTER_SIZE),
            __member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TYPE = layout.offsetof(0);
        NEXT = layout.offsetof(1);
        APPLICATIONVM = layout.offsetof(2);
        APPLICATIONCONTEXT = layout.offsetof(3);
    }

    public static class Buffer extends StructBuffer<XrLoaderInitInfoAndroidKHR, Buffer> implements NativeResource {
        private static final XrLoaderInitInfoAndroidKHR ELEMENT_FACTORY = XrLoaderInitInfoAndroidKHR.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / XrLoaderInitInfoAndroidKHR.SIZEOF);
        }

        public Buffer(long address, int cap) {
            super(address, null, -1, 0, cap, cap);
        }

        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {
            super(address, container, mark, pos, lim, cap);
        }

        protected Buffer self() {
            return this;
        }

        protected XrLoaderInitInfoAndroidKHR getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType("XrStructureType")
        public int type() {
            return XrLoaderInitInfoAndroidKHR.ntype(this.address());
        }

        @NativeType("void const *")
        public long next() {
            return XrLoaderInitInfoAndroidKHR.nnext(this.address());
        }

        @NativeType("void *")
        public long applicationVM() {
            return XrLoaderInitInfoAndroidKHR.napplicationVM(this.address());
        }

        @NativeType("void *")
        public long applicationContext() {
            return XrLoaderInitInfoAndroidKHR.napplicationContext(this.address());
        }

        public Buffer type(@NativeType("XrStructureType") int value) {
            XrLoaderInitInfoAndroidKHR.ntype(this.address(), value);
            return this;
        }

        public Buffer type$Default() {
            return this.type(1000089000);
        }

        public Buffer next(@NativeType("void const *") long value) {
            XrLoaderInitInfoAndroidKHR.nnext(this.address(), value);
            return this;
        }

        public Buffer applicationVM(@NativeType("void *") long value) {
            XrLoaderInitInfoAndroidKHR.napplicationVM(this.address(), value);
            return this;
        }

        public Buffer applicationContext(@NativeType("void *") long value) {
            XrLoaderInitInfoAndroidKHR.napplicationContext(this.address(), value);
            return this;
        }
    }
}
