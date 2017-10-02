import ij.ImagePlus;
import ij.process.ImageProcessor;

public class Returner2
{
    public static void main (String[] args){

        // /csc2002s_2014_quiz_template.tiff
        // /rectangleTEST.tiff
        // /rotated25r.tiff
        // /upsidedown.tiff
        final String IMAGEPATH = "/upsidedown.tiff";

        PageScanner ps = null;
        ImagePlus ip = null;
        ImageProcessor imageProcessor = null;

        try {
            // Gets an image from the working dir()
            long t = System.nanoTime();
            System.out.println(System.getProperty("user.dir") + IMAGEPATH);
            ip = new ImagePlus(System.getProperty("user.dir") + IMAGEPATH);

            System.out.println("Read Time: "+(System.nanoTime() - t)/1000000000.0);

            imageProcessor = ip.getProcessor();//new ColorProcessor(image);
            // add some noise
            //imageProcessor.noise(25.0);
            System.out.println("Read Time: "+(System.nanoTime() - t)/1000000000.0);
            /**TODO
             * Ask user if the test is a quiz
             */
            // new object which processes scanned image
            ps = new PageScanner(true);
        }

        catch(Exception e) {
            System.out.println("Error Occurred");
            e.printStackTrace();
        }

        ps.setup("test", ip);
        ps.run(imageProcessor);
    }
}
