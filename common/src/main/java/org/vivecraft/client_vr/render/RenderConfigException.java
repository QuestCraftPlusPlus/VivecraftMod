package org.vivecraft.client_vr.render;

import net.minecraft.network.chat.Component;

public class RenderConfigException extends Exception {
    public Component title;
    public Component error;

    public final boolean restartRequired;

    public RenderConfigException(Component title, Component error) {
        this(title, error, false);
    }

    public RenderConfigException(Component title, Component error, boolean restartRequired) {
        super(error.getString());
        this.title = title;
        this.error = error;
        this.restartRequired = restartRequired;
    }

    public String toString() {
        return this.getClass().getName() + ": " + this.error.getString();
    }
}
