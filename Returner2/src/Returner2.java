import ij.ImagePlus;
import ij.process.ImageProcessor;
import processing.PageScanner;

import java.io.IOException;

public class Returner2
{
    public static void main (String[] args){

        /**TODO
         * Ask user if the test is a quiz
         * This can be done in the command line
         */

        // /csc2002s_2014_quiz_template.tiff
        // /rectangleTEST.tiff
        // /rotated15r.tiff
        // /upsidedown.tiff
        // /rectangleRotate.tiff
        final String IMAGEPATH = "/rotated15r.tiff";

        PageScanner ps = null;
        ImagePlus ip = null;
        ImageProcessor imageProcessor = null;

        try {

            ip = new ImagePlus(System.getProperty("user.dir") + IMAGEPATH);
            System.out.println("Testing: " + System.getProperty("user.dir") + IMAGEPATH);
            imageProcessor = ip.getProcessor();//new ColorProcessor(image);
            ps = new PageScanner(true);
        }

        catch(Exception e) {
            System.out.println("Error Occurred");
            e.printStackTrace();
        }

        ps.setup("test", ip);

        if (imageProcessor != null) {
            ps.run(imageProcessor);
            try {
                System.out.println("Wrote file");
                ps.writeFile(imageProcessor);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
