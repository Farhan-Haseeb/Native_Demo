# =============================================================================
# example_android - integrator App-side ProGuard / R8 rules template
#
# This Demo enables minifyEnabled to verify how the IDO Native SDK AAR behaves under R8.
# Integrators can copy the sections below into their own app/proguard-rules.pro and enable only the ones they use.
#
# Description:
# - SDK AAR (protocol_channel / flutter_bluetooth / native_channel) rules are merged via
#   consumerProguardFiles automatically; the App side must not add global keeps like 'whole-package kotlin.** /
#   android.app.Activity / io.flutter.**' and similar global keep rules, otherwise they lower Google Play
#   DEX optimization coverage.
# - This Demo depends on the flutter_embedding AAR directly (not the flutter.gradle full-injection path):
#   Embedding JNI entry points rely on in-library @Keep + the keepattributes below; do not hand-write whole-package keep rules.
# - Zephyr / MCUmgr: keep + dontwarn are already in native_channel consumer-rules.pro;
#   Native AAR (packaged mode, compileOnly): integrators add implementation 'mcumgr-ble:1.5.2' on demand (requires 1.5.x).
#   Flutter path includes it transitively by default; no need to add it again.
# =============================================================================

# ---------- Debug stack trace (recommended on for the Demo; can be turned off in release builds) ----------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------- Required: Gson / reflection / @Keep metadata ----------
# SDK Model and App need Signature and annotations kept if they use Gson; field keep rules are covered by the SDK consumer rules
# for com.idosmart.**. For the App's own Gson bean classes, add -keep yourself.
# *Annotation* also ensures that Embedding and other libraries' androidx.annotation.Keep takes effect.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses, EnclosingMethod
-dontwarn com.google.gson.**

# ---------- Flutter Embedding (Native host; do not blanket -keep io.flutter.**) ----------
# Aligned with Flutter's official flutter_proguard_rules.pro dontwarn entries (this Demo does not go through the flutter.gradle full-injection path)
-dontwarn io.flutter.plugin.**
# Embedding references Play Core SplitCompat; suppress missing-class warnings when Play Feature Delivery is not integrated
-dontwarn com.google.android.play.core.splitcompat.SplitCompatApplication
-dontwarn com.google.android.play.core.splitinstall.**
-dontwarn com.google.android.play.core.tasks.**

# ---------- Required: this Demo directly depends on OkHttp / Okio ----------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# ---------- Required: optional classes referenced transitively by native_channel but not bundled in the flatDir AAR ----------
# Alexa upload and similar paths reference commons-io; if it is not added as a separate implementation, dontwarn is enough
# (If your business logic reaches that path, edit build.gradle to add the commons-io dependency instead of only dontwarn).
-dontwarn org.apache.commons.io.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.**

# =============================================================================
# Nordic Legacy / Secure DFU (no.nordicsemi.android:dfu)
# =============================================================================
# Prerequisite: uncomment in build.gradle
#   implementation 'no.nordicsemi.android:dfu:2.5.0'
# flutter_bluetooth AAR consumer already includes keep + dontwarn; the rules below are a fallback (still compiles when not merged into
# a new AAR / flatDir old package). When not integrated, keeping -dontwarn is enough.
#-keep class no.nordicsemi.android.dfu.** { *; }
#-dontwarn no.nordicsemi.android.dfu.**
#-dontwarn no.nordicsemi.android.error.**

# =============================================================================
# Nordic Zephyr / MCUmgr DFU (no.nordicsemi.android:mcumgr-ble:1.5.2)
# =============================================================================
# keep + dontwarn are already merged automatically from the native_channel AAR consumer-rules.pro; do not repeat them in the App.
# Native AAR packaged mode uses compileOnly; when using startDFU, declare it in build.gradle (requires 1.5.x):
#   implementation 'no.nordicsemi.android:mcumgr-ble:1.5.2'

# =============================================================================
# Other platform OTA (on demand)
# =============================================================================
# When Realtek / Sifli etc. paths are unused no keep is needed; add rules only if the App reflects the related classes directly.
#-keep class com.realsil.** { *; }
#-dontwarn com.realsil.**
