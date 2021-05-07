package vault.algorithms;

import vault.exceptions.CannotDecodeException;
import vault.exceptions.CannotEncodeException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


// Superclass of all the classes used in the steganographic process.
public abstract class BaseSteganography{

    // Hidden data encryption status
    boolean isEncrypted = false;

    // Hidden data compression status
    boolean isCompressed = false;

    // Encoding mode (1 or 2 pixels/byte)
    byte pixelsPerByte;

    // Capacity of the cover image
    long capacity;

    // Contains information about the embedded data
    byte[] header;

    // Contains info about the embedded data in a readable format
    HiddenData secretInfo;

    // x-axis current pixel position
    int i = 0;

    // y-axis current pixel position
    int j = 0;

    // Constructor
    BaseSteganography(){}

    // Encodes a byte array into a cover image
    public abstract void encode(byte[] message, File output) throws IOException, CannotEncodeException;

    // Encodes a document file into a cover image
    public abstract void encode(File doc, File output) throws IOException, CannotEncodeException;

    // Decodes the content of a stego image and saves it to a file
    public abstract void decode(File file) throws IOException, CannotDecodeException;

    // Returns the header containing information about the embedded data from a stego image.
    public abstract byte[] getHeader() throws CannotDecodeException;

    // Increments the position of the current pixel in the image.
    protected abstract void increment();

    // Resets the value of the index.
    protected abstract void reset();

    //Returns the capacity of the cover image.
    public long getCapacity(){
        return this.capacity;
    }

    // Returns the secretInfo field containing the info about the embedded data of the stego image.
    protected vault.algorithms.HiddenData getSecretInfo(){
        return this.secretInfo;
    }

    // Sets the secretInfo field containing info about the embedded data in the stego image.
    void setSecretInfo(vault.algorithms.HiddenData info){
        this.secretInfo = info;
    }

     // Sets the class {header field with information about a message to embed in the image.
     // ['M', Encryption Status ('E' | 'C'), Compression Status ('C' | 'U'), Pixels/Byte (1|2), Message length (3 bytes = 16777215 bytes), '!']
     // Example: [01001101, 01000100, 01010101, 00000001, 00000000, 00110011, 11001100, 00100001]
     // Corresponds to: ['M' (Message), 'E' (isEncrypted), 'U' (!isCompressed), 1 Pixel/Byte, 13260 bytes, '!']
     // CannotEncodeException => if the message is empty or larger than maximum capacity.
    byte[] setHeader(byte[] message) throws CannotEncodeException
    {
        if (message.length == 0)
            throw new CannotEncodeException("Message is empty");

        if (message.length > 16777215)
            throw new CannotEncodeException("Message is larger than maximum allowed capacity (16777215 bytes)");

        List<Byte> header = new ArrayList<>();
        header.add((byte)'M');
        header.add((this.isEncrypted) ? (byte)'E' : (byte)'U');
        header.add((this.isCompressed) ? (byte)'C' : (byte)'U');
        header.add(this.pixelsPerByte);
        String messageLength = String.format("%24s", Integer.toBinaryString(message.length)).replace(' ', '0');

        for(int i=0; i<messageLength.length();i+=8)
        {
            header.add((byte)Integer.parseInt(messageLength.substring(i,i+8),2));
        }

        header.add(((byte) '!'));
        this.header = vault.algorithms.Utils.toByteArray(header);

        if (capacity - this.header.length < message.length)
            throw new CannotEncodeException("Message is larger than image capacity by "+(message.length-capacity+this.header.length)+" bytes.");

        return vault.algorithms.Utils.toByteArray(header);
    }

    // Sets the class {header field with information about a message to embed in the image.
    // ['M', Encryption Status ('E' | 'C'), Compression Status ('C' | 'U'), Pixels/Byte (1|2), Message length (3 bytes = 16777215 bytes), '!']
    // Example: [01001101, 01000100, 01010101, 00000001, 00000000, 00110011, 11001100, 00100001]
    // Corresponds to: ['M' (Message), 'E' (isEncrypted), 'U' (!isCompressed), 1 Pixel/Byte, 13260 bytes, '!']
    // CannotEncodeException => if the message is empty or larger than maximum capacity
    byte[] setHeader(File file) throws IOException, CannotEncodeException
    {
        if (file.length() == 0)
            throw new CannotEncodeException("File is empty.");

        if (file.length() >  16777215)
            throw new CannotEncodeException("File is larger than maximum allowed capacity (16777215 bytes)");

        List<Byte> header = new ArrayList<>();
        String extension = vault.algorithms.Utils.getFileExtension(file).toLowerCase();
        header.add((byte)'D');
        header.add((this.isEncrypted) ? (byte)'E' : (byte)'U');
        header.add((this.isCompressed) ? (byte)'C' : (byte)'U');
        header.add(this.pixelsPerByte);
        String fileLength = String.format("%24s",Long.toBinaryString(file.length())).replace(' ', '0');

        for(int i=0; i<fileLength.length();i+=8)
        {
            header.add((byte)Integer.parseInt(fileLength.substring(i,i+8),2));
        }

        byte[] fileExtension = extension.getBytes(StandardCharsets.UTF_8);

        for(byte b : fileExtension)
            header.add(b);

        header.add(((byte) '!'));
        this.header = vault.algorithms.Utils.toByteArray(header);

        if (capacity - this.header.length < file.length())
            throw new CannotEncodeException("File is larger than maximum capacity by "+(file.length()-capacity+this.header.length)+" bytes.");

        return vault.algorithms.Utils.toByteArray(header);
    }

}
