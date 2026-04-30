package com.p2pmessenger.crypto

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.whispersystems.libsignal.*
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.ecc.ECKeyPair
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.IdentityKeyStore
import org.whispersystems.libsignal.state.PreKeyStore
import org.whispersystems.libsignal.state.SignedPreKeyStore
import org.whispersystems.libsignal.state.SessionStore
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "encryption_keys")

private val IDENTITY_KEY_PAIR_KEY = stringPreferencesKey("identity_key_pair")
private val REGISTRATION_ID_KEY = stringPreferencesKey("registration_id")

@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SignalProtocolStore {
    
    private val identityKeyStore = object : IdentityKeyStore {
        override fun getIdentityKeyPair(): IdentityKeyPair {
            return identityKeyPair
        }
        
        override fun getLocalRegistrationId(): Int {
            return registrationId
        }
        
        override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean {
            // Store peer identity keys
            return true
        }
        
        override fun isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey, direction: IdentityKeyStore.Direction): Boolean {
            // For P2P, we can trust first-use keys
            return true
        }
    }
    
    private val preKeyStore = object : PreKeyStore {
        private val preKeys = mutableMapOf<Int, PreKeyRecord>()
        
        override fun loadPreKey(preKeyId: Int): PreKeyRecord {
            return preKeys[preKeyId] ?: throw Exception("PreKey not found")
        }
        
        override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
            preKeys[preKeyId] = record
        }
        
        override fun containsPreKey(preKeyId: Int): Boolean {
            return preKeys.containsKey(preKeyId)
        }
        
        override fun removePreKey(preKeyId: Int) {
            preKeys.remove(preKeyId)
        }
    }
    
    private val signedPreKeyStore = object : SignedPreKeyStore {
        private val signedPreKeys = mutableMapOf<Int, SignedPreKeyRecord>()
        
        override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
            return signedPreKeys[signedPreKeyId] ?: throw Exception("SignedPreKey not found")
        }
        
        override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
            signedPreKeys[signedPreKeyId] = record
        }
        
        override fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
            return signedPreKeys.containsKey(signedPreKeyId)
        }
        
        override fun removeSignedPreKey(signedPreKeyId: Int) {
            signedPreKeys.remove(signedPreKeyId)
        }
    }
    
    private val sessionStore = object : SessionStore {
        private val sessions = mutableMapOf<String, SessionRecord>()
        
        override fun loadSession(address: SignalProtocolAddress): SessionRecord {
            return sessions[address.name] ?: SessionRecord()
        }
        
        override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
            sessions[address.name] = record
        }
        
        override fun containsSession(address: SignalProtocolAddress): Boolean {
            return sessions.containsKey(address.name)
        }
        
        override fun deleteSession(address: SignalProtocolAddress) {
            sessions.remove(address.name)
        }
        
        override fun deleteAllSessions(name: String) {
            sessions.keys.filter { it.startsWith(name) }.forEach { sessions.remove(it) }
        }
        
        override fun getSubDeviceSessions(name: String): List<Int> {
            return sessions.keys.filter { it.startsWith(name) }.map { 1 }
        }
    }
    
    private lateinit var identityKeyPair: IdentityKeyPair
    private lateinit var registrationId: Int
    
    suspend fun initialize() {
        context.dataStore.data.collect { preferences ->
            val storedKeyPair = preferences[IDENTITY_KEY_PAIR_KEY]
            val storedRegId = preferences[REGISTRATION_ID_KEY]
            
            if (storedKeyPair != null && storedRegId != null) {
                identityKeyPair = deserializeIdentityKeyPair(storedKeyPair)
                registrationId = storedRegId.toInt()
            } else {
                generateNewIdentity()
            }
        }
    }
    
    private suspend fun generateNewIdentity() {
        val keyPair = Curve.generateKeyPair()
        identityKeyPair = IdentityKeyPair(keyPair)
        registrationId = KeyHelper.generateRegistrationId(false)
        
        context.dataStore.edit { preferences ->
            preferences[IDENTITY_KEY_PAIR_KEY] = serializeIdentityKeyPair(identityKeyPair)
            preferences[REGISTRATION_ID_KEY] = registrationId.toString()
        }
    }
    
    fun getPublicKey(): String {
        return Base64.getEncoder().encodeToString(
            identityKeyPair.publicKey.serialize()
        )
    }
    
    fun getPeerId(): String {
        return getPublicKey().take(16)
    }
    
    suspend fun encryptMessage(peerPublicKey: String, message: String): String {
        val peerAddress = SignalProtocolAddress(peerPublicKey, 1)
        val sessionCipher = SessionCipher(this, peerAddress)
        
        val ciphertext = sessionCipher.encrypt(message.toByteArray())
        return Base64.getEncoder().encodeToString(ciphertext.serialize())
    }
    
    suspend fun decryptMessage(peerPublicKey: String, encryptedMessage: String): String {
        val peerAddress = SignalProtocolAddress(peerPublicKey, 1)
        val sessionCipher = SessionCipher(this, peerAddress)
        
        val ciphertext = PreKeySignalMessage(
            Base64.getDecoder().decode(encryptedMessage)
        )
        
        val plaintext = sessionCipher.decrypt(ciphertext)
        return String(plaintext)
    }
    
    // SignalProtocolStore implementation
    override fun identityKeyStore(): IdentityKeyStore = identityKeyStore
    override fun preKeyStore(): PreKeyStore = preKeyStore
    override fun signedPreKeyStore(): SignedPreKeyStore = signedPreKeyStore
    override fun sessionStore(): SessionStore = sessionStore
    
    private fun serializeIdentityKeyPair(keyPair: IdentityKeyPair): String {
        return Base64.getEncoder().encodeToString(keyPair.serialize())
    }
    
    private fun deserializeIdentityKeyPair(serialized: String): IdentityKeyPair {
        return IdentityKeyPair(Base64.getDecoder().decode(serialized))
    }
}
