# ==============================================================================
# Reglas ProGuard / R8 — Biblia de Estudio (v1.5.0)
# ==============================================================================

# 1. Conservar atributos de depuración, anotaciones y metadatos de Kotlin
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault
-keep class kotlin.Metadata { *; }

# 2. Room (Entidades, DAOs, Base de datos, constructores y clases generadas *_Impl)
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * {
    <fields>;
    <init>(...);
}
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
    @androidx.room.Dao *;
}
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.example.data.database.** { *; }
-keep class com.example.data.dao.** { *; }

# 3. Modelos de datos (bíblicos, comentarios, diccionarios, catálogo de módulos y sincronización)
-keep class com.example.data.model.** { *; }
-keep class com.example.data.repository.CloudUserProfile { *; }
-keep class com.example.data.repository.CloudBackupResult { *; }
-keep class com.example.data.repository.SyncStatus { *; }

# 4. Serialización JSON / Assets (Moshi, Gson, Kotlinx Serialization y java.io.Serializable)
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep class **JsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
    public <init>(com.squareup.moshi.Moshi, java.lang.reflect.Type[]);
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepattributes *Annotation*
-keep,includedescriptorclasses class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class kotlinx.serialization.** { *; }

# 5. Firebase (Auth, Firestore, AI, App Check)
-keep class com.google.firebase.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# 6. Google Play Services, Auth y Credential Manager (Google Sign-In)
-keep class com.google.android.gms.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# 7. Corrutinas de Kotlin
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }

# 8. ViewModel y fábrica
-keep class com.example.ui.viewmodel.BibleViewModel { *; }
-keep class com.example.ui.viewmodel.BibleViewModelFactory { *; }

# 9. Clases exclusivas de JVM de escritorio referenciadas opcionalmente por Ktor (transitivo de firebase-ai)
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

