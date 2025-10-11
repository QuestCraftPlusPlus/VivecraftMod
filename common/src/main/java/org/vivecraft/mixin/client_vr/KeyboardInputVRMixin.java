package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.provider.control.VRInputAction;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.common.utils.MathUtils;

@Mixin(KeyboardInput.class)
public class KeyboardInputVRMixin extends ClientInput {

    @Final
    @Shadow
    private Options options;
    @Unique
    private boolean vivecraft$wasAutoSprint = false;
    @Unique
    private boolean vivecraft$wasAnalogMovement = false;

    @Unique
    private float vivecraft$axisToDigital(float value) {
        if (value > 0.5f) {
            return 1F;
        } else if (value < -0.5f) {
            return -1F;
        } else {
            return 0;
        }
    }

    @Unique
    private float vivecraft$getAxisValue(KeyMapping keyBinding) {
        return Math.abs(ClientDataHolderVR.getInstance().vr.getInputAction(keyBinding).getAxis1DUseTracked());
    }

    @WrapOperation(method = "tick", at = @At(value = "NEW", target = "net/minecraft/world/entity/player/Input"))
    private Input vivecraft$noMovementWhenClimbing(
        boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint,
        Operation<Input> original, @Share("climbing") LocalBooleanRef climbing)
    {
        if (VRState.VR_RUNNING) {
            climbing.set(!Minecraft.getInstance().player.isInWater() &&
                ClientDataHolderVR.getInstance().climbTracker.isClimbeyClimb() &&
                ClientDataHolderVR.getInstance().climbTracker.isGrabbingLadder());

            forward = (forward || VivecraftVRMod.INSTANCE.keyTeleportFallback.isDown()) && !climbing.get();
            backward &= !climbing.get();
            left &= !climbing.get();
            right &= !climbing.get();

            ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

            jump &= Minecraft.getInstance().screen == null && !climbing.get() &&
                (dataHolder.vrPlayer.getFreeMove() || dataHolder.vrSettings.simulateFalling);

            shift = Minecraft.getInstance().screen == null &&
                (dataHolder.sneakTracker.sneakCounter > 0 || dataHolder.sneakTracker.sneakOverride || shift);
        }
        return original.call(forward, backward, left, right, jump, shift, sprint);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void vivecraft$analogInput(CallbackInfo ci, @Share("climbing") LocalBooleanRef climbing) {
        if (!VRState.VR_RUNNING) return;

        boolean setMovement = false;
        float forwardAxis = 0.0F;

        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        float x = this.moveVector.x;
        float y = this.moveVector.y;

        if (!climbing.get() && !dataHolder.vrSettings.seated && Minecraft.getInstance().screen == null &&
            !KeyboardHandler.SHOWING)
        {
            // override everything
            Vector2fc moveStrafe = dataHolder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveStrafe)
                .getAxis2DUseTracked();
            Vector2fc moveRotate = dataHolder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveRotate)
                .getAxis2DUseTracked();

            if (moveStrafe.x() != 0.0F || moveStrafe.y() != 0.0F) {
                setMovement = true;
                forwardAxis = moveStrafe.y();

                if (dataHolder.vrSettings.analogMovement) {
                    y = moveStrafe.y();
                    x = -moveStrafe.x();
                } else {
                    y = this.vivecraft$axisToDigital(moveStrafe.y());
                    x = this.vivecraft$axisToDigital(-moveStrafe.x());
                }
            } else if (moveRotate.y() != 0.0F) {
                setMovement = true;
                forwardAxis = moveRotate.y();

                if (dataHolder.vrSettings.analogMovement) {
                    y = moveRotate.y();
                    // use left/right key as fallback
                    x = 0.0F;
                    x -= vivecraft$getAxisValue(this.options.keyRight);
                    x += vivecraft$getAxisValue(this.options.keyLeft);
                } else {
                    y = this.vivecraft$axisToDigital(moveRotate.y());
                }
            } else if (dataHolder.vrSettings.analogMovement) {
                // neither axis input active, use single key values
                setMovement = true;
                y = 0.0F;
                x = 0.0F;

                forwardAxis = vivecraft$getAxisValue(this.options.keyUp);
                if (forwardAxis == 0.0F) {
                    forwardAxis = vivecraft$getAxisValue(VivecraftVRMod.INSTANCE.keyTeleportFallback);
                }

                y += forwardAxis;
                y -= vivecraft$getAxisValue(this.options.keyDown);

                x -= vivecraft$getAxisValue(this.options.keyRight);
                x += vivecraft$getAxisValue(this.options.keyLeft);

                float deadZone = 0.05F;
                y = MathUtils.applyDeadzone(y, deadZone);
                x = MathUtils.applyDeadzone(x, deadZone);
            }

            if (setMovement) {
                this.vivecraft$wasAnalogMovement = true;
                // just assuming all this below is needed for compatibility.
                boolean forward = y > 0.0F;
                boolean backward = y < 0.0F;
                boolean left = x > 0.0F;
                boolean right = x < 0.0F;
                VRInputAction.setKeyBindState(this.options.keyUp, forward);
                VRInputAction.setKeyBindState(this.options.keyDown, backward);
                VRInputAction.setKeyBindState(this.options.keyLeft, left);
                VRInputAction.setKeyBindState(this.options.keyRight, right);

                // need to make a new one , since it is a record
                this.keyPresses = new Input(forward, backward, left, right, this.keyPresses.jump(),
                    this.keyPresses.shift(), this.keyPresses.sprint());

                if (dataHolder.vrSettings.autoSprint && !this.keyPresses.shift()) {
                    // Sprint only works for walk forwards obviously
                    if (forwardAxis >= dataHolder.vrSettings.autoSprintThreshold) {
                        Minecraft.getInstance().player.setSprinting(true);
                        this.vivecraft$wasAutoSprint = true;
                        y = 1.0F;
                    } else if (y > 0.0F && dataHolder.vrSettings.analogMovement) {
                        // Adjust range so you can still reach full speed while not sprinting
                        y = y / dataHolder.vrSettings.autoSprintThreshold;
                    }
                }
            }
        }

        if (!setMovement && this.vivecraft$wasAnalogMovement) {
            // stop movement when returning the stick to center
            VRInputAction.setKeyBindState(this.options.keyUp, false);
            VRInputAction.setKeyBindState(this.options.keyDown, false);
            VRInputAction.setKeyBindState(this.options.keyLeft, false);
            VRInputAction.setKeyBindState(this.options.keyRight, false);
        }
        this.vivecraft$wasAnalogMovement = setMovement;

        if (this.vivecraft$wasAutoSprint && forwardAxis < dataHolder.vrSettings.autoSprintThreshold) {
            // stop sprinting when below the threshold and sprinting was active
            Minecraft.getInstance().player.setSprinting(false);
            this.vivecraft$wasAutoSprint = false;
        }
        this.moveVector = new Vec2(x, y);
    }
}
