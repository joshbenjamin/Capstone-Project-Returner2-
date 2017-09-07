/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Returner2
{
    public static void main (String[] args){
        try {
            File file = new File("csc2002s_2014_quiz_template.jpg");
            BufferedImage image = ImageIO.read(file);

            //
            ImagePlus ip = new ImagePlus("quiz_template.jpg", image);

            ImageProcessor imageProcessor = new ColorProcessor(image);

            PageScanner ps = new PageScanner(null, null);
            ps.setup("test", ip);
            ps.run(imageProcessor);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
