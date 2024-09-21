package org.vivecraft.util;

public class VLoader {
    static {
        System.loadLibrary("openvr_api");
    }

    public static native long getVKImage1();
    public static native long getVKImage2();
    public static native long getVKInstance();
    public static native long getVKPhysicalDevice();
    public static native long getVKDevice();
    public static native long getVKQueue();
    public static native int getVKQueueIndex();
}
