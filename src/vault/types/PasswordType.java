package vault.types;

/**
 * Sets the behaviour of the PasswordPrompt based on the type of the operation.
 *
 * @see vault.modals.PasswordPrompt
 */
public enum PasswordType {
    /** Use the Encryption mode (password validation and confirmation box). */
    ENCRYPT,
    /** Use the Decryption mode. */
    DECRYPT}
