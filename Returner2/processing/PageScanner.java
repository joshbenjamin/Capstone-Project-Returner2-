import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class PageScanner implements PlugInFilter
{

    private boolean isQuiz = false;
    private boolean flipped = false;

    private static final int THRESHOLD = 50;
    private static final int RECTANGLE_WIDTH = 77;
    private static final int RECTANGLE_HEIGHT = 211;

    private static final int BLACK_PIXEL = -8000000;

    // Object Constructor Method
    // Let the test know whether there are quiz answers or not

    public PageScanner(Boolean isQuiz){
        this.isQuiz = isQuiz;
    }

    public int setup(String arg, ImagePlus imp)
    {
        return DOES_8G+DOES_STACKS+SUPPORTS_MASKING;
    }
    // The image processor turns our image into an array of pixels
    // the processor has to be from the original image - duh

    // The order of operations are as follows:
    //      1.) Allign the page (80%)
    //      2.) Find black square.
    //      2.) Check/Correct page orientation
    //      3.) Perform checks
    //
    public void run(ImageProcessor ip) {
        // we immediately correct potential orientation issues
        int[] pixels;
        int[] origin;

        /***TODO
         *  do null check etc
         */


        pixels = allignPage(ip);

        ip.snapshot();

        origin = findOrigin(ip);

        // now we need a non-scaled version.
        ip.reset();

        if(flipped)
        {
            ip.rotate(180);
        }
        establishOrigin(origin[0], origin[1], ip);

        // write file method?


        ip.setPixels(pixels);
        BufferedImage image = ip.getBufferedImage();
        try {
            ImageIO.write(image, "png", new File("out.png"));
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
     *
     * If the rectangle is not found, the page is flipped.
     */

    public int[] allignPage(ImageProcessor ip)
    {
        /**TODO
         * Spli the check if line was found to seperate method
         *
         * only consider points that have next 3 pixels also black. ;)
         *
         */
        /**
         *  This method takes the difference in length from the left edge
         *  of the page to the border, uses arctan() to calculate the
         *  angle, and adjusts it with the rotate(angle) method.
         */
        System.out.println("Allign Page");

        int[] pixels = (int[]) ip.getPixels();
        boolean isLine = false;
        Rectangle roi = ip.getRoi();

        int distanceTop = 0;
        int distanceBot = 0;

        for (int y = 700; y < roi.y + roi.height; y += 200) {

            int offset = y*ip.getWidth();

            for (int x = roi.x; x < roi.x+roi.width; x++) {

                int pos = offset+x;
                if (y == 700)
                {
                    if(pixels[pos] > -15000000) { //check if the pixel is white
                        distanceTop ++;
                    }
                    else {
                        for (int i = 0; i !=100 ;i++) {
                            pixels[pos + i] = -1800000;
                        }
                        break;
                    }
                }
                if (y == 900) {
                    if (pixels[pos] > -15000000) { //check if the pixel is white

                        distanceBot++;
                    } else {
                        for (int i = 0; i !=100 ;i++) {
                            pixels[pos + i] = -1800000;
                        }
                        break;
                    }
                }
            }
        }

        System.out.println("top: "+distanceTop +"" +
                "\nbot: "+distanceBot);
        int pixelThresh = (int) Math.round(1110*0.7);
        int foundPixels = 0;
        int interpCount = 1;
        /**
         * Tests to see if we actually encountered the side border and not just some scanned noise.
         */
        int interpInterval = Math.abs(distanceTop - distanceBot)/1110;
        int x = distanceTop;

        System.out.println("Interpolation Interval: " + interpInterval);

        for (int row = 700; row != 900; row++) {
            int offset = row*ip.getWidth();

            if(pixels[x + offset] < -1000000){
                foundPixels ++;
            }
            // actual interpolation
            if (row - 1110 > interpInterval*interpCount){
                if(distanceTop > distanceBot){
                    x--;
                }
                else if(distanceTop < distanceBot){
                    x++;
                }
                else{
                    continue;
                }
                interpCount ++;
            }
        }
        System.out.println("FoundPixels: " + foundPixels);
        if (foundPixels >= pixelThresh) {
            isLine = true;
            System.out.println("Is Line?: " + isLine);
        }

        else{
            // improve line search algorithm
        }
        System.out.println("TOP: "+distanceTop);
        System.out.println("BOT: "+distanceBot);

        int diff = distanceTop - distanceBot;

        double radAngle = Math.atan(diff/200.0);
        double Angle =  Math.toDegrees(radAngle);
        System.out.println("Rotate angle: " + Angle);
        ip.rotate(-Angle);

        return (int[])ip.getPixels();
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param ip
     *
     * From the given ImageProcessor, it finds scales the image down to 20%, and finds the block, returning the
     * co-ordinate of the blocks corner. It also checks the orientation of the page which it corrects, if the page is
     * upside-down
     *
     * @return int[x,y]
     * Here x and y are the relative co-ordinates on the original ImageProcessor.
     */
    private int[] findOrigin(ImageProcessor ip){

        int[] Origin = new int[2];

        int[] pixels = (int[])ip.getPixels();
        //int[] originalPixels = pixels.clone();

        int row;

        int width = ip.getWidth();
        int height = ip.getHeight();
        int newWidth = (int)Math.round(width * 0.2);
        int newHeight = (int)Math.round(height * 0.2);

        boolean rotated = false;

        ip.scale(0.2,0.2);

        for (row = 0; row < newHeight; row++) {
            if( row > newHeight/2){

                if(rotated) {
                    return null;
                }

                // this implies that the block, if any block at all, is at the bottom of the page
                // an incorrect orientation, so we flip the image and start again.
                ip.rotate(180);
                rotated = true;
                flipped = true;

                row = 0;
            }

            for (int count = (width - newWidth) / 2; count != newWidth + (width - newWidth) / 2; count++) {

                int position = count + row*width + width * (height - newHeight)/2;

                if (pixels[position] < BLACK_PIXEL) {
                    // found a pixel which matches the description
                    // now check if the next row is also the same
                    // i.e. we are determining if this is the black square
                    boolean isBlock = true;

                    blockcheck:
                    {
                        for (int line = 0; line < 10; line++) {
                            for (int collumn = position; collumn < position + 15; collumn++) {

                                if (pixels[width * line + collumn] > BLACK_PIXEL) {
                                    isBlock = false;

                                    break blockcheck;
                                }
                            }
                        }
                    }
                    if (isBlock) {
                        pixels[position] = -180000;// - highlights pixel whose co-ordinate is returned

                        Origin[0] = (int) Math.round((count - (width - newWidth) / 2)/0.2);
                        Origin[1] = (int) Math.round(row/0.2);

                        return  Origin;
                    }
                }
            }
        }

        return null;
    }
    //------------------------------------------------------------------------------------------------------------------

    // This method does not change, even if it is not a quiz
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
    //------------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param x x co-ordinate of estimated origin
     * @param y y co-ordinate of estimated origin
     *
     * This method takes the estimated origin which was found on a scaled down image, and now with a guide as to
     * where to look, can establish the origin of the page, the top left corner of the black block.
     *
     * @return int[x,y]
     */
    private int establishOrigin(int x, int y, ImageProcessor ip){

        System.out.println(x + ", " + y);

        int[] pixels = (int[])ip.getPixels();


        for (int row = y -20; row != y + 20; row ++) {
            for (int collumn = x - 20; collumn != x + 20; collumn ++){

                int position = row * ip.getWidth() + collumn;

                if (pixels[position] < BLACK_PIXEL) {
                    boolean isBlock = true;

                    blockcheck:
                    {
                        for (int line = 0; line < 10; line++) {
                            for (int col = 0; col < 15; col++) {

                                if (pixels[position + col + line*ip.getWidth()] > BLACK_PIXEL) {
                                    isBlock = false;
                                    break blockcheck;
                                }
                            }
                        }
                    }

                    if (isBlock) {
                        System.out.println("Found it");
                        System.out.println(collumn + ", " + row);
                        pixels[position] = -180000;
                        System.out.println(position);
                        return  position;
                    }
                }
            }
        }
        return -1;
    }
}
