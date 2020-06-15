package us.dragonma.backgrounds.update

import java.awt.GraphicsEnvironment
import java.awt.HeadlessException

internal object GraphicsEnvironmentExt {
    val isReallyHeadless: Boolean
        get() = if (GraphicsEnvironment.isHeadless()) {
            true
        } else try {
            val screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
            screenDevices == null || screenDevices.isEmpty()
        } catch (e: HeadlessException) {
            true
        }
}
