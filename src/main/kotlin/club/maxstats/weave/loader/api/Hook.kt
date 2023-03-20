package club.maxstats.weave.loader.api

import org.objectweb.asm.tree.ClassNode

abstract class Hook(val targetClassName: List<String>) {
    abstract fun transform(node: ClassNode, cfg: AssemblerConfig)

    abstract class AssemblerConfig {
        abstract fun computeFrames()
    }
}
