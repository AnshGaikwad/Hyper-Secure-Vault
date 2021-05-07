package vault.algorithms;

import vault.exceptions.CannotDecodeException;
import vault.exceptions.CannotEncodeException;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


// Handles the steganographic process for GIF89a images using the Least Significant Bit (LSB) method.
// Due to the limited Colormap of each pixel in a gif (255 colors instead of 16 million colors), each byte is hidden inside the LSB of 8 consecutive pixels
// The gif steganography process is optimized for hiding small data, as large data can heavily alter the gif stego image.
public class GifSteganography extends BaseSteganography
{
    // Array of individual gif frames
    private final BufferedImage[] frames;

    // Array of gif frames metadatas
    private final IIOMetadata[] metadatas;

    // Delay of first gif frame
    private final int delayMS;

    // z-index current position (index of current frame)
    private int k=0;

    // Creates a GifSteganography object to perform embedding or extraction of data in GIF89a images
    public GifSteganography(File input, boolean isEncrypted, boolean isCompressed) throws IOException
    {
        this.isEncrypted = isEncrypted;
        this.isCompressed = isCompressed;
        this.frames = Metadata.getFrames(input);
        this.metadatas = Metadata.getMetadatas(input);
        this.delayMS = Metadata.getDelayMS(input);
        this.pixelsPerByte = 8;
        this.setCapacity(this.frames);
    }

    // Creates a GifSteganography object to perform embedding or extraction of data in GIF89a images
    public GifSteganography(File input) throws IOException
    {
        this(input, false, false);
    }

    // Sets the capacity field by adding up the capacity of each frame of the gif image.
    private void setCapacity(BufferedImage[] frames)
    {
        for (BufferedImage bi : frames)
            this.capacity += (long) bi.getHeight() * bi.getWidth();

        this.capacity/=8;
    }

    // Returns the header that contains information about the data embedded in the stego image.
    public byte[] getHeader() throws CannotDecodeException
    {
        int b;
        int mode = revealByte();
        List<Byte> header = new ArrayList<>();

        if (mode != (byte) 'M' && mode != (byte) 'D')
            throw new CannotDecodeException("There is nothing embedded in this image");

        reset();

        do
        {
            b = revealByte();
            header.add((byte) b);
        }
        while(b != (byte) '!');
        return Utils.toByteArray(header);
    }

    // Writes the data contained in a byte array or header (here) to the first pixels of the image
    private void writeHeader(byte[] header)
    {
        for(byte b : header)
            hideByte(b);
    }

    // Calls the writeHeader(byte[]) to write the message header into the gif stego image,
    // then encodes the message to hide using hideByte(byte) byte by byte,
    // then saves the stego gif image using GifSequenceWriter
    public void encode(byte [] message, File output) throws IOException, CannotEncodeException
    {
        this.writeHeader(this.setHeader(message));

        for(byte b : message)
            hideByte(b);

        ImageOutputStream ios = new FileImageOutputStream(output);
        ColorModel cm = this.frames[0].getColorModel();
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(cm, cm.createCompatibleSampleModel(1, 1));
        GifSequenceWriter writer = new GifSequenceWriter(ios, imageType, this.delayMS, true);

        for(int x=0; x<this.frames.length; x++)
            writer.writeToSequence(this.frames[x], this.metadatas[x]);

        writer.close();
        ios.close();
    }

    // Calls the writeHeader(byte[]) to write the message header into the gif stego image,
    // then encodes the message to hide using hideByte(byte) byte by byte,
    // then saves the stego gif image using GifSequenceWriter
    public void encode(File doc, File output) throws IOException, CannotEncodeException
    {
        this.writeHeader(this.setHeader(doc));
        FileInputStream fis = new FileInputStream(doc);
        byte[] buffer = new byte[1024];

        while(fis.read(buffer) > 0)
            for(byte b : buffer)
                hideByte(b);

        ImageOutputStream ios = new FileImageOutputStream(output);
        ColorModel cm = this.frames[0].getColorModel();
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(cm, cm.createCompatibleSampleModel(1, 1));
        GifSequenceWriter writer = new GifSequenceWriter(ios, imageType, this.delayMS, true);

        for(int x=0; x<this.frames.length; x++)
            writer.writeToSequence(this.frames[x], this.metadatas[x]);

        writer.close();
        ios.close();
    }

    // Extracts the header from the stego image to create the secretInfo field,
    // then decodes the hidden data in the image using revealByte(),
    // then saves the secret file
    public void decode(File file) throws IOException, CannotDecodeException
    {
        reset();
        int pos = 0;
        this.setSecretInfo(new HiddenData(this.getHeader()));
        FileOutputStream fos = new FileOutputStream(file);
        do
        {
            fos.write(revealByte());
            pos++;
        }
        while(pos<secretInfo.length);
        fos.close();
    }

    // Embeds one bit into the least significant bit of a pixel.
    private int embed(int pixel, char c)
    {
        String before = String.format("%8s", Integer.toBinaryString(pixel)).replace(' ','0');
        String after = before.substring(0,7)+c;
        return Integer.parseInt(after,2);
    }

    // Hides one byte into the least significant bit of 8 consecutive pixels using embed(int, char).
    private void hideByte(byte b)
    {
        int[] pixel = new int[4];
        String currentByte;
        for(int l=0; l<8; l++)
        {
            WritableRaster raster = this.frames[k].getRaster();
            raster.getPixel(j,i,pixel);
            currentByte = String.format("%8s",Integer.toBinaryString(b)).replace(' ', '0');
            currentByte = currentByte.substring(currentByte.length()-8, currentByte.length());
            pixel[0] = embed(pixel[0], currentByte.charAt(l));
            raster.setPixel(j,i,pixel);
            increment();
        }
    }

    // Reveals one byte embedded into 8 consecutive pixels.
    // Returns the hidden byte.
    private byte revealByte()
    {
        int[] pixel = new int[4];
        int b;
        String currentByte;
        StringBuilder bit = new StringBuilder();

        for(int l=0; l<8; l++)
        {
            Raster raster = this.frames[k].getRaster();
            raster.getPixel(j,i,pixel);
            currentByte = String.format("%8s", Integer.toBinaryString(pixel[0])).replace(" ", "0");
            currentByte = currentByte.substring(currentByte.length()-8, currentByte.length());
            bit.append(currentByte.charAt(7));
            increment();
        }

        b = Integer.parseInt(bit.toString(),2);
        return (byte) b;
    }

    // Increments the x, y and z axis position in a GIF89a image.
    // Note : i and j represent the position of the pixel in a gif frame,
    // and z the index of the frame currently processed
    protected void increment()
    {
        this.j++;
        if(this.j == this.frames[this.k].getWidth())
        {
            this.j=0;this.i++;
        }

        if(this.i == this.frames[this.k].getHeight())
        {
            this.j=0;this.i=0;this.k++;
        }
    }

    // Resets the index back to (0, 0, 0).
    protected void reset(){ this.i = 0; this.j = 0; this.k = 0; }

}