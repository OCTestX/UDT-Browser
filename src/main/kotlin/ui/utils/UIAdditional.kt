package ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

object UIAdditional {

    object Typographies {
        val DefaultTypography @Composable get() = MaterialTheme.typography

        private val MiSansTypographyFamily = FontFamily(
            Font(resource = "font/miSans/MiSans-Bold.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            Font(resource = "font/miSans/MiSans-Demibold.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            Font(resource = "font/miSans/MiSans-ExtraLight.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            Font(resource = "font/miSans/MiSans-Heavy.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            Font(resource = "font/miSans/MiSans-Light.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            Font(resource = "font/miSans/MiSans-Medium.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            Font(resource = "font/miSans/MiSans-Normal.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            Font(resource = "font/miSans/MiSans-Regular.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            Font(resource = "font/miSans/MiSans-Semibold.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            Font(resource = "font/miSans/MiSans-Thin.ttf", weight = FontWeight.Bold, style = FontStyle.Normal),
            )
//        val MiSansTypography @Composable get() = Typography(
//            displayLarge = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.displayLarge.fontWeight,
//                fontSize = DefaultTypography.displayLarge.fontSize,
//                lineHeight = DefaultTypography.displayLarge.lineHeight,
//                letterSpacing = DefaultTypography.displayLarge.letterSpacing,
//            ),
//            displayMedium = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.displayMedium.fontWeight,
//                fontSize = DefaultTypography.displayMedium.fontSize,
//                lineHeight = DefaultTypography.displayMedium.lineHeight,
//                letterSpacing = DefaultTypography.displayMedium.letterSpacing,
//            ),
//            displaySmall = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.displaySmall.fontWeight,
//                fontSize = DefaultTypography.displaySmall.fontSize,
//                lineHeight = DefaultTypography.displaySmall.lineHeight,
//                letterSpacing = DefaultTypography.displaySmall.letterSpacing,
//            ),
//            headlineLarge = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.headlineLarge.fontWeight,
//                fontSize = DefaultTypography.headlineLarge.fontSize,
//                lineHeight = DefaultTypography.headlineLarge.lineHeight,
//                letterSpacing = DefaultTypography.headlineLarge.letterSpacing,
//            ),
//            headlineMedium = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.headlineMedium.fontWeight,
//                fontSize = DefaultTypography.headlineMedium.fontSize,
//                lineHeight = DefaultTypography.headlineMedium.lineHeight,
//                letterSpacing = DefaultTypography.headlineMedium.letterSpacing,
//            ),
//            headlineSmall = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.headlineSmall.fontWeight,
//                fontSize = DefaultTypography.headlineSmall.fontSize,
//                lineHeight = DefaultTypography.headlineSmall.lineHeight,
//                letterSpacing = DefaultTypography.headlineSmall.letterSpacing,
//            ),
//            titleLarge = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.titleLarge.fontWeight,
//                fontSize = DefaultTypography.titleLarge.fontSize,
//                lineHeight = DefaultTypography.titleLarge.lineHeight,
//                letterSpacing = DefaultTypography.titleLarge.letterSpacing,
//            ),
//            titleMedium = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.titleMedium.fontWeight,
//                fontSize = DefaultTypography.titleMedium.fontSize,
//                lineHeight = DefaultTypography.titleMedium.lineHeight,
//                letterSpacing = DefaultTypography.titleMedium.letterSpacing,
//            ),
//            titleSmall = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.titleSmall.fontWeight,
//                fontSize = DefaultTypography.titleSmall.fontSize,
//                lineHeight = DefaultTypography.titleSmall.lineHeight,
//                letterSpacing = DefaultTypography.titleSmall.letterSpacing,
//            ),
//            bodyLarge = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.bodyLarge.fontWeight,
//                fontSize = DefaultTypography.bodyLarge.fontSize,
//                lineHeight = DefaultTypography.bodyLarge.lineHeight,
//                letterSpacing = DefaultTypography.bodyLarge.letterSpacing,
//            ),
//            bodyMedium = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.bodyMedium.fontWeight,
//                fontSize = DefaultTypography.bodyMedium.fontSize,
//                lineHeight = DefaultTypography.bodyMedium.lineHeight,
//                letterSpacing = DefaultTypography.bodyMedium.letterSpacing,
//            ),
//            bodySmall = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.bodySmall.fontWeight,
//                fontSize = DefaultTypography.bodySmall.fontSize,
//                lineHeight = DefaultTypography.bodySmall.lineHeight,
//                letterSpacing = DefaultTypography.bodySmall.letterSpacing,
//            ),
//            labelLarge = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.labelLarge.fontWeight,
//                fontSize = DefaultTypography.labelLarge.fontSize,
//                lineHeight = DefaultTypography.labelLarge.lineHeight,
//                letterSpacing = DefaultTypography.labelLarge.letterSpacing,
//            ),
//            labelMedium = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.labelMedium.fontWeight,
//                fontSize = DefaultTypography.labelMedium.fontSize,
//                lineHeight = DefaultTypography.labelMedium.lineHeight,
//                letterSpacing = DefaultTypography.labelMedium.letterSpacing,
//            ),
//            labelSmall = TextStyle(
//                fontFamily = MiSansTypographyFamily,
//                fontWeight = DefaultTypography.labelSmall.fontWeight,
//                fontSize = DefaultTypography.labelSmall.fontSize,
//                lineHeight = DefaultTypography.labelSmall.lineHeight,
//                letterSpacing = DefaultTypography.labelSmall.letterSpacing,
//            ),
//        )
    }
}