# Keep PayPing model and serialization classes intact if minify is enabled later
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.darkxwn.payping.domain.backup.**$$serializer { *; }
-keepclassmembers class com.darkxwn.payping.domain.backup.** {
    *** Companion;
}
