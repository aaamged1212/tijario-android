# Tijario application-specific R8 rules.
# Library-provided consumer rules remain active; avoid retaining entire dependency namespaces.

# Room database subclasses are instantiated through generated Room code.
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Preserve metadata used by Kotlinx Serialization generated serializers.
-keepclassmembers class * {
    *** Companion;
}
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Ktor and Supabase publish consumer ProGuard rules. Keep compatibility warnings quiet,
# but allow R8 to remove and obfuscate code that is not reachable from the application.
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**
