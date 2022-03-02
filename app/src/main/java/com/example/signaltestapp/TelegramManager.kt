package com.example.signaltestapp

import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
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

    fun initClient() {
        client = Client.create(UpdateHandler(), null, null)
    }

    private fun onAuthorizationStateUpdated(authState: TdApi.AuthorizationState) {
        Timber.d("Got onAuthStateUpdated: ${authState.javaClass.canonicalName}")
        when(authState.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                val parameters = TdlibParameters()
                parameters.databaseDirectory = "tdlib"
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
            TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateClosed")
//                client = Client.create(
//                    UpdateHandler(),
//                    null,
//                    null
//                ) // recreate client after previous has closed
            }
            TdApi.AuthorizationStateClosing.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateClosing")
            }
            TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateLoggingOut")
            }
            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateReady -> authorized")
            }
            TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateWaitCode")
                // TODO pickup the code from the text box
                val code = "123"
                client.send(CheckAuthenticationCode(code), AuthorizationRequestHandler())
            }
            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                Timber.d("AuthorizationStateWaitCode")
                client.send(CheckDatabaseEncryptionKey(), AuthorizationRequestHandler())
            }
            TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                TODO()
            }
            TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                TODO()
            }
            TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                TODO()
            }
            TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR -> {
                TODO()
            }
        }
    }

    inner class AuthorizationRequestHandler: Client.ResultHandler {
        override fun onResult(result: TdApi.Object) {
            when(result.constructor) {
                TdApi.Error.CONSTRUCTOR -> {
                    Timber.d("Authorization error")
                    onAuthorizationStateUpdated(null); // repeat last action
                }
                TdApi.Ok.CONSTRUCTOR -> {
                    Timber.d("Authorization OK")
                }
                else -> {
                    Timber.d("Received wrong response from TDLib: ${result.constructor}")
                }
            }
        }

    }

    inner class UpdateHandler(): Client.ResultHandler {
        override fun onResult(response: TdApi.Object) {
            Timber.d("Got UpdateHandler response: ${response.javaClass.canonicalName}")
            when(response.constructor) {
                TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {

                }
                else -> {

                }
            }

        }
    }

}