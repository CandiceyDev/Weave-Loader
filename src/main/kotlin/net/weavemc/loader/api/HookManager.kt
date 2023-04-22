package net.weavemc.loader.api

import net.weavemc.loader.bootstrap.SafeTransformer
import net.weavemc.loader.hooks.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.transformers.MixinClassWriter
import java.util.function.Consumer

public class HookManager {
    private val hooks = mutableListOf(
        ChatReceivedEventHook(),
        ChatSentEventHook(),
        EntityListEventAddHook(), EntityListEventRemoveHook(),
        GuiOpenEventHook(),
        KeyboardEventHook(),
        MouseEventHook(),
        PlayerListEventHook(),
        RenderGameOverlayHook(),
        RenderHandEventHook(),
        RenderLivingEventHook(),
        RenderWorldEventHook(),
        ServerConnectEventHook(),
        ShutdownEventHook(),
        ModInitializerHook(),
        TickEventHook(),
        PlayerTickEventHook(),
        WorldEventHook(),
        EntityJoinWorldEventHook(),
        PacketEventHook()
    )

    public fun register(vararg hooks: Hook) {
        this.hooks += hooks
    }

    public fun register(name: String, block: Consumer<ClassNode>) {
        hooks += object : Hook(name) {
            override fun transform(node: ClassNode, cfg: AssemblerConfig) {
                block.accept(node)
            }
        }
    }

    internal inner class Transformer : SafeTransformer {

        override fun transform(
            loader: ClassLoader,
            className: String,
            originalClass: ByteArray
        ): ByteArray? {
            val hooks = hooks.filter { it.targets.contains(className) }
            if (hooks.isEmpty()) return null

            val node = ClassNode()
            val reader = ClassReader(originalClass)
            reader.accept(node, 0)

            var computeFrames = false
            val cfg = Hook.AssemblerConfig { computeFrames = true }

            hooks.forEach { it.transform(node, cfg) }
            val flags = if (computeFrames) ClassWriter.COMPUTE_FRAMES else ClassWriter.COMPUTE_MAXS

            //HACK: use MixinClassWriter because it doesn't load classes when computing frames.
            val writer = MixinClassWriter(reader, flags)
            node.accept(writer)
            return writer.toByteArray()
        }
    }
}
