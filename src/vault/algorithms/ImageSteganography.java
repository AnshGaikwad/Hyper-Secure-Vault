package vault.algorithms;

import vault.exceptions.CannotDecodeException;
import vault.exceptions.CannotEncodeException;
import vault.exceptions.UnsupportedImageTypeException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Handles the steganographic process for 24-bit, RGB Bitmap images using the Least Significant Bit (LSB) method.
// Image formats supported for encoding: png, bmp, jpg
// Image formats supported for decoding: png, bmp
// One byte can be hidden in either 1 pixel of the image (for maximum capacity) or 2 pixels (for minimum effect on the image).
public class ImageSteganography extends BaseSteganography
{
    // To perform embedding/extraction on
    BufferedImage image;

    // Creates an ImageSteganography object to perform embedding or extraction of data on 24-bit, RGB Bitmap images.
    public ImageSteganography(File input, boolean isEncrypted, boolean isCompressed, byte pixelsPerByte) throws IOException, UnsupportedImageTypeException
    {
        this.isEncrypted = isEncrypted;
        this.isCompressed = isCompressed;
        this.pixelsPerByte = pixelsPerByte;
        this.image = ImageIO.read(input);
        this.capacity = (long) this.image.getHeight() * this.image.getWidth() / this.pixelsPerByte;
        if(this.image.getType() == BufferedImage.TYPE_CUSTOM || this.image.getType() >= 8)
            throw new UnsupportedImageTypeException("Image type " + this.image.getType() + " is unsupported");
    }

    // Creates an ImageSteganography object to perform embedding or extraction of data on 24-bit, RGB Bitmap images.
    public ImageSteganography(File input) throws IOException, UnsupportedImageTypeException
    {
        this(input, false, false, (byte) 1);
    }

    // Returns the image field used to perform the embedding/extraction on
    public BufferedImage getImage()
    {
        return this.image;
    }

    // Returns the header that contains information about the data embedded in the stegonographic image.
    public byte[] getHeader() throws CannotDecodeException
    {
        int b;
        List<Byte> header = new ArrayList<>();
        if (revealByte(0,0,(byte)1) != (byte) 'M' && revealByte(0,0,(byte)1) != (byte) 'D' && revealByte(0,0,(byte)1) != (byte) 'I')
            throw new CannotDecodeException("There is nothing embedded in this image");
        do
        {
            b = revealByte(this.i, this.j, (byte)1);
            increment();
            header.add((byte)b);
        }
        while(b != (byte) '!');

        this.header = Utils.toByteArray(header);
        return Utils.toByteArray(header);
    }

    // Writes the data contained in a byte array header to the first pixels of the image.
    void writeHeader(byte[] header)
    {
        for(byte b : header)
        {
            hideByte(b, this.i, this.j, (byte)1);
            increment();
        }
    }

    // Calls setHeader(byte[]) to write the message header to the stego image,
    // then encodes the message to hide using hideByte(byte, int, int, byte) byte by byte,
    // then saves the stego image.
    public void encode(byte[] message, File output) throws IOException, CannotEncodeException
    {
        this.writeHeader(this.setHeader(message));
        for (byte b : message)
        {
            hideByte(b, this.i, this.j, this.pixelsPerByte);
            increment();
        }
        ImageIO.write(this.image, "png", output);
    }

    // Calls setHeader(byte[]) to write the message header to the stego image,
    // then encodes the message to hide using hideByte(byte, int, int, byte) byte by byte,
    // then saves the stego image.
    public void encode(File doc, File output) throws IOException, CannotEncodeException
    {
        this.writeHeader(this.setHeader(doc));
        FileInputStream fis = new FileInputStream(doc);
        byte[] buffer = new byte[256];
        while(fis.read(buffer) > 0)
            for(byte b : buffer)
            {
                hideByte(b, this.i, this.j, this.pixelsPerByte);
                increment();
            }
        ImageIO.write(this.image, "png", output);
    }

    // Extracts the header from the stego image to create the secretInfo field,
    // then decodes the hidden data in the image using revealByte(int, int, byte),
    // then saves the secret file.
    public void decode(File file) throws IOException, CannotDecodeException
    {
        reset();
        int pos = 0;
        this.setSecretInfo(new HiddenData(this.getHeader()));
        FileOutputStream fos = new FileOutputStream(file);
        do
        {
            fos.write(revealByte(this.i, this.j, secretInfo.pixelsPerByte));
            increment();
            pos++;
        }
        while(pos<secretInfo.length);
        fos.close();
    }

    // Hides one byte in one pixels inside the least significant bits of the pixel colors.
    // if pixelsPerByte == 1: The 8 bits that form the byte are hidden inside the 3 LSBs of red, 2 in the 2 LSBs of green, and 3 in the 3 LSBs of blue
    // if pixelsPerByte == 2: The 4 bits to hide from the 8 bits that form the byte are hidden inside the LSB of red, 2 in the 2 LSBs of green, and  in the LSB of blue
    private void embed(byte b, int i, int j, byte pixelsPerByte)
    {
        int pixelMask = (pixelsPerByte == 1) ? 0xF8 : 0xFE, bitMask = (pixelsPerByte == 1) ? 0x07 : 0x01, shift = (pixelsPerByte == 1) ? 3 : 1;
        Color oldColor = new Color(this.image.getRGB(j,i));
        int red = oldColor.getRed(), green = oldColor.getGreen(), blue = oldColor.getBlue();
        red = red & pixelMask | b & bitMask; b = (byte) (b >> shift);
        green = green & 0xFC | b & 0x03; b = (byte) (b >> 2);
        blue = blue & pixelMask | b & bitMask;
        Color newColor = new Color(red, green,blue);
        this.image.setRGB(j,i,newColor.getRGB());
    }

    // Hides one byte using {@link #embed(byte, int, int, byte)}.
    // if pixelsPerByte == 1
    // => we hide the byte in the pixel of the cover image at the index i,j
    // if pixelsPerByte == 2
    // => we hide the 4 most significant bits of the byte in the pixel (i, j) of the cover image
    // and then we perform a left shift of 4 bits to the byte
    // and we hide the 4 least significant bits of the byte in the opposite pixel of the cover image (height-1-i, width-1-j)
    private void hideByte(byte b, int i, int j, byte pixelsPerByte)
    {
        embed(b, i, j, pixelsPerByte);
        if(pixelsPerByte == 2)
        {
            b = (byte) (b >> 4);
            embed(b, this.image.getHeight()-i-1, this.image.getWidth()-j-1, pixelsPerByte);
        }
    }

    // Reveals one byte hidden inside the (i, j) pixel.
    // if pixelsPerByte == 1: We extract the 3 LSBs in red, 2 LSBs in green and 3 LSBs in blue to form the byte
    // if pixelsPerByte == 2: We extract the LSB in red, 2 LSBs in green and LSB in blue to form the byte
    private byte extract(int i, int j, byte pixelsPerByte)
    {
        int b;
        int pixelMask = (pixelsPerByte == 1) ? 0x07 : 0x01, shift = (pixelsPerByte == 1) ? 3 : 1;
        Color color = new Color(this.image.getRGB(j,i));
        int red = color.getRed(), green = color.getGreen(), blue = color.getBlue();
        b = (blue & pixelMask) ;
        b = b << 2;
        b = b | (green & 0x03);
        b = b << shift;
        b = b | (red & pixelMask);
        return (byte) b;
    }

    // Reveals one byte using extract(int, int, byte)
    // if pixelsPerByte == 1: We extract the byte in pixel (i, j)
    // if pixelsPerByte == 2: We extract the 4 MSBs of the byte in pixel (i, j) and then the 4 LSBs of the byte in pixel (height-1-i, width-1-j)
    private byte revealByte(int i, int j, byte pixelsPerByte)
    {
        byte b = extract(i, j, pixelsPerByte);
        if(pixelsPerByte == 2)
        {
            byte c = extract(this.image.getHeight()-i-1, this.image.getWidth()-j-1, pixelsPerByte);
            c = (byte) (c << 4);
            b = (byte) (c | b);
        }
        return b;
    }

    // Increments the position of the current pixel in the 24-bit, RGB Bitmap image.
    protected void increment()
    {
        this.j++;
        if(this.j==this.image.getWidth()-1)
        {
            this.j=0;
            this.i++;
        }
    }

    // resets index back to (0, 0).
    protected void reset()
    {
        this.i = 0;
        this.j = 0;
    }

}
