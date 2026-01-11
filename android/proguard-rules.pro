# libGDX
-keep class com.badlogic.gdx.** { *; }
-keep class com.badlogic.gdx.backends.android.** { *; }

# Reflection used by libGDX
-keepclassmembers class * {
    @com.badlogic.gdx.utils.reflect.* *;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# FreeType
-keep class com.badlogic.gdx.graphics.g2d.freetype.** { *; }

# === ADS (Descomentar cuando se agreguen) ===
# AdMob
# -keep class com.google.android.gms.ads.** { *; }

# AppLovin
# -keep class com.applovin.** { *; }

# Unity Ads
# -keep class com.unity3d.ads.** { *; }

# Game classes
-keep class com.darkphoenixteam.kawaiinekomemory.** { *; }