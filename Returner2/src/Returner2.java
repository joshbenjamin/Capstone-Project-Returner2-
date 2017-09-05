
import java.awt.Desktop;
import java.io.File;




/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Cameron Beetar
 */
public class Returner2
{
    public static void main (String[] args)
    {
        try {
            File pdf = new File("/home/cameron/csc_2016_returner_cover.pdf");
            if (pdf.exists()) 
            {
                //Opens the pdf on the pc but serves no real purpose at this stage.
                if (Desktop.isDesktopSupported()) 
                {
                    //we can open the pdf
                    Desktop.getDesktop().open(pdf);
		} 
                else
                {
        		System.out.println("Desktop Error");
		}

            }
            else
            {
		System.out.println("File not found!");
            }
            System.out.println("Done");

	}catch (Exception e) 
        {
            e.printStackTrace();
	}
    }
}
