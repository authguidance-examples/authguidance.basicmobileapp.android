/*
 * The root gradle file brings in plugins which are applied in child scripts
 */
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
}
