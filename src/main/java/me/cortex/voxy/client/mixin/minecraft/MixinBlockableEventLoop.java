package me.cortex.voxy.client.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.cortex.voxy.client.LoadException;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockableEventLoop.class)
public abstract class MixinBlockableEventLoop {

    //1.21.1 port: there is no isNonRecoverable, doRunTask just logs the exception, so rethrow from the log call
    @WrapOperation(method = "doRunTask", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private void voxy$forceCrashOnError(Logger logger, Marker marker, String msg, Object name, Object exception, Operation<Void> original) {
        if (exception instanceof LoadException le) {
            if (le.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw le;
        }
        original.call(logger, marker, msg, name, exception);
    }
}
