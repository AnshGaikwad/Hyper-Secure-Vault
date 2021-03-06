package vault.exceptions;

// Exception is thrown when an error occurs when decoding an image,
// mainly when trying to decode images that don't contain hidden data.
public class CannotDecodeException extends Exception
{
    // Constructs a new <code>CannotDecodeException</code> with the specified error message.
    // message the error message which can be retrieved with the <code>getMessage</code> method
    public CannotDecodeException(String message)
    {
        super(message);
    }

    // Constructs a new CannotDecodeException with the specified error message and cause.
    // message => the error message which can be retrieved with the getMessage method
    // cause => the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
    public CannotDecodeException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
