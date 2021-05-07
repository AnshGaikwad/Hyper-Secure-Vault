package vault.algorithms;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// The Metadata class is used by GifSteganography and GifSequenceWriter to extract metadata from a gif image
// This data will be used to recreate the gif after embedding the metadata in the image using GifSteganography
class Metadata {

    // Returns an array of BufferedImage of a given gif image.
    public static BufferedImage[] getFrames(File gif) throws IOException
    {
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        ImageInputStream input = ImageIO.createImageInputStream(gif);
        reader.setInput(input);
        int count = reader.getNumImages(true);
        BufferedImage[] frames = new BufferedImage[count];
        for (int index = 0; index < count; index++)
            frames[index] = reader.read(index);
        return frames;
    }

    // Returns an array of IIOMetadata that contains the metadatas of each frame of a given gif image.
    public static IIOMetadata[] getMetadatas(File gif) throws IOException
    {
        ImageReader reader = ImageIO.getImageReadersBySuffix("gif").next();
        ImageInputStream input = ImageIO.createImageInputStream(gif);
        reader.setInput(input);
        int count = reader.getNumImages(true);
        IIOMetadata[] metadatas = new IIOMetadata[count];
        for(int index = 0; index < count; index++)
            metadatas[index] =  reader.getImageMetadata(index);
        return metadatas;
    }

    // Returns the original delay of the gif image (delay of the first frame)
    public static int getDelayMS(File gif) throws IOException
    {
        ImageReader gif_img = ImageIO.getImageReadersBySuffix("gif").next();
        ImageInputStream input = ImageIO.createImageInputStream(gif);
        gif_img.setInput(input);
        IIOMetadata imageMetaData =  gif_img.getImageMetadata(0);
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode)imageMetaData.getAsTree(metaFormatName);
        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        return Integer.parseInt(graphicsControlExtensionNode.getAttribute("delayTime"));
    }

    // Returns an existing child node, or creates and returns a new child node (if the requested node does not exist).
    // rootNode => the IIOMetadataNode to search for the child node
    // nodeName => the name of the child node
    public static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName)
    {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++)
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0)
                return((IIOMetadataNode) rootNode.item(i));
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return(node);
    }

}