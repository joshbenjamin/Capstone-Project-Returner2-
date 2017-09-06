import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;

import javax.imageio.ImageIO;

import ij.plugin.filter.PlugInFilter;
import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.*;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;


public class PageScanner implements PlugInFilter
{
    private File file = null;
    private Test test;

    public PageScanner(File image, Test test){
        this.file = image;
        this.test = test;
    }

    // still need to learn whats going on here
    public int setup(String arg, ImagePlus imp)
    {
        if (arg.equals("about"))
        {
            return DONE;
        }
        return DOES_8G+DOES_STACKS+SUPPORTS_MASKING;
    }
    // from the tutorial
    // the image processor turns our image into an array of pixels
    // the processor has to be from the original image - duh
    public void run(ImageProcessor ip) {
        int[] pixels = (int[]) ip.getPixels();
        System.out.println(pixels.length);
        int width = ip.getWidth();
        Rectangle r = ip.getRoi();

        int offset, i;
        //goes down the rows
        for (int y = r.y; y < (r.y + r.height); y++) {
            // since pixels is all just one long row, splits them by adding the Y*width
            offset = y * width;
            for (int x = r.x; x < (r.x + r.width); x++) {
                i = offset + x;
                //System.out.println(pixels[i]);
                pixels[i] = (byte) (16777216 - pixels[i]);
            }
        }
        ip.setPixels(pixels);
        BufferedImage image = ip.getBufferedImage();
        try {
            ImageIO.write(image, "jpg", new File("/home/dawie/Documents/Work/2017/CSC3003S/out.jpg"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Scans the file which was in the argument for the class
     *
     * @throws IOException
     */
    public void scan() throws IOException {
        // Only consider front pages..

        BufferedImage image = ImageIO.read(file);

        // test - passed
        System.out.println("Height: "+image.getHeight());
        System.out.println("Width: "+image.getWidth());

         /**TODO
         * Current rectangle co-ords:
         * 171, 265 TL
         * 248, 265 TR
         * 171, 476 BL
         * 248, 476 BR
         */

    }
    
    public void orientatePage(){
        
    }
    
    public void scalePage(){
        
    }
}
