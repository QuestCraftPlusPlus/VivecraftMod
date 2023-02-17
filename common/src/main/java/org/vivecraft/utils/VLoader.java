package org.vivecraft.utils;

public class VLoader {
    static {
        System.loadLibrary("openvr_api");
    }

    public static native int createGLImage(int width, int height);
    public static native void writeImage(int tex, int width, int height, long byteBuf);
}
