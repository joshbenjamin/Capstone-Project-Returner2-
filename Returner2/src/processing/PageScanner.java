package processing;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PageScanner implements PlugInFilter
{

    private boolean isQuiz = false;
    private boolean flipped = false;

    private static final int VERTICAL_GAP = 8;
    private static final int HORIZONTAL_GAP = 26;
    private static final int BORDER_THICKNESS = 1;
    private static final int BOX_WIDTH = 29;
    private static final int BOX_HEIGHT = 29;
    private static final int BORDER_THRESHOLD = 300;
    private static final int REGION_CHECK = 10;
    private static final int BLACK_PIXEL = -8000000;

    private static final double RECTANGLE_WIDTH = 77.0;
    private static final double RECTANGLE_HEIGHT = 221.0;

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

        allignPage(ip);

        try {
            writeFile(ip);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        //ensures that we can revert back ti the original, non-scaled down version
        ip.snapshot();
        // find origin region
        origin = findOrigin(ip);

        if (origin == null) {
            System.out.println("No origin found");
            System.exit(0);
        }

        // now we need a non-scaled version.
        ip.reset();



        int pixelPosition = establishOrigin(origin[0], origin[1], ip);
        scaleCorrection(pixelPosition,ip);
        try {
            writeFile(ip);
        }
        catch (IOException e){
            e.printStackTrace();
        }
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

        int[][] test = getStudentNumber(pixelPosition, ip);

        if (isQuiz){
            // mark the quiz
        }
        else{

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
        int distanceMid = -1;
        int topStart = (int) Math.round(ip.getHeight() * 0.10);
        int midStart = ip.getHeight()/2;
        int botStart = (int) Math.round(ip.getHeight() * 0.9);

        int[] pixels = (int[]) ip.getPixels();

        boolean topBorder = true;
        boolean midBorder = true;
        boolean botBorder = true;
        System.out.println(ip.getWidth());

        System.out.println("Allign Page");
        System.out.println("Top Row start: " + topStart);

        // measures distance from edge to the border  at the topStart row.
        topRowCheck:{
            int pixelPosition = topStart * ip.getWidth();
            /** TODO
             * Remove this
             */
            for (int test = 0; test != 10; test++) {
                pixels[pixelPosition + test] = -18560;
            }

            for (int column = 0; column != BORDER_THRESHOLD; column++) {
                if (pixels[pixelPosition + column] < BLACK_PIXEL) {
                    //now check the next 3 pixels to see if they are also black indicating a border.
                    boolean isBorder = true;

                    // determines if pixel found is not just noise.
                    borderCheck:
                    {
                        for (int pos = 1; pos <= 3; pos++) {
                            if (pixels[pixelPosition + column + pos] > BLACK_PIXEL) {
                                isBorder = false;
                                break borderCheck;
                            }
                        }
                    }

                    if (isBorder) {
                        for (int test = 0; test != 20; test++) {
                            pixels[pixelPosition + column + test] = -18000;
                        }
                        topBorder = true;
                        distanceTop = column;
                        break topRowCheck;
                    }
                    topBorder = false;
                }
            }
        }
        botRowCheck:{
            int pixelPosition = botStart * ip.getWidth();
            /** TODO
             * Remove this
             */
            for (int test = 0; test != 10; test++) {
                pixels[pixelPosition + test] = -18560;
            }

            for (int column = 0; column != BORDER_THRESHOLD; column++) {
                if (pixels[pixelPosition + column] < BLACK_PIXEL) {
                    //now check the next 3 pixels to see if they are also black indicating a border.
                    boolean isBorder = true;

                    // determines if pixel found is not just noise.
                    borderCheck:
                    {
                        for (int pos = 1; pos <= 3; pos++) {
                            if (pixels[pixelPosition + column + pos] > BLACK_PIXEL) {
                                isBorder = false;
                                break borderCheck;
                            }
                        }
                    }

                    if (isBorder) {
                        for (int test = 0; test != 20; test++) {
                            pixels[pixelPosition + column + test] = -18000;
                        }
                        botBorder = true;
                        distanceBot = column;
                        break botRowCheck;
                    }
                    botBorder = false;
                }
            }
        }
        midRowCheck:{
            int pixelPosition = midStart * ip.getWidth();
            /** TODO
             * Remove this
             */
            for (int test = 0; test != 10; test++) {
                pixels[pixelPosition + test] = -18560;
            }

            for (int column = 0; column != BORDER_THRESHOLD; column++) {
                if (pixels[pixelPosition + column] < BLACK_PIXEL) {
                    //now check the next 3 pixels to see if they are also black indicating a border.
                    boolean isBorder = true;

                    // determines if pixel found is not just noise.
                    borderCheck:
                    {
                        for (int pos = 1; pos <= 3; pos++) {
                            if (pixels[pixelPosition + column + pos] > BLACK_PIXEL) {
                                isBorder = false;
                                break borderCheck;
                            }
                        }
                    }

                    if (isBorder) {
                        for (int test = 0; test != 20; test++) {
                            pixels[pixelPosition + column + test] = -18000;
                        }

                        distanceMid = column;
                        midBorder = true;
                        break midRowCheck;
                    }
                    midBorder = false;
                }
            }
        }
        if (botBorder && topBorder){

            int rows = botStart - topStart;
            System.out.println("Measuring top and bottom");
            System.out.println("TOP: " + distanceTop +
                    "\nBOT: " + distanceBot);

            int diff = distanceTop - distanceBot;

            double radAngle = Math.atan(diff * 1.0/rows);
            double Angle =  Math.toDegrees(radAngle);

            System.out.println("Rotate angle: " + Angle);
            ip.rotate(-Angle);
        }
        else if(botBorder && midBorder){
            int rows = botStart - midStart;
            System.out.println("top border too close to the edge");
            System.out.println("so we take mid and bot");

            int diff = distanceMid - distanceBot;
            double radAngle = Math.atan(diff * 1.0/rows);
            double Angle =  Math.toDegrees(radAngle);
            System.out.println("Rotate angle: " + Angle);
            ip.rotate(-Angle);
        }
        else if(topBorder && midBorder){
            int rows = midStart - topStart;
            System.out.println("bot border too close to the edge");
            System.out.println("so we take MID and TOP");

            int diff = distanceTop - distanceMid;
            double radAngle = Math.atan(diff * 1.0/rows);
            double Angle =  Math.toDegrees(radAngle);
            System.out.println("Rotate angle: " + Angle);
            ip.rotate(-Angle);
        }
        else{
            System.out.println("Couldn't Rotate");
        }
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
    public int[][] getStudentNumber(int originPixel, ImageProcessor ip){

        int[] pixels = (int[]) ip.getPixels();

        int currentPixelPoint = originPixel + 754 + (222 * ip.getWidth());

        // outer loop goes through boxes
        for (int column = 0; column != 9; column ++){

            int count = 0;

            if (column %2 == 0){
                currentPixelPoint += 1;
            }

            for (int row = 0; row != 26; row ++){
                if (column >= 6 && row >= 10){
                    continue;
                }
                count ++;
                if (count % 5 ==0){
                    currentPixelPoint -= ip.getWidth();
                }
                // shifts it down by a box and its space
                currentPixelPoint += ip.getWidth() * (BORDER_THICKNESS*3 + BOX_HEIGHT + VERTICAL_GAP);

                // iterates through actual pixel block
                for(int pixelRow = 2; pixelRow != BOX_HEIGHT - 3; pixelRow ++){
                    for(int pixelColumn = 2; pixelColumn != BOX_WIDTH-3; pixelColumn ++){


                        pixels[currentPixelPoint + ip.getWidth()*pixelRow + pixelColumn] = -18000;
                    }
                }
            }
            if (column >= 6){
                currentPixelPoint -= 10 * ip.getWidth() * (BORDER_THICKNESS*3 + BOX_HEIGHT + VERTICAL_GAP);
                currentPixelPoint += (BORDER_THICKNESS*3 + BOX_WIDTH + HORIZONTAL_GAP);
                currentPixelPoint += 2*ip.getWidth();
            }
            else {
                currentPixelPoint -= 26 * ip.getWidth() * (BORDER_THICKNESS * 3 + BOX_HEIGHT + VERTICAL_GAP);
                currentPixelPoint += (BORDER_THICKNESS * 3 + BOX_WIDTH + HORIZONTAL_GAP);
                currentPixelPoint += 5 * ip.getWidth();
            }
        }

        return null;
    }
    //------------------------------------------------------------------------------------------------------------------
    public int[][] getQuizAnswers(int originPixel, ImageProcessor ip){
        //start of A1 block is 581, 1770

        int[] pixels = (int[]) ip.getPixels();

        int currentPixelPoint = originPixel + 754 + (222 * ip.getWidth());


        return null;
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

        int startWidth = pixelPosition + 50 * ip.getWidth();
        int startHeight = pixelPosition + 30;

        int[] pixels =(int[]) ip.getPixels();

        while (pixels[startWidth] < BLACK_PIXEL){
            width ++;
            startWidth ++;
        }

        while (pixels[startHeight] < BLACK_PIXEL){
            height ++;
            startHeight += ip.getWidth();
        }

        //ip.scale(77.0/width,211.0/height);

        System.out.println("Scaled image: " + RECTANGLE_WIDTH/width +
                ", " + RECTANGLE_HEIGHT/height);
        System.out.println(width + ", " + height);

    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param ip
     * @throws IOException
     *
     * This method is to be used once the checks have been completed. It is the end result of correcting orientation
     * as well as marking front pages.
     */
    public void writeFile(ImageProcessor ip) throws IOException{
        BufferedImage image = ip.getBufferedImage();
        ImageIO.write(image, "png", new File("out.png"));

    }
}
