package vault.algorithms;

import vault.exceptions.CannotDecodeException;
import vault.exceptions.CannotEncodeException;
import vault.exceptions.UnsupportedImageTypeException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Handles the steganographic process of embedding a 24-bit, RGB Bitmap image into another using the Least Significant Bit (LSB) method.
// We can either hide the 3 most significant bits of the secret image (for maximum secret image size) or the 6 most significant bits (for better secret image quality).
public class ImageInImageSteganography extends ImageSteganography
{

    // Encoding mode (1 or 2 pixels/pixel) (3 or 6 hidden image MSBs)
    private byte pixelsPerPixel;

    // Creates an ImageInImageSteganography object to perform embedding/extraction of another image
    public ImageInImageSteganography(File input, byte pixelsPerPixel)
            throws IOException, UnsupportedImageTypeException
    {
        super(input);
        this.pixelsPerPixel = pixelsPerPixel;
    }

    // Creates an ImageInImageSteganography object to perform embedding/extraction of another image
    public ImageInImageSteganography(File input) throws IOException, UnsupportedImageTypeException
    {
        this(input,(byte) 1);
    }

    // Sets the class header field with information about the secret image to embed in the cover image.
    // The header contains the following : ['I', pixels/pixel (1|2), Width (2 bytes), Height (2 bytes), '!']
    // Example: [01001001, 00000010, 00000101, 00000000, 00000010, 11010000, 00100001]
    // Corresponds to: ['I' (Image), 2, 1280 , 720, '!']
    byte[] setHeader(File file) throws IOException
    {
        List<Byte> header = new ArrayList<>();
        header.add((byte)'I');
        header.add(pixelsPerPixel);
        BufferedImage bimg = ImageIO.read(file);
        String width = String.format("%16s",Long.toBinaryString(bimg.getWidth())).replace(' ', '0');
        String height = String.format("%16s",Long.toBinaryString(bimg.getHeight())).replace(' ', '0');
        for(int i=0; i<width.length();i+=8)
            header.add((byte)Integer.parseInt(width.substring(i,i+8),2));
        for(int i=0; i<height.length();i+=8)
            header.add((byte)Integer.parseInt(height.substring(i,i+8),2));
        header.add(((byte) '!'));
        this.header = Utils.toByteArray(header);
        return Utils.toByteArray(header);
    }

    // Calls writeHeader(byte[]) to write the the image header to the stego image,
    // then encodes the image using hidePixel(int, int) pixel per pixel,
    // then saves the stego image.
    public void encode(File img, File output) throws IOException, CannotEncodeException
    {
        this.writeHeader(this.setHeader(img));
        BufferedImage imageToHide = ImageIO.read(img);
        int sourceWidth = this.image.getWidth(), sourceHeight = this.image.getHeight();
        int embedWidth = imageToHide.getWidth(), embedHeight = imageToHide.getHeight();
        int pos = ((this.header.length)/(sourceWidth))+1;
        if (embedHeight + pos > sourceHeight || embedWidth > sourceWidth)
            throw new CannotEncodeException("Secret image larger than source image.");
        for (int k=pos; k< embedHeight+pos; k++)
        {
            for (int l = 0; l < embedWidth; l++)
            {
                this.image.setRGB(l, k, hidePixel(this.image.getRGB(l, k), imageToHide.getRGB(l, k - pos)));
                if (this.pixelsPerPixel == 2)
                {
                    this.image.setRGB(sourceWidth - l - 1, sourceHeight - k - 1, hidePixel(this.image.getRGB(sourceWidth - l - 1, sourceHeight - k - 1), imageToHide.getRGB(l, k - pos) << 3));
                    if (sourceWidth - l - 1 <= l && sourceHeight - k - 1 <= k)
                        throw new CannotEncodeException("Secret image too large.");
                }
            }
        }
        ImageIO.write(this.image,"png",output);
    }

    // Extracts the header from the stego image to create the {@link #secretInfo} field,
    // then decodes the hidden image using {@link #revealPixel(int)},
    // then saves the secret image.
    public void decode(File result) throws IOException, CannotDecodeException
    {
        reset();
        int sourceWidth = this.image.getWidth(), sourceHeight = this.image.getHeight();
        this.setSecretInfo(new HiddenData(this.getHeader()));
        int pos = (this.header.length/sourceWidth)+1;
        BufferedImage hiddenImage = new BufferedImage(secretInfo.width, secretInfo.height, BufferedImage.TYPE_INT_RGB);
        for (int k=pos ;k<secretInfo.height+pos ;k++)
        {
            for (int l = 0; l < secretInfo.width; l++)
            {
                int pixel = revealPixel(this.image.getRGB(l, k));
                if (secretInfo.pixelsPerPixel == 2)
                {
                    int pixel2 = revealPixel(this.image.getRGB(sourceWidth - l - 1, sourceHeight - k - 1));
                    pixel = pixel | (pixel2 >> 3);
                }
                hiddenImage.setRGB(l, k - pos, pixel);
            }
        }
        ImageIO.write(hiddenImage,"png",result);
    }

    // Hides The 3 MSBs of the red, green and blue color components of pixelB.
    // in the 3 LSBs of the red, green and blue color components of pixelA.
    // pixelA => cover pixel
    // pixelB => pixel to hide
    // return => a pixel that contains another hidden pixel.
    private int hidePixel(int pixelA, int pixelB)
    {
        return pixelA & 0xFFF8F8F8 | (pixelB & 0x00E0E0E0) >> 5;
    }

    // Reveals the content hidden inside the 3 LSBs of each color component of pixel
    // pixel => pixel to extract hidden pixel from
    // return => the 3 MSBs of secret pixel
    private int revealPixel(int pixel) { return (pixel & 0xFF070707) << 5; }
}
