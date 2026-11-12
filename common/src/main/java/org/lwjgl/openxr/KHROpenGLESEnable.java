/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * MACHINE GENERATED FILE, DO NOT EDIT
 */
package org.lwjgl.openxr;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeType;

import static org.lwjgl.system.Checks.CHECKS;
import static org.lwjgl.system.Checks.check;
import static org.lwjgl.system.JNI.callPJPI;

public class KHROpenGLESEnable {

    public static final int XR_KHR_opengl_es_enable_SPEC_VERSION = 8;

    public static final String XR_KHR_OPENGL_ES_ENABLE_EXTENSION_NAME = "XR_KHR_opengl_es_enable";

    public static final int
        XR_TYPE_GRAPHICS_BINDING_OPENGL_ES_ANDROID_KHR = 1000024001,
        XR_TYPE_SWAPCHAIN_IMAGE_OPENGL_ES_KHR          = 1000024002,
        XR_TYPE_GRAPHICS_REQUIREMENTS_OPENGL_ES_KHR    = 1000024003;

    protected KHROpenGLESEnable() {
        throw new UnsupportedOperationException();
    }

    // --- [ xrGetOpenGLESGraphicsRequirementsKHR ] ---

    /** {@code XrResult xrGetOpenGLESGraphicsRequirementsKHR(XrInstance instance, XrSystemId systemId, XrGraphicsRequirementsOpenGLESKHR * graphicsRequirements)} */
    private static long xrGetOpenGLESGraphicsRequirementsKHRPtr = 0x0;

    public static int nxrGetOpenGLESGraphicsRequirementsKHR(XrInstance instance, long systemId, long graphicsRequirements) {
        if(xrGetOpenGLESGraphicsRequirementsKHRPtr == 0x0) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer pfn = stack.callocPointer(1);
                XR10.xrGetInstanceProcAddr(instance, "xrGetOpenGLESGraphicsRequirementsKHR", pfn);
                xrGetOpenGLESGraphicsRequirementsKHRPtr = pfn.get(0);
            }
        }
        if (Checks.CHECKS) {
            Checks.check(xrGetOpenGLESGraphicsRequirementsKHRPtr);
        }

        return JNI.callPJPI(instance.address(), systemId, graphicsRequirements, xrGetOpenGLESGraphicsRequirementsKHRPtr);
    }

    /** {@code XrResult xrGetOpenGLESGraphicsRequirementsKHR(XrInstance instance, XrSystemId systemId, XrGraphicsRequirementsOpenGLESKHR * graphicsRequirements)} */
    @NativeType("XrResult")
    public static int xrGetOpenGLESGraphicsRequirementsKHR(XrInstance instance, @NativeType("XrSystemId") long systemId, @NativeType("XrGraphicsRequirementsOpenGLESKHR *") XrGraphicsRequirementsOpenGLESKHR graphicsRequirements) {
        return nxrGetOpenGLESGraphicsRequirementsKHR(instance, systemId, graphicsRequirements.address());
    }

}
