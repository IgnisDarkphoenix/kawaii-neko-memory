# ============================================
# Kawaii Neko Memory - ProGuard Rules
# DarkphoenixTeam
# ============================================

# === LIBGDX ===
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.jnigen.BuildTarget*
-dontwarn com.badlogic.gdx.graphics.g2d.freetype.FreeType

-keep class com.badlogic.gdx.** { *; }
-keep class com.badlogic.gdx.backends.android.** { *; }
-keep class com.badlogic.gdx.physics.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# === APPLOVIN MAX ===
-dontwarn com.applovin.**
-keep class com.applovin.** { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { *; }

# === GOOGLE ADMOB ===
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }
-keep class com.google.ads.** { *; }

# === UNITY ADS ===
-dontwarn com.unity3d.ads.**
-keep class com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }

# === NUESTRO CÓDIGO ===
-keep class com.darkphoenixteam.kawaiinekomemory.** { *; }

# === GENERAL ===
# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}