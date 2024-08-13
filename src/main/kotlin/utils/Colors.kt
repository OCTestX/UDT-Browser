package utils

import androidx.compose.material.lightColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Colors {
    object ThemeColorScheme {
        private val blueColorScheme = lightColorScheme(
            primary = Color(0xFF3F51B5), // 深蓝色
            onPrimary = Color.White, // 主色上的文字颜色
            primaryContainer = Color(0xFFE8EAF6), // 主色的容器背景
            onPrimaryContainer = Color(0xFF3F51B5), // 主色容器上的文字颜色
            secondary = Color(0xFF03A9F4), // 辅助色
            onSecondary = Color.White, // 辅助色上的文字颜色
            secondaryContainer = Color(0xFFDFF4FF), // 辅助色的容器背景
            onSecondaryContainer = Color(0xFF03A9F4), // 辅助色容器上的文字颜色
            tertiary = Color(0xFF00BCD4), // 第三色
            onTertiary = Color.White, // 第三色上的文字颜色
            tertiaryContainer = Color(0xFFE0F7FA), // 第三色的容器背景
            onTertiaryContainer = Color(0xFF00BCD4), // 第三色容器上的文字颜色
            error = Color(0xFFB71C1C), // 错误色
            onError = Color.White, // 错误色上的文字颜色
            errorContainer = Color(0xFFFFCDD2), // 错误色的容器背景
            onErrorContainer = Color(0xFFB71C1C), // 错误色容器上的文字颜色
            background = Color.White, // 背景色
            onBackground = Color.Black, // 背景上的文字颜色
            surface = Color.White, // 表面色
            onSurface = Color.Black, // 表面色上的文字颜色
            inverseSurface = Color.Black, // 反转表面色
            inverseOnSurface = Color.White, // 反转表面色上的文字颜色
            inversePrimary = Color.White // 反转主色
        )
        private val yellowColorScheme = lightColorScheme(
            primary = Color(0xFFCDDC39), // 黄绿色
            onPrimary = Color.Black, // 主色上的文字颜色
            primaryContainer = Color(0xFFF9F5D7), // 主色的容器背景
            onPrimaryContainer = Color(0xFFCDDC39), // 主色容器上的文字颜色
            secondary = Color(0xFFFFEB3B), // 辅助色
            onSecondary = Color.Black, // 辅助色上的文字颜色
            secondaryContainer = Color(0xFFFFF176), // 辅助色的容器背景
            onSecondaryContainer = Color(0xFFFFEB3B), // 辅助色容器上的文字颜色
            tertiary = Color(0xFFFFC107), // 第三色
            onTertiary = Color.Black, // 第三色上的文字颜色
            tertiaryContainer = Color(0xFFFFE0B2), // 第三色的容器背景
            onTertiaryContainer = Color(0xFFFFC107), // 第三色容器上的文字颜色
            error = Color(0xFFB71C1C), // 错误色
            onError = Color.White, // 错误色上的文字颜色
            errorContainer = Color(0xFFFFCDD2), // 错误色的容器背景
            onErrorContainer = Color(0xFFB71C1C), // 错误色容器上的文字颜色
            background = Color.White, // 背景色
            onBackground = Color.Black, // 背景上的文字颜色
            surface = Color.White, // 表面色
            onSurface = Color.Black, // 表面色上的文字颜色
            inverseSurface = Color.Black, // 反转表面色
            inverseOnSurface = Color.White, // 反转表面色上的文字颜色
            inversePrimary = Color.Black // 反转主色
        )
        private val greenColorScheme = lightColorScheme(
            primary = Color(0xFF4CAF50), // 深绿色
            onPrimary = Color.White, // 主色上的文字颜色
            primaryContainer = Color(0xFFC8E6C9), // 主色的容器背景
            onPrimaryContainer = Color(0xFF4CAF50), // 主色容器上的文字颜色
            secondary = Color(0xFF8BC34A), // 辅助色
            onSecondary = Color.White, // 辅助色上的文字颜色
            secondaryContainer = Color(0xFFF1F8E9), // 辅助色的容器背景
            onSecondaryContainer = Color(0xFF8BC34A), // 辅助色容器上的文字颜色
            tertiary = Color(0xFF009688), // 第三色
            onTertiary = Color.White, // 第三色上的文字颜色
            tertiaryContainer = Color(0xFFE0F2F1), // 第三色的容器背景
            onTertiaryContainer = Color(0xFF009688), // 第三色容器上的文字颜色
            error = Color(0xFFB71C1C), // 错误色
            onError = Color.White, // 错误色上的文字颜色
            errorContainer = Color(0xFFFFCDD2), // 错误色的容器背景
            onErrorContainer = Color(0xFFB71C1C), // 错误色容器上的文字颜色
            background = Color.White, // 背景色
            onBackground = Color.Black, // 背景上的文字颜色
            surface = Color.White, // 表面色
            onSurface = Color.Black, // 表面色上的文字颜色
            inverseSurface = Color.Black, // 反转表面色
            inverseOnSurface = Color.White, // 反转表面色上的文字颜色
            inversePrimary = Color.White // 反转主色
        )
        private val pinkColorScheme = lightColorScheme(
            primary = Color(0xFFE91E63), // 粉红色
            onPrimary = Color.White, // 主色上的文字颜色
            primaryContainer = Color(0xFFFFCDD2), // 主色的容器背景
            onPrimaryContainer = Color(0xFFE91E63), // 主色容器上的文字颜色
            secondary = Color(0xFFFF5252), // 辅助色
            onSecondary = Color.White, // 辅助色上的文字颜色
            secondaryContainer = Color(0xFFFFEBEE), // 辅助色的容器背景
            onSecondaryContainer = Color(0xFFFF5252), // 辅助色容器上的文字颜色
            tertiary = Color(0xFFFF80AB), // 第三色
            onTertiary = Color.Black, // 第三色上的文字颜色
            tertiaryContainer = Color(0xFFFFD8E6), // 第三色的容器背景
            onTertiaryContainer = Color(0xFFFF80AB), // 第三色容器上的文字颜色
            error = Color(0xFFB71C1C), // 错误色
            onError = Color.White, // 错误色上的文字颜色
            errorContainer = Color(0xFFFFCDD2), // 错误色的容器背景
            onErrorContainer = Color(0xFFB71C1C), // 错误色容器上的文字颜色
            background = Color.White, // 背景色
            onBackground = Color.Black, // 背景上的文字颜色
            surface = Color.White, // 表面色
            onSurface = Color.Black, // 表面色上的文字颜色
            inverseSurface = Color.Black, // 反转表面色
            inverseOnSurface = Color.White, // 反转表面色上的文字颜色
            inversePrimary = Color.White // 反转主色
        )
        private val redColorScheme = lightColorScheme(
            primary = Color(0xFFC62828), // 深红色
            onPrimary = Color.White, // 主色上的文字颜色
            primaryContainer = Color(0xFFFFD3D3), // 主色的容器背景
            onPrimaryContainer = Color(0xFFC62828), // 主色容器上的文字颜色
            secondary = Color(0xFFFF4444), // 辅助色
            onSecondary = Color.White, // 辅助色上的文字颜色
            secondaryContainer = Color(0xFFFFEBEE), // 辅助色的容器背景
            onSecondaryContainer = Color(0xFFFF4444), // 辅助色容器上的文字颜色
            tertiary = Color(0xFFFF5722), // 第三色
            onTertiary = Color.White, // 第三色上的文字颜色
            tertiaryContainer = Color(0xFFFFD7C6), // 第三色的容器背景
            onTertiaryContainer = Color(0xFFFF5722), // 第三色容器上的文字颜色
            error = Color(0xFFB71C1C), // 错误色
            onError = Color.White, // 错误色上的文字颜色
            errorContainer = Color(0xFFFFCDD2), // 错误色的容器背景
            onErrorContainer = Color(0xFFB71C1C), // 错误色容器上的文字颜色
            background = Color.White, // 背景色
            onBackground = Color.Black, // 背景上的文字颜色
            surface = Color.White, // 表面色
            onSurface = Color.Black, // 表面色上的文字颜色
            inverseSurface = Color.Black, // 反转表面色
            inverseOnSurface = Color.White, // 反转表面色上的文字颜色
            inversePrimary = Color.White // 反转主色
        )
        private val tealColorScheme = lightColorScheme(
            primary = Color(0xFF0097A7), // 青色
            onPrimary = Color.White, // 主色上的文字颜色
            primaryContainer = Color(0xFFE0F7FA), // 主色的容器背景
            onPrimaryContainer = Color(0xFF0097A7), // 主色容器上的文字颜色
            secondary = Color(0xFF26A69A), // 辅助色
            onSecondary = Color.White, // 辅助色上的文字颜色
            secondaryContainer = Color(0xFFD2F0EC), // 辅助色的容器背景
            onSecondaryContainer = Color(0xFF26A69A), // 辅助色容器上的文字颜色
            tertiary = Color(0xFF00796B), // 第三色
            onTertiary = Color.White, // 第三色上的文字颜色
            tertiaryContainer = Color(0xFFC8E6D9), // 第三色的容器背景
            onTertiaryContainer = Color(0xFF00796B), // 第三色容器上的文字颜色
            error = Color(0xFFB71C1C), // 错误色
            onError = Color.White, // 错误色上的文字颜色
            errorContainer = Color(0xFFFFCDD2), // 错误色的容器背景
            onErrorContainer = Color(0xFFB71C1C), // 错误色容器上的文字颜色
            background = Color.White, // 背景色
            onBackground = Color.Black, // 背景上的文字颜色
            surface = Color.White, // 表面色
            onSurface = Color.Black, // 表面色上的文字颜色
            inverseSurface = Color.Black, // 反转表面色
            inverseOnSurface = Color.White, // 反转表面色上的文字颜色
            inversePrimary = Color.White // 反转主色
        )
        private val orangeColorScheme = lightColorScheme(
            primary = Color(0xFFF57C00), // 橙色
            onPrimary = Color.White, // 主色上的文字颜色
            primaryContainer = Color(0xFFFFD180), // 主色的容器背景
            onPrimaryContainer = Color(0xFFF57C00), // 主色容器上的文字颜色
            secondary = Color(0xFFFFA726), // 辅助色
            onSecondary = Color.White, // 辅助色上的文字颜色
            secondaryContainer = Color(0xFFFFE0B2), // 辅助色的容器背景
            onSecondaryContainer = Color(0xFFFFA726), // 辅助色容器上的文字颜色
            tertiary = Color(0xFFFFC107), // 第三色
            onTertiary = Color.Black, // 第三色上的文字颜色
            tertiaryContainer = Color(0xFFFFE599), // 第三色的容器背景
            onTertiaryContainer = Color(0xFFFFC107), // 第三色容器上的文字颜色
            error = Color(0xFFB71C1C), // 错误色
            onError = Color.White, // 错误色上的文字颜色
            errorContainer = Color(0xFFFFCDD2), // 错误色的容器背景
            onErrorContainer = Color(0xFFB71C1C), // 错误色容器上的文字颜色
            background = Color.White, // 背景色
            onBackground = Color.Black, // 背景上的文字颜色
            surface = Color.White, // 表面色
            onSurface = Color.Black, // 表面色上的文字颜色
            inverseSurface = Color.Black, // 反转表面色
            inverseOnSurface = Color.White, // 反转表面色上的文字颜色
            inversePrimary = Color.White // 反转主色
        )




        val schemes = listOf(
            lightColorScheme(),
            darkColorScheme(),
            blueColorScheme,
            yellowColorScheme,
            greenColorScheme,
            pinkColorScheme,
            redColorScheme,
            tealColorScheme,
            orangeColorScheme
        )
    }
}