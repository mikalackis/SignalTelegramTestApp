package com.example.signaltestapp

import android.content.Context
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi.*
import timber.log.Timber


class TelegramManager {

    companion object {
        init {
            try {
                System.loadLibrary("tdjni");
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace();
            }
        }
    }

    lateinit var client: Client

    lateinit var context: Context

    lateinit var phoneNumber: String

    private val defaultHandler = DefaultHandler()

    fun initClient(context: Context, phoneNumber: String) {
        this.context = context
        this.phoneNumber = phoneNumber
        client = Client.create(UpdateHandler(), null, null)
    }

    fun createSecretChat(chatId: Int) {
        client.send(CreateSecretChat(chatId), defaultHandler)
    }

    private fun onAuthorizationStateUpdated(authState: AuthorizationState?) {
        Timber.d("Got onAuthStateUpdated: ${authState?.javaClass?.canonicalName}")
        when(authState?.constructor) {
            AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                val parameters = TdlibParameters()
                parameters.databaseDirectory = context.filesDir.absolutePath
                parameters.useMessageDatabase = true
                parameters.useSecretChats = true
                parameters.apiId = 94575
                parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2"
                parameters.systemLanguageCode = "en"
                parameters.deviceModel = "ArielPhone"
                parameters.applicationVersion = "1.0"
                parameters.enableStorageOptimizer = true
                if(BuildConfig.DEBUG)
                    parameters.useTestDc = true

                client.send(SetTdlibParameters(parameters), AuthorizationRequestHandler())
            }
            AuthorizationStateClosed.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateClosed")
//                client = Client.create(
//                    UpdateHandler(),
//                    null,
//                    null
//                ) // recreate client after previous has closed
            }
            AuthorizationStateClosing.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateClosing")
            }
            AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateLoggingOut")
            }
            AuthorizationStateReady.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateReady -> authorized")
            }
            AuthorizationStateWaitCode.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateWaitCode")
                // TODO pickup the code from the text box
                val code = "123"
                client.send(CheckAuthenticationCode(code), AuthorizationRequestHandler())
            }
            AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateWaitCode")
                client.send(CheckDatabaseEncryptionKey(), AuthorizationRequestHandler())
            }
            AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateWaitOtherDeviceConfirmation -> confirm link on another device")
            }
            AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateWaitPassword -> please enter password")
            }
            AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateWaitPhoneNumber -> please enter phone number")
                client.send(
                    SetAuthenticationPhoneNumber(phoneNumber, null),
                    AuthorizationRequestHandler()
                )
            }
            AuthorizationStateWaitRegistration.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateWaitRegistration -> please enter first and last name")
            }
        }
    }

    inner class AuthorizationRequestHandler: Client.ResultHandler {
        override fun onResult(result: Object) {
            when(result.constructor) {
                Error.CONSTRUCTOR -> {
                    Timber.d("Authorization error")
                    onAuthorizationStateUpdated(null); // repeat last action
                }
                Ok.CONSTRUCTOR -> {
                    Timber.d("Authorization OK")
                }
                else -> {
                    Timber.d("Received wrong response from TDLib: ${result.constructor}")
                }
            }
        }

    }

    inner class UpdateHandler: Client.ResultHandler {
        override fun onResult(response: Object) {
            Timber.d("Got UpdateHandler response: ${response.javaClass.canonicalName}")
            when(response.constructor) {
                UpdateAuthorizationState.CONSTRUCTOR -> {
                    onAuthorizationStateUpdated((response as UpdateAuthorizationState).authorizationState)
                }
                else -> {

                }
            }

        }
    }

    inner class DefaultHandler: Client.ResultHandler {
        override fun onResult(result: Object?) {
            Timber.d("DefaultHandler response: ${result.toString()}")
        }
    }

}