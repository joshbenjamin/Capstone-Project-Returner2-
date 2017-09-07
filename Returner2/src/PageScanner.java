import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

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
        return DOES_8G+DOES_STACKS+SUPPORTS_MASKING;
    }
    // from the tutorial
    // the image processor turns our image into an array of pixels
    // the processor has to be from the original image - duh

    public void run(ImageProcessor ip) {
        // we immediately correct potential orientation issues
        int[] pixels = flipPage(ip);
        allignPage(ip);
        pixels = getStudentNumber(ip);
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
         * This loop is only looking for the rectangle
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
        System.out.println("Allign Page");

        int[] pixels = (int[]) ip.getPixels();
        Rectangle roi = ip.getRoi();

        int distanceTop = 0;
        int distanceBot = 0;

        for (int y = 1110; y < roi.y + roi.height; y += 1110) {

            int offset = y*ip.getWidth();

            for (int x = roi.x; x < roi.x+roi.width; x++) {

                int pos = offset+x;
                if (y == 1110)
                {
                    if(pixels[pos] > -100) { //check if the pixel is white
                        distanceTop ++;
                    }
                    else {
                        break;
                    }
                }
                if (y == 2220) {
                    if (pixels[pos] > -100) { //check if the pixel is white
                        distanceBot++;
                    } else {
                        break;
                    }
                }
            }
        }
        System.out.println("TOP: "+distanceTop);
        System.out.println("BOT: "+distanceBot);

        int diff = distanceTop - distanceBot;

        double radAngle = Math.atan(diff);
        double Angle =  Math.toDegrees(radAngle);

        ip.rotate(Angle);

        return (int[])ip.getPixels();
    }
    public void scalePage(){
        
    }

    private int[] getStudentNumber(ImageProcessor ip){
        int[] pixels = (int[]) ip.getPixels();
        int width = ip.getWidth();
        boolean numbers = false;
        /**
         * start of first block (927, 527)
         * blocks are 30x30
         * have 29 pixels between them on x axis
         * have 9 pixels between them on y axis
         */

        // outer loop gets us to top left corner of each box

        for(int collumn = 934; collumn < 1445; collumn += 59){

            if(collumn >= 934+59*6) {
                numbers = true;
            }
            for(int row = 527; row < 1560; row += 40){
                // now we are in a box. Check if pixels are here -> map selected
                if (numbers && row >= 527+400) {
                    break;
                }
                for (int y = row; y < row +30; y ++) {
                    int offset = y*width;
                    for (int x = collumn; x < collumn + 30; x ++){
                        // working with a pixel in the box
                        int i = offset + x;
                        pixels[i] = - 1800000;
                    }
                }
            }
        }

        return pixels;
    }
}
