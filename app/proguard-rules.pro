# Preserve annotations and inner classes for serialization and Room under R8 Full Mode
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.**

# Keep PayPing JSON backup DTOs intact under R8 Full Mode
-keep @kotlinx.serialization.Serializable class com.darkxwn.payping.domain.backup.** { *; }
-keepclassmembers class com.darkxwn.payping.domain.backup.** {
    *** Companion;
}
