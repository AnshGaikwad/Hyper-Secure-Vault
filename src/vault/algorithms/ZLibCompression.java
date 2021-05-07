package vault.algorithms;

import vault.modals.AlertBox;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

// Uses java ZLib compression/decompression library to compress/decompress file
// To reduce the effect of the embedded data on the steganographic image
public class ZLibCompression
{

    // Size of the buffer used to compress a file by chucks
    private static final int BUF_SIZE = 256;

    // Compress the file
    // raw : InputFile, compressed : OutputFile
    public static void compress(File raw, File compressed)
    {
        try
        {
            InputStream is = new FileInputStream(raw);
            OutputStream os = new DeflaterOutputStream(new FileOutputStream(compressed));
            copy(is, os);

        }
        catch (IOException e)
        {
            e.printStackTrace();
            AlertBox.error("Error while compressing", e.getMessage());
        }
    }

    // Compress file with byte array param and return type byte array
    // To avoid giving output file as param
    public static byte[] compress(byte [] raw)
    {
        InputStream is = new ByteArrayInputStream(raw);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = new DeflaterOutputStream(baos);
        try
        {
            copy(is, os);
            return baos.toByteArray();

        }
        catch (IOException e)
        {
            e.printStackTrace();
            AlertBox.error("Error while compressing", e.getMessage());
            return null;
        }
    }

    // Decompress the file
    // compressed : InputFile, raw : OutputFile
    public static void decompress(File compressed, File raw)
    {
        try
        {
            InputStream is = new InflaterInputStream(new FileInputStream(compressed));
            OutputStream os = new FileOutputStream(raw);
            copy(is, os);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            AlertBox.error("Error while decompressing", e.getMessage());
        }
    }

    // Decompress file with byte array param and return type byte array
    // To avoid giving output file as param
    public static byte[] decompress(byte[] compressed)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = new InflaterInputStream(new ByteArrayInputStream(compressed));
        try
        {
            copy(is, baos);
            return baos.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            AlertBox.error("Error while decompressing", e.getMessage());
            return null;
        }
    }

    // Copies the input stream to ouput stream in chucks using a buffer, therefore compressing it
    private static void copy(InputStream is, OutputStream os) throws IOException
    {
        byte[] buffer = new byte[BUF_SIZE];
        int length;
        while ((length = is.read(buffer)) > 0)
            os.write(buffer, 0, length);
        is.close();
        os.close();
    }

}
