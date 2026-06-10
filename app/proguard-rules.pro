# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
-verbose

# Optimization is turned off by default. Dontoptimize to remain on.
-dontoptimize

# Preserve some line number information for debugging stack traces.
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom application classes
-keep class com.pillow.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Keep Room classes
-keep class androidx.room.** { *; }
-keepclasseswithmembers class * {
    @androidx.room.* <methods>;
}

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
