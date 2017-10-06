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
    private ImagePlus imp;

    private ImageProcessor ip;
    private boolean isQuiz = false;
    private boolean flipped = false;

    private double ScaleFactor = 1;

    private static final int VERTICAL_GAP = 9;
    private static final int HORIZONTAL_GAP = 28;
    private static final int BORDER_THICKNESS = 1;
    private static final int BOX_WIDTH = 28;
    private static final int BOX_HEIGHT = 28;

    private static final int BORDER_THRESHOLD = 300;
    private static final int SCALED_DOWN_REGION_CHECK = 5;
    private static final int SCALED_DOWN_REGION_THRESHOLD = 20;
    private static final int REGION_CHECK = 10;
    private static final int REGION_THRESHOLD = 50;

    private static final int BLACK_PIXEL = -7000000;
    private static final int PENCIL_PIXEL = -170000;

    private static final double RECTANGLE_WIDTH = 77.0;
    private static final double RECTANGLE_HEIGHT = 221.0;

    // Object Constructor Method
    // Let the test know whether there are quiz answers or not

    public PageScanner(Boolean isQuiz){
        this.isQuiz = isQuiz;
    }

    public int setup(String arg, ImagePlus imp)
    {
        this.imp = imp;
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
        this.ip = ip;

        int[] pixels = (int[]) this.ip.getPixels();
        int[] origin;

        alignPage(this.ip);

        //try {
        //    writeFile(ip);
        //}
        //catch (IOException e){
        //    e.printStackTrace();
        //}

        //ensures that we can revert back ti the original, non-scaled down version
        ip.snapshot();
        // find origin region

        origin = findOrigin();
        try {
            writeFile(this.ip, "outScaledDown");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        if (origin == null) {
            System.out.println("No origin found");
            System.exit(0);
        }

        // now we need a non-scaled version.
        ip.reset();

        try {
            writeFile(ip, "outOG");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        // We should now know the region in which the origin exists.

        int pixelPosition = establishOrigin(origin[0], origin[1]);

        scaleCorrection(pixelPosition);

        // Ensure that the file has correct dimensions for file checks

        this.ip.snapshot();

        origin = findOrigin();
        try {
            writeFile(this.ip, "test20");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        if (origin == null) {
            System.out.println("No origin found");
            System.exit(0);
        }

        // now we need a non-scaled version.
        this.ip.reset();

        pixelPosition = establishOrigin(origin[0], origin[1]);
        try {
            writeFile(this.ip, "Pre Process");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        if (pixelPosition == -1){
            System.out.println("Could not find origin");
            System.exit(0);
        }
        pixels[pixelPosition] = -18000;
        // write file method?

        try {
            writeFile(this.ip, "FINAL ORIGIN FOUND");
        }
        catch (IOException e){
            e.printStackTrace();
        }


        int[][] test = getStudentNumber(pixelPosition);
        System.out.println("B-pixels" + test[0][1]);

        if (isQuiz){
            // mark the quiz
        }
        else{

        }

    }
    //------------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param ip TODO
     *  INSERT GUCCI COMMENT HERE
     * @return int[]
     *  This int[] array is the new array of pixels, which is used to rectify the image
     */
    private void alignPage(ImageProcessor ip) {
        System.out.println("ALIGN PAGE");
        int distanceTop = -1;
        int distanceBot = -1;
        int distanceMid = -1;
        int topStart = (int) Math.round(this.ip.getHeight() * 0.10);
        int midStart = ip.getHeight()/2;
        int botStart = (int) Math.round(this.ip.getHeight() * 0.9);

        int[] pixels = (int[]) this.ip.getPixels();

        boolean topBorder = true;
        boolean midBorder = true;
        boolean botBorder = true;

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
            int pixelPosition = botStart * this.ip.getWidth();

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
        if(botBorder && midBorder){
            int rows = botStart - midStart;
            System.out.println("top border too close to the edge");
            System.out.println("so we take mid and bot");

            int diff = distanceMid - distanceBot;
            double radAngle = Math.atan(diff * 1.0/rows);
            double Angle =  Math.toDegrees(radAngle);
            System.out.println("Rotate angle: " + Angle);
            ip.rotate(-Angle);
        }
        else if (botBorder && topBorder){

            int rows = botStart - topStart;

            int diff = distanceTop - distanceBot;

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
     * From the given ImageProcessor, it scales the image down to 20%, and finds the block, returning the
     * co-ordinate of the blocks corner. It also checks the orientation of the page which it corrects, if the page is
     * upside-down
     *
     * The distance to the border must be a minimum of the edge width, which is 78
     *
     * @return int[x,y]
     * Here x and y are the relative co-ordinates on the original ImageProcessor.
     */
    private int[] findOrigin(){
        System.out.println("findOrigin");

        int[] Origin = new int[2];
        int[] pixels = (int[])this.ip.getPixels();

        int row;

        int width = this.ip.getWidth();
        int height = this.ip.getHeight();
        int newWidth = (int)Math.round(width * 0.2);
        int newHeight = (int)Math.round(height * 0.2);
        int halfWidth = Math.abs(width - newWidth) / 2;
        int halfHeight = Math.abs(height - newHeight)/2;

        boolean rotated = false;
        this.ip.scale(0.2,0.2);

        System.out.println(width + ", " + height);
        System.out.println(newWidth + ", " + newHeight);

        for (row = 0; row < newHeight; row++) {
            //System.out.println(row + ", " + newHeight/2);
            if( row > newHeight/2){

                if(rotated) {
                    System.out.println("There is no black block on this page FLAG");
                    this.ip.setPixels(pixels);

                    return null;
                }

                // this implies that the block, if any block at all, is at the bottom of the page
                // an incorrect orientation, so we flip the image and start again.
                ip.rotate(180);
                rotated = true;
                flipped = true;

                row = 0;
            }
            //System.out.println("halfWidth: "+halfWidth);
            for (int count = halfWidth; count != newWidth + halfWidth; count++) {

                int position =  count + width * (halfHeight + row);
                //System.out.println(pixels[position]);
                if (pixels[position] < BLACK_PIXEL) {
                    //System.out.println("Find a black pixel");
                    // found a pixel which matches the description
                    // now check if the next row is also the same
                    // i.e. we are determining if this is the black square
                    boolean isBlock = false;

                    blockcheck:
                    {
                        int pixelCount = 0;
                        for (int line = 0; line < SCALED_DOWN_REGION_CHECK; line++) {
                            for (int column = position; column < position + SCALED_DOWN_REGION_CHECK; column++) {

                                if (pixels[width * line + column] <= BLACK_PIXEL) {
                                    pixelCount ++;
                                }
                            }
                        }
                        if (pixelCount >= SCALED_DOWN_REGION_THRESHOLD){
                            isBlock = true;
                        }
                    }
                    if (isBlock) {

                        pixels[position] = -18000;// - highlights pixel whose co-ordinate is returned

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
    public int[][] getStudentNumber(int originPixel){
        int[][] namePixels = new int[10][26];
        System.out.println("------------Mark Student Numbers------------------");
        System.out.println("XScale: " + ScaleFactor);
        System.out.println("YScale: " + ScaleFactor);
        int[] pixels = (int[]) this.ip.getPixels();
        System.out.println(this.ip.getWidth() + ", " + this.ip.getHeight());
        int currentPixelPoint = originPixel + (int)Math.round(771*ScaleFactor) +
                (int)Math.round(264* ScaleFactor * this.ip.getWidth()) +//changed from 262
                (int)Math.round(BORDER_THICKNESS*ScaleFactor);

        System.out.println("Jump down gap: " + (BORDER_THICKNESS * 3 + BOX_HEIGHT + VERTICAL_GAP));
        System.out.println("Jump across gap: " + (BORDER_THICKNESS * 2 + BOX_WIDTH + HORIZONTAL_GAP));

        System.out.println(originPixel/ip.getWidth()+", "+ originPixel%ip.getWidth());
        System.out.println((int)Math.round(771*ScaleFactor));
        // outer loop goes through box columns
        int collCount = 0;
        for (int column = 0; column != 10; column ++){
            collCount ++;
            if (collCount % 2 == 0){
                currentPixelPoint += 3;
            }
            // inner loop goes through box rows
            int count = 0;
            for (int row = 0; row != 26; row ++){
                namePixels[column][row] = 0;
                if (column > 6 && row >= 10){
                    continue;
                }
                if (count % 2 == 0){
                    currentPixelPoint += 2*this.ip.getWidth();
                }
                // shifts it down by a box and its space
                if (row!=0) {
                    currentPixelPoint += this.ip.getWidth() *
                            (BORDER_THICKNESS * 2 + BOX_HEIGHT + VERTICAL_GAP);

                }

                // iterates through actual pixel block
                //System.out.println(column + ", " + row);
                //System.out.println(currentPixelPoint);
                for(int pixelRow = 2; pixelRow != (int) Math.round(BOX_HEIGHT*ScaleFactor)-2; pixelRow ++){
                    for(int pixelColumn = 2; pixelColumn != (int) Math.round(BOX_WIDTH*ScaleFactor)-2; pixelColumn ++){



                        if(row == 1 && column == 0) {
                            System.out.println(pixels[currentPixelPoint]);
                        }
                        if (pixels[currentPixelPoint + pixelColumn + pixelRow*this.ip.getWidth()] < BLACK_PIXEL) {

                            namePixels[column][row] += 1;
                            pixels[currentPixelPoint + pixelColumn + pixelRow*this.ip.getWidth()] = -180000;
                        }
                    }
                }
                System.out.println(namePixels[column][row]);
                count ++;
            }

            currentPixelPoint = originPixel + (int)Math.round(771*ScaleFactor) +
                    (int)Math.round(264* ScaleFactor * this.ip.getWidth()) +
                    (int)Math.round(BORDER_THICKNESS * ScaleFactor) +
                    column * (2 * BORDER_THICKNESS + BOX_WIDTH * 2);
        }
        this.ip.setPixels(pixels);
        try{
            writeFile(this.ip, "PLZWORK");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return namePixels;
    }
    //------------------------------------------------------------------------------------------------------------------
    public int[][] getQuizAnswers(int originPixel){
        //start of A1 block is 581, 1770
        int[][] namePixels = new int[16][10];
        System.out.println("------------Mark Quiz Numbers------------------");

        int[] pixels = (int[]) this.ip.getPixels();
        System.out.println(this.ip.getWidth() + ", " + this.ip.getHeight());
        int currentPixelPoint = originPixel + (int)Math.round(771*ScaleFactor) +
                (int)Math.round(264* ScaleFactor * this.ip.getWidth()) +//changed from 262
                (int)Math.round(BORDER_THICKNESS*ScaleFactor);

        System.out.println("Jump down gap: " + (BORDER_THICKNESS * 3 + BOX_HEIGHT + VERTICAL_GAP));
        System.out.println("Jump across gap: " + (BORDER_THICKNESS * 2 + BOX_WIDTH + HORIZONTAL_GAP));

        System.out.println(originPixel/ip.getWidth()+", "+ originPixel%ip.getWidth());
        System.out.println((int)Math.round(771*ScaleFactor));
        // outer loop goes through box columns
        int collCount = 0;
        for (int column = 0; column != 10; column ++){
            collCount ++;
            if (collCount % 2 == 0){
                currentPixelPoint += 3;
            }
            // inner loop goes through box rows
            int count = 0;
            for (int row = 0; row != 26; row ++){
                namePixels[column][row] = 0;
                if (column > 6 && row >= 10){
                    continue;
                }
                if (count % 2 == 0){
                    currentPixelPoint += 2*this.ip.getWidth();
                }
                // shifts it down by a box and its space
                if (row!=0) {
                    currentPixelPoint += this.ip.getWidth() *
                            (BORDER_THICKNESS * 2 + BOX_HEIGHT + VERTICAL_GAP);

                }

                // iterates through actual pixel block
                //System.out.println(column + ", " + row);
                //System.out.println(currentPixelPoint);
                for(int pixelRow = 2; pixelRow != (int) Math.round(BOX_HEIGHT*ScaleFactor)-2; pixelRow ++){
                    for(int pixelColumn = 2; pixelColumn != (int) Math.round(BOX_WIDTH*ScaleFactor)-2; pixelColumn ++){



                        if(row == 1 && column == 0) {
                            System.out.println(pixels[currentPixelPoint]);
                        }
                        if (pixels[currentPixelPoint + pixelColumn + pixelRow*this.ip.getWidth()] < BLACK_PIXEL) {

                            namePixels[column][row] += 1;
                            pixels[currentPixelPoint + pixelColumn + pixelRow*this.ip.getWidth()] = -180000;
                        }
                    }
                }
                System.out.println(namePixels[column][row]);
                count ++;
            }

            currentPixelPoint = originPixel + (int)Math.round(771*ScaleFactor) +
                    (int)Math.round(264* ScaleFactor * this.ip.getWidth()) +
                    (int)Math.round(BORDER_THICKNESS * ScaleFactor) +
                    column * (2 * BORDER_THICKNESS + BOX_WIDTH * 2);
        }
        this.ip.setPixels(pixels);
        try{
            writeFile(this.ip, "PLZWORK");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return namePixels;
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
    private int establishOrigin(int x, int y){
        System.out.println("Establish Origin");
        int[] pixels = (int[])this.ip.getPixels();

        for (int row = y -10; row != y + 10; row ++) {
            if (row <0) {
                row = 0;
            }
            for (int column = x - 10; column != x + 10; column ++){
                if (column <0) {
                    column = 0;
                }
                int position = row * this.ip.getWidth() + column;

                if (pixels[position] < BLACK_PIXEL) {
                    boolean isBlock = true;

                    blockcheck:
                    {
                        int pixelCount = 0;
                        for (int line = 0; line < REGION_CHECK; line++) {
                            for (int col = 0; col < REGION_CHECK; col++) {

                                if (pixels[position + col + line*this.ip.getWidth()] <= BLACK_PIXEL) {
                                    pixelCount ++;
                                }
                            }
                        }
                        System.out.println(pixelCount);
                        if (pixelCount >= REGION_THRESHOLD){
                            isBlock = true;
                        }
                    }

                    if (isBlock) {
                        pixels[position] = -180000;
                        System.out.println("Established origin: "+column+", "+row);
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
     *
     * We know that the width of the square should be 77 pixels wide. We also know that the height of the square should
     * be 211. This method now checks what the length of the perceived box is and scales the page accordingly in the
     * correct axis.
     */
    private void scaleCorrection(int pixelPosition){
        System.out.println("Scale Correction");
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
        System.out.println("WIDTH: "+ width);
        System.out.println("HEIGHT: "+ height);
        ScaleFactor = (1 - (width*1.0/height*1.0) / RECTANGLE_WIDTH/RECTANGLE_HEIGHT) ;

        System.out.println(ScaleFactor);
        System.out.println((int)Math.round(ip.getWidth()*(ScaleFactor)));
        System.out.println((int)Math.round(ip.getWidth()*(ScaleFactor)));

        this.ip =  this.ip.resize((int)Math.round(this.ip.getWidth()*(ScaleFactor)),
                (int)Math.round(this.ip.getHeight()*(ScaleFactor)));

        try{
            writeFile(this.ip, "postScale");
        }
        catch (IOException e){
            e.printStackTrace();
        }
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
    public void writeFile(ImageProcessor ip, String name) throws IOException{
        System.out.println("Wrote file " + name);
        BufferedImage image = ip.getBufferedImage();
        ImageIO.write(image, "png", new File(name +".png"));

    }
    public ImageProcessor getIp() {
        return ip;
    }
}
