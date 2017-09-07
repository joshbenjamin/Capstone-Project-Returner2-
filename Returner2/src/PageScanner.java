import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class PageScanner implements PlugInFilter
{
    private File file = null;
    private Test test;
    private final int THRESHOLD = 50;
    public Student student;

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
        // we immediately correct potential orientation issues
        int[] pixels = flipPage(ip);
        int width = ip.getWidth();
        int height = ip.getHeight();

        // We assume the page must be oriented until we find the rectangle.

        System.out.println("width: " + width);
        System.out.println("height: " + height);







        // redraws the image with new pixel set.
        ip.setPixels(pixels);
        BufferedImage image = ip.getBufferedImage();
        try {
            ImageIO.write(image, "jpg", new File("out.jpg"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Scans the file which was in the argument for the class.
     *
     * @throws IOException
     */
    public void scan() throws IOException {
        // Only consider front pages..

        BufferedImage image = ImageIO.read(file);

        // test - passed
        System.out.println("Height: "+image.getHeight());
        System.out.println("Width: "+image.getWidth());


    }
    /**
     * Current rectangle co-ords:
     * 171, 265 TL
     * 248, 265 TR
     * 171, 476 BL
     * 248, 476 BR
     */

    private int[] flipPage(ImageProcessor ip){

        int[] pixels = (int[]) ip.getPixels();
        int width = ip.getWidth();
        int offset,i ;
        int pixelCount = 0;
        Rectangle r = ip.getRoi();
        boolean orientation = true;
        /**
         * This loop is only looking for the recatngle
         *
         *
         * A value of -1 for a pixel position indicates whitespace.
         *
         * a value of - 16777216 is a black pixel.
         */
        for (int y = 266; y < (r.y + 475); y += 3) {

            //new row
            offset = y * width;

            for (int x = 172; x < (r.x + 247); x += 3) {
                i = offset + x;

                if (pixels[i] <= -16000000) {
                    pixelCount ++;

                    if (pixelCount >= THRESHOLD) {
                        System.out.println("Found rectangle");
                        orientation = false;
                    }
                }
            }
            if (!orientation){
                break;
            }
        }

        // flip the page
        if (orientation){
            ip.rotate(180);
            pixels = (int[])  ip.getPixels();
            System.out.println("Rotated");
        }
         return pixels;
    }

    /**
     * Asserts that the page is alligned with 90 degrees, else corrects it.
     * @param ip
     * @return
     */
    public int[] allignPage(ImageProcessor ip)
    {
        return null;
    }
    public void scalePage(){
        
    }

    private String getStudentNumber(ImageProcessor ip){
        
        return "";
    }
}
