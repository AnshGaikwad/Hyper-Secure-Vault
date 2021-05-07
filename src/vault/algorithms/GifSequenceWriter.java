package vault.algorithms;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;

// Creates a GIF89a image based on an array of BufferedImage and an array of IIOMetadata
class GifSequenceWriter {

    // Used to write a gif image
    private final ImageWriter gifWriter;

    // Describes how the gif will be written
    private final ImageWriteParam imageWriteParam;

     // Creates a new GifSequenceWriter
     // outputStream        => the ImageOutputStream to be written to
     // imageTypeSpecifier  => the ImageTypeSpecifier of the image
     // timeBetweenFramesMS => the time between frames in milliseconds
     // loopContinuously    => whether the gif should loop repeatedly
    public GifSequenceWriter
    (
            ImageOutputStream outputStream,
            ImageTypeSpecifier imageTypeSpecifier,
            int timeBetweenFramesMS,
            boolean loopContinuously
    ) throws IOException
    {

        // my method to create a writer
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();

        IIOMetadata imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier,
                imageWriteParam);

        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode)
                imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = Metadata.getNode(
                root,
                "GraphicControlExtension");

        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute(
                "transparentColorFlag",
                "FALSE");
        graphicsControlExtensionNode.setAttribute(
                "delayTime",
                Integer.toString(timeBetweenFramesMS / 10));
        graphicsControlExtensionNode.setAttribute(
                "transparentColorIndex",
                "0");

        IIOMetadataNode commentsNode = Metadata.getNode(root, "CommentExtensions");
        commentsNode.setAttribute("CommentExtension", "Created by MAH");

        IIOMetadataNode appEntensionsNode = Metadata.getNode(
                root,
                "ApplicationExtensions");
        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");
        int loop = loopContinuously ? 0 : 1;

        child.setUserObject(new byte[]{0x1, (byte) (loop & 0xFF),
                (byte) ((loop >> 8) & 0xFF)});

        appEntensionsNode.appendChild(child);

        imageMetaData.setFromTree(metaFormatName, root);

        gifWriter.setOutput(outputStream);

        gifWriter.prepareWriteSequence(null);
    }

    // Returns the first available GIF ImageWriter using
    // ImageIO.getImageWritersBySuffix("gif")
    private static ImageWriter getWriter() throws IIOException
    {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext())
        {
            throw new IIOException("No GIF Image Writers Exist");
        } else
        {
            return iter.next();
        }
    }

    // Adds a frame to the gif sequence.
    public void writeToSequence(RenderedImage img, IIOMetadata originalImageMetaData)
            throws IOException
    {
        gifWriter.writeToSequence(
                new IIOImage(
                        img,
                        null,
                        originalImageMetaData),
                imageWriteParam);
    }


    // Close this GifSequenceWriter object. This does not close the underlying
    // stream, just finishes off the GIF.
    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }

}