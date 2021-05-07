package vault.exceptions;

// Exception is thrown when the cover image is unsupported by the encoding process.
// e.g. : grayscale images, 16bit images
public class UnsupportedImageTypeException extends Exception{

    // Constructs a new CannotDecodeException with the specified error message.
    // message the error message which can be retrieved with the getMessage method
    public UnsupportedImageTypeException(String message)
    {
        super(message);
    }

    // Constructs a new CannotDecodeException with the specified error message and cause
    // message => the error message which can be retrieved with the getMessage method
    // cause => the cause (which is saved for later retrieval by the Throwable.getCause() method).
    // (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
    public UnsupportedImageTypeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
