package com.p2pmessenger.crypto

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "encryption_keys")

private val PUBLIC_KEY_KEY = stringPreferencesKey("public_key")
private val PRIVATE_KEY_KEY = stringPreferencesKey("private_key")

@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private lateinit var secretKey: SecretKey
    private val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    suspend fun initialize() {
        context.dataStore.data.collect { preferences ->
            val storedPublicKey = preferences[PUBLIC_KEY_KEY]
            val storedPrivateKey = preferences[PRIVATE_KEY_KEY]
            
            if (storedPublicKey != null && storedPrivateKey != null) {
                // Load existing keys
                secretKey = restoreSecretKey(storedPrivateKey)
            } else {
                generateNewKeys()
            }
        }
    }
    
    private suspend fun generateNewKeys() {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        secretKey = keyGenerator.generateKey()
        
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        
        context.dataStore.edit { preferences ->
            preferences[PUBLIC_KEY_KEY] = encodedKey
            preferences[PRIVATE_KEY_KEY] = encodedKey
        }
    }
    
    private fun restoreSecretKey(encoded: String): SecretKey {
        val keyBytes = Base64.getDecoder().decode(encoded)
        return javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
    }
    
    fun getPublicKey(): String {
        return Base64.getEncoder().encodeToString(secretKey.encoded)
    }
    
    fun getPeerId(): String {
        return getPublicKey().take(16)
    }
    
    fun encryptMessage(message: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12)
        java.security.SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        
        val ciphertext = cipher.doFinal(message.toByteArray())
        val combined = iv + ciphertext
        
        return Base64.getEncoder().encodeToString(combined)
    }
    
    fun decryptMessage(encryptedMessage: String): String {
        val combined = Base64.getDecoder().decode(encryptedMessage)
        val iv = combined.sliceArray(0..11)
        val ciphertext = combined.sliceArray(12 until combined.size)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext)
    }
}
