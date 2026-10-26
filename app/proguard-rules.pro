# ===== Retrofit =====
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ===== OkHttp =====
-dontwarn okhttp3.
-dontwarn okio.
-dontwarn javax.annotation.

# ===== kotlinx.serialization =====
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json. {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json. {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.piu.vpn.$$serializer { *; }
-keepclassmembers class com.piu.vpn. {
    *** Companion;
}
-keepclasseswithmembers class com.piu.vpn. {
    kotlinx.serialization.KSerializer serializer(...);
}

# ===== WireGuard =====
-keep class com.wireguard. { *; }
-dontwarn com.wireguard.

# ===== AdMob =====
-keep class com.google.android.gms.ads. { *; }
-dontwarn com.google.android.gms.ads.

# ===== Data classes (serialization အတွက်) =====
-keep class com.piu.vpn.data.api.dto. { *; }
-keep class com.piu.vpn.data. { *; }

# ===== Keep Compose =====
-keep class androidx.compose. { *; }
-dontwarn androidx.compose.**