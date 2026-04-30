# WebRTC
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**

# Signal Protocol
-keep class org.whispersystems.** { *; }
-dontwarn org.whispersystems.**

# QR Code
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
