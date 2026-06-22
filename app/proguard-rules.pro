# ProGuard rules for Tijario App
# Control what code is kept or discarded during minification

# Room rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Kotlinx Serialization rules
-keepclassmembers class * {
    *** Companion;
}
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep serializable model classes
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Ktor rules
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Supabase rules
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**
