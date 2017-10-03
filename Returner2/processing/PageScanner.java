import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class PageScanner implements PlugInFilter
{

    private boolean isQuiz = false;
    private boolean flipped = false;

    private static final int THRESHOLD = 50;
    private static final int RECTANGLE_WIDTH = 77;
    private static final int RECTANGLE_HEIGHT = 211;

    private static final int BORDER_THRESHOLD = 300;
    private static final int REGION_CHECK = 10;
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

    /**
     *
     * @param ip
     *      The order of operations are as follows:
     *      1.) Allign the page (80%)
     *      2.) Find black square. (100%)
     *          This is done with two methods:
     *              - findOrigin()
     *              - establishOrigin()
     *      3.) Scale page to a size
     *      4.) Since the Origin has shifted, we find it again:
     *              - establishOrigin()
     *      3.) Perform checks
     */
    public void run(ImageProcessor ip) {
        int[] pixels = (int[]) ip.getPixels();
        int[] origin;

        if (ip.equals(null)){
            System.out.println("Unsuccessful file read");
            System.exit(0);
        }
        allignPage(ip);

        ip.snapshot();
        /**
        origin = findOrigin(ip);
        if (origin == null) {
            System.out.println("No origin found");
            System.exit(0);
        }

        // now we need a non-scaled version.
        ip.reset();

        if(flipped){
            ip.rotate(180);
        }

        int pixelPosition = establishOrigin(origin[0], origin[1], ip);
        scaleCorrection(pixelPosition,ip);
        // Ensure that the file has correct dimensions for file checks

        ip.snapshot();

        origin = findOrigin(ip);
        if (origin == null) {
            System.out.println("No origin found");
            System.exit(0);
        }

        // now we need a non-scaled version.
        ip.reset();
        pixelPosition = establishOrigin(origin[0], origin[1], ip);
        pixels[pixelPosition] = -18000;
        // write file method?
        */
        if (isQuiz){
            // mark the quiz
        }
        else{
            // check mark allocation
        }

        ip.setPixels(pixels);
        BufferedImage image = ip.getBufferedImage();
        try {
            ImageIO.write(image, "png", new File("out.png"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param ip
     *  This method takes the difference in length from the left edge of the page to the border, uses arctan() to
     *  calculate the angle, and adjusts it with the rotate(angle) method.
     *
     *  We first check at the row with the value of 10% of the height and then 250 rows from that row. If we the
     *  distance is to small or to great from the edge (violates the rules of being an edge), we check similary at the
     *  row with the value of 90% of the height and 250 rows before it.
     *
     * @return int[]
     *  This int[] array is the new array of pixels, which is used to rectify the image
     */
    private void allignPage(ImageProcessor ip) {
        int distanceTop = -1;
        int distanceBot = -1;
        int topStart = (int) Math.round(ip.getHeight() * 0.10);
        int botStart = (int) Math.round(ip.getHeight() * 0.9);
        int[] pixels = (int[]) ip.getPixels();

        System.out.println(ip.getWidth());
        boolean topCheck = true;

        System.out.println("Allign Page");
        System.out.println("Top Row start: " + topStart);

        for (int row = topStart; row <= topStart + 250; row += 250){
            int pixelPosition = row * ip.getWidth();
            for(int test = 0; test != 10; test++) {
                pixels[pixelPosition +test] = -18560;
            }
            for(int collumn = 0; collumn != BORDER_THRESHOLD; collumn ++) {
                if (pixels[pixelPosition + collumn] < BLACK_PIXEL){
                    //now check the next 3 pixels to see if they are also black indicating a border.
                    boolean isBorder = true;

                    borderCheck:{
                        for (int pos = 1; pos<=3; pos ++){
                            if (pixels[pixelPosition + collumn +pos] > BLACK_PIXEL){
                                isBorder = false;
                                break borderCheck;
                            }
                        }
                    }

                    if (isBorder){
                        for(int test = 0; test != 20; test++) {
                            pixels[pixelPosition + collumn +test] = -18000;
                        }
                        if(row == topStart){
                            distanceTop = collumn;
                        }
                        else{
                            distanceBot = collumn;
                        }
                        collumn = BORDER_THRESHOLD-1;
                    }
                }

            }
        }
        if (distanceTop == -1 || distanceBot == -1){
            topCheck = false;
            System.out.println("top border not found");
        }
        if(distanceTop < 80 || distanceBot < 80){
            topCheck = false;
            System.out.println("top border too close to the edge");
        }

        if (!topCheck){
            for (int row = botStart - 250; row <= botStart; row += 250){
                int pixelPosition = row * ip.getWidth();

                for(int collumn = 0; collumn != BORDER_THRESHOLD; collumn ++) {
                    if (pixels[pixelPosition + collumn] < BLACK_PIXEL){
                        //now check the next 3 pixels to see if they are also black indicating a border.
                        boolean isBorder = true;

                        borderCheck:{
                            for (int pos = 1; pos<=3; pos ++){
                                if (pixels[pixelPosition + collumn +pos] > BLACK_PIXEL){
                                    isBorder = false;
                                    break borderCheck;
                                }
                            }
                        }

                        if (isBorder){
                            for(int test = 0; test != 20; test++) {
                                pixels[pixelPosition + collumn +test] = -18000;
                            }
                            if(row == topStart){
                                distanceTop = collumn;
                            }
                            else{
                                distanceBot = collumn;
                            }
                            collumn = BORDER_THRESHOLD-1;
                        }
                    }
                }
            }
        }
        if (distanceTop == -1 || distanceBot == -1){
            topCheck = false;
            System.out.println("top border not found");
            System.out.println("ERROR");
        }
        if(distanceTop < 80 || distanceBot < 80){
            topCheck = false;
            System.out.println("top border too close to the edge");
            System.out.println("ERROR");
        }
        System.out.println("TOP: " + distanceTop);
        System.out.println("BOT: " + distanceBot);

        int diff = distanceTop - distanceBot;

        double radAngle = Math.atan(diff/250.0);
        double Angle =  Math.toDegrees(radAngle);
        System.out.println("Rotate angle: " + Angle);
        ip.rotate(-Angle);
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param ip
     *
     * From the given ImageProcessor, it scales the image down to 20%, and finds the block, returning the
     * co-ordinate of the blocks corner. It also checks the orientation of the page which it corrects, if the page is
     * upside-down
     *
     * The distance to the border must be a minimum of the edge width, which is 78
     *
     * @return int[x,y]
     * Here x and y are the relative co-ordinates on the original ImageProcessor.
     */
    private int[] findOrigin(ImageProcessor ip){

        int[] Origin = new int[2];

        int[] pixels = (int[])ip.getPixels();

        int row;

        int width = ip.getWidth();
        int height = ip.getHeight();
        int newWidth = (int)Math.round(width * 0.2);
        int newHeight = (int)Math.round(height * 0.2);
        int halfWidth = (width - newWidth) / 2;

        boolean rotated = false;

        ip.scale(0.2,0.2);

        for (row = 0; row < newHeight; row++) {
            if( row > newHeight/2){

                if(rotated) {
                    System.out.println("There is no black block on this page FLAG");
                    return null;
                }

                // this implies that the block, if any block at all, is at the bottom of the page
                // an incorrect orientation, so we flip the image and start again.
                ip.rotate(180);
                rotated = true;
                flipped = true;

                row = 0;
            }

            for (int count = halfWidth; count != newWidth + halfWidth; count++) {

                int position = count + row*width + width * (height - newHeight)/2;

                if (pixels[position] < BLACK_PIXEL) {
                    // found a pixel which matches the description
                    // now check if the next row is also the same
                    // i.e. we are determining if this is the black square
                    boolean isBlock = true;

                    blockcheck:
                    {
                        for (int line = 0; line < REGION_CHECK; line++) {
                            for (int collumn = position; collumn < position + REGION_CHECK; collumn++) {

                                if (pixels[width * line + collumn] > BLACK_PIXEL) {
                                    isBlock = false;

                                    break blockcheck;
                                }
                            }
                        }
                    }
                    if (isBlock) {
                        pixels[position] = -180000;// - highlights pixel whose co-ordinate is returned

                        Origin[0] = (int) Math.round((count - halfWidth)/0.2);
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
     * @param ip
     *
     * This method takes the estimated origin which was found on a scaled down image, and now with a guide as to
     * where to look, can establish the origin of the page, the top left corner of the black block.
     *
     * @return int[x,y]
     */
    private int establishOrigin(int x, int y, ImageProcessor ip){

        int[] pixels = (int[])ip.getPixels();

        for (int row = y -10; row != y + 10; row ++) {
            if (row <0) {
                row = 0;
            }

            for (int collumn = x - 10; collumn != x + 10; collumn ++){
                if (collumn <0) {
                    collumn = 0;
                }
                int position = row * ip.getWidth() + collumn;

                if (pixels[position] < BLACK_PIXEL) {
                    boolean isBlock = true;

                    blockcheck:
                    {
                        for (int line = 0; line < REGION_CHECK; line++) {
                            for (int col = 0; col < REGION_CHECK; col++) {

                                if (pixels[position + col + line*ip.getWidth()] > BLACK_PIXEL) {
                                    isBlock = false;
                                    break blockcheck;
                                }
                            }
                        }
                    }

                    if (isBlock) {

                        System.out.println(position);
                        System.out.println("Found true Origin");
                        return  position;
                    }
                }
            }
        }
        System.out.println("Could not find the block on OG image");
        return -1;
    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param pixelPosition
     * @param ip
     *
     * We know that the width of the square should be 77 pixels wide. We also know that the height of the square should
     * be 211. This method now checks what the length of the perceived box is and scales the page accordingly in the
     * correct axis.
     */
    private void scaleCorrection(int pixelPosition, ImageProcessor ip){
        int width = 0;
        int height = 0;
        int start = pixelPosition;
        int[] pixels =(int[]) ip.getPixels();

        while (pixels[start] < BLACK_PIXEL){
            width ++;
            start ++;
        }
        start = pixelPosition;
        while (pixels[start] < BLACK_PIXEL){
            height ++;
            start += ip.getWidth();
        }

        ip.scale(77.0/width,211.0/height);
        System.out.println("Scaled image: " + 77.0/width + ", " + 211.0/height);
        System.out.println(width + ", " + height);

    }
}
