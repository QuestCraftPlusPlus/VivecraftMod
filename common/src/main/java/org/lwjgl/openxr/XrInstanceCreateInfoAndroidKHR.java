package org.lwjgl.openxr;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

public class XrInstanceCreateInfoAndroidKHR extends Struct implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TYPE;
    public static final int NEXT;
    public static final int APPLICATIONVM;
    public static final int APPLICATIONACTIVITY;

    protected XrInstanceCreateInfoAndroidKHR(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    protected XrInstanceCreateInfoAndroidKHR create(long address, @Nullable ByteBuffer container) {
        return new XrInstanceCreateInfoAndroidKHR(address, container);
    }

    public XrInstanceCreateInfoAndroidKHR(ByteBuffer container) {
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
    public long applicationActivity() {
        return napplicationActivity(this.address());
    }

    public XrInstanceCreateInfoAndroidKHR type(@NativeType("XrStructureType") int value) {
        ntype(this.address(), value);
        return this;
    }

    public XrInstanceCreateInfoAndroidKHR type$Default() {
        return this.type(1000008000);
    }

    public XrInstanceCreateInfoAndroidKHR next(@NativeType("void const *") long value) {
        nnext(this.address(), value);
        return this;
    }

    public XrInstanceCreateInfoAndroidKHR applicationVM(@NativeType("void *") long value) {
        napplicationVM(this.address(), value);
        return this;
    }

    public XrInstanceCreateInfoAndroidKHR applicationActivity(@NativeType("void *") long value) {
        napplicationActivity(this.address(), value);
        return this;
    }

    public XrInstanceCreateInfoAndroidKHR set(int type, long next, long applicationVM, long applicationActivity) {
        this.type(type);
        this.next(next);
        this.applicationVM(applicationVM);
        this.applicationActivity(applicationActivity);
        return this;
    }

    public XrInstanceCreateInfoAndroidKHR set(XrInstanceCreateInfoAndroidKHR src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static XrInstanceCreateInfoAndroidKHR malloc() {
        return new XrInstanceCreateInfoAndroidKHR(MemoryUtil.nmemAllocChecked((long)SIZEOF), (ByteBuffer)null);
    }

    public static XrInstanceCreateInfoAndroidKHR calloc() {
        return new XrInstanceCreateInfoAndroidKHR(MemoryUtil.nmemCallocChecked(1L, (long)SIZEOF), (ByteBuffer)null);
    }

    public static XrInstanceCreateInfoAndroidKHR create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new XrInstanceCreateInfoAndroidKHR(MemoryUtil.memAddress(container), container);
    }

    public static XrInstanceCreateInfoAndroidKHR create(long address) {
        return new XrInstanceCreateInfoAndroidKHR(address, (ByteBuffer)null);
    }

    @Nullable
    public static XrInstanceCreateInfoAndroidKHR createSafe(long address) {
        return address == 0L ? null : new XrInstanceCreateInfoAndroidKHR(address, (ByteBuffer)null);
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

    public static XrInstanceCreateInfoAndroidKHR malloc(MemoryStack stack) {
        return new XrInstanceCreateInfoAndroidKHR(stack.nmalloc(ALIGNOF, SIZEOF), (ByteBuffer)null);
    }

    public static XrInstanceCreateInfoAndroidKHR calloc(MemoryStack stack) {
        return new XrInstanceCreateInfoAndroidKHR(stack.ncalloc(ALIGNOF, 1, SIZEOF), (ByteBuffer)null);
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

    public static long napplicationVM(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)APPLICATIONVM);
    }

    public static long napplicationActivity(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)APPLICATIONACTIVITY);
    }

    public static void ntype(long struct, int value) {
        UNSAFE.putInt((Object)null, struct + (long)TYPE, value);
    }

    public static void nnext(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)NEXT, value);
    }

    public static void napplicationVM(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)APPLICATIONVM, Checks.check(value));
    }

    public static void napplicationActivity(long struct, long value) {
        MemoryUtil.memPutAddress(struct + (long)APPLICATIONACTIVITY, Checks.check(value));
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)APPLICATIONVM));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)APPLICATIONACTIVITY));
    }

    static {
        Struct.Layout layout = __struct(new Struct.Member[]{__member(4), __member(POINTER_SIZE), __member(POINTER_SIZE), __member(POINTER_SIZE)});
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TYPE = layout.offsetof(0);
        NEXT = layout.offsetof(1);
        APPLICATIONVM = layout.offsetof(2);
        APPLICATIONACTIVITY = layout.offsetof(3);
    }

    public static class Buffer extends StructBuffer<XrInstanceCreateInfoAndroidKHR, Buffer> implements NativeResource {
        private static final XrInstanceCreateInfoAndroidKHR ELEMENT_FACTORY = XrInstanceCreateInfoAndroidKHR.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / XrInstanceCreateInfoAndroidKHR.SIZEOF);
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

        protected XrInstanceCreateInfoAndroidKHR getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType("XrStructureType")
        public int type() {
            return XrInstanceCreateInfoAndroidKHR.ntype(this.address());
        }

        @NativeType("void const *")
        public long next() {
            return XrInstanceCreateInfoAndroidKHR.nnext(this.address());
        }

        @NativeType("void *")
        public long applicationVM() {
            return XrInstanceCreateInfoAndroidKHR.napplicationVM(this.address());
        }

        @NativeType("void *")
        public long applicationActivity() {
            return XrInstanceCreateInfoAndroidKHR.napplicationActivity(this.address());
        }

        public Buffer type(@NativeType("XrStructureType") int value) {
            XrInstanceCreateInfoAndroidKHR.ntype(this.address(), value);
            return this;
        }

        public Buffer type$Default() {
            return this.type(1000008000);
        }

        public Buffer next(@NativeType("void const *") long value) {
            XrInstanceCreateInfoAndroidKHR.nnext(this.address(), value);
            return this;
        }

        public Buffer applicationVM(@NativeType("void *") long value) {
            XrInstanceCreateInfoAndroidKHR.napplicationVM(this.address(), value);
            return this;
        }

        public Buffer applicationActivity(@NativeType("void *") long value) {
            XrInstanceCreateInfoAndroidKHR.napplicationActivity(this.address(), value);
            return this;
        }
    }
}
