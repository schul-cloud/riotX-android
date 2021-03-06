/*
 * Copyright 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.matrix.android.api.session.securestorage

import im.vector.matrix.android.api.MatrixCallback
import im.vector.matrix.android.api.listeners.ProgressListener

/**
 * Some features may require clients to store encrypted data on the server so that it can be shared securely between clients.
 * Clients may also wish to securely send such data directly to each other.
 * For example, key backups (MSC1219) can store the decryption key for the backups on the server, or cross-signing (MSC1756) can store the signing keys.
 *
 * https://github.com/matrix-org/matrix-doc/pull/1946
 *
 */

interface SharedSecretStorageService {

    /**
     * Generates a SSSS key for encrypting secrets.
     * Use the SsssKeyCreationInfo object returned by the callback to get more information about the created key (recovery key ...)
     *
     * @param keyId the ID of the key
     * @param keyName a human readable name
     * @param keySigner Used to add a signature to the key (client should check key signature before storing secret)
     *
     * @param callback Get key creation info
     */
    fun generateKey(keyId: String,
                    keyName: String,
                    keySigner: KeySigner?,
                    callback: MatrixCallback<SsssKeyCreationInfo>)

    /**
     * Generates a SSSS key using the given passphrase.
     * Use the SsssKeyCreationInfo object returned by the callback to get more information about the created key (recovery key, salt, iteration ...)
     *
     * @param keyId the ID of the key
     * @param keyName human readable key name
     * @param passphrase The passphrase used to generate the key
     * @param keySigner Used to add a signature to the key (client should check key signature before retrieving secret)
     * @param progressListener The derivation of the passphrase may take long depending on the device, use this to report progress
     *
     * @param callback Get key creation info
     */
    fun generateKeyWithPassphrase(keyId: String,
                                  keyName: String,
                                  passphrase: String,
                                  keySigner: KeySigner,
                                  progressListener: ProgressListener?,
                                  callback: MatrixCallback<SsssKeyCreationInfo>)

    fun getKey(keyId: String): KeyInfoResult

    /**
     * A key can be marked as the "default" key by setting the user's account_data with event type m.secret_storage.default_key
     * to an object that has the ID of the key as its key property.
     * The default key will be used to encrypt all secrets that the user would expect to be available on all their clients.
     * Unless the user specifies otherwise, clients will try to use the default key to decrypt secrets.
     */
    fun getDefaultKey(): KeyInfoResult

    fun setDefaultKey(keyId: String, callback: MatrixCallback<Unit>)

    /**
     * Check whether we have a key with a given ID.
     *
     * @param keyId The ID of the key to check
     * @return Whether we have the key.
     */
    fun hasKey(keyId: String): Boolean

    /**
     * Store an encrypted secret on the server
     * Clients MUST ensure that the key is trusted before using it to encrypt secrets.
     *
     * @param name The name of the secret
     * @param secret The secret contents.
     * @param keys The list of (ID,privateKey) of the keys to use to encrypt the secret.
     */
    fun storeSecret(name: String, secretBase64: String, keys: List<KeyRef>, callback: MatrixCallback<Unit>)

    /**
     * Use this call to determine which SSSSKeySpec to use for requesting secret
     */
    fun getAlgorithmsForSecret(name: String): List<KeyInfoResult>

    /**
     * Get an encrypted secret from the shared storage
     *
     * @param name The name of the secret
     * @param keyId The id of the key that should be used to decrypt (null for default key)
     * @param secretKey the secret key to use (@see #RawBytesKeySpec)
     *
     */
    fun getSecret(name: String, keyId: String?, secretKey: SsssKeySpec, callback: MatrixCallback<String>)

    fun checkShouldBeAbleToAccessSecrets(secretNames: List<String>, keyId: String?) : IntegrityResult

    data class KeyRef(
            val keyId: String?,
            val keySpec: SsssKeySpec?
    )
}
