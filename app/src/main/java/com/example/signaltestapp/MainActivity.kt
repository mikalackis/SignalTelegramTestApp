package com.example.signaltestapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.util.KeyHelper
import org.whispersystems.libsignal.util.guava.Optional
import org.whispersystems.signalservice.api.SignalServiceAccountManager
import org.whispersystems.signalservice.api.push.TrustStore
import org.whispersystems.signalservice.internal.configuration.SignalCdnUrl
import org.whispersystems.signalservice.internal.configuration.SignalContactDiscoveryUrl
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration
import org.whispersystems.signalservice.internal.configuration.SignalServiceUrl
import java.io.InputStream
import java.util.*
import java.util.concurrent.ExecutorService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val simpleRunnable = Thread(SimpleRunnable())
        simpleRunnable.start()
    }

    class SimpleRunnable: Runnable {
        public override fun run() {
            testSignal()
        }

        private fun testSignal() {
            // todo wtf is this
            val signedPreKeyId = 999
            val identityKey = KeyHelper.generateIdentityKeyPair()
            val oneTimePreKeys = KeyHelper.generatePreKeys(0, 100)
            //val lastResortKey: PreKeyRecord = KeyHelper.generateLastResortPreKey()
            val signedPreKeyRecord = KeyHelper.generateSignedPreKey(identityKey, signedPreKeyId)

            val arielTrustStore = ArielTrustStore()

            val signalServiceUrl = SignalServiceUrl("https://chat.signal.org", arielTrustStore)
            val signalCdn1 = SignalCdnUrl("https://cdn.signal.org", arielTrustStore)
            val signalCdn2 = SignalCdnUrl("https://cdn2.signal.org", arielTrustStore)

            val signalContactDiscoveryUrl = SignalContactDiscoveryUrl("https://api.directory.signal.org", arielTrustStore)

            val signalServiceConfiguration = SignalServiceConfiguration(
                arrayOf(signalServiceUrl), arrayOf(signalCdn1, signalCdn2), arrayOf(signalContactDiscoveryUrl))

            val myUUID = UUID.randomUUID()

            val signalAccountManager = SignalServiceAccountManager(signalServiceConfiguration,
                myUUID, "+381652470801", "ariel_pass", "ariel-guardian")

            signalAccountManager.setPreKeys(identityKey.publicKey, signedPreKeyRecord, oneTimePreKeys)

            signalAccountManager.addDevice("ariel_pixel_2", identityKey.publicKey)

            signalAccountManager.requestSmsVerificationCode(true, Optional.absent(), Optional.absent())

        }

        private class ArielTrustStore: TrustStore {
            override fun getKeyStoreInputStream(): InputStream {
                return ArielTrustStore::class.java.getResourceAsStream("whisper.store")!!
            }

            override fun getKeyStorePassword(): String {
                return "whisper"
            }

        }
    }
}