package conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class PDF2Image
{
    public static void main(String[] args) throws IOException, InterruptedException{
        
        final int numPages = 7;  // Sets number of pages per test
        final int dpi = 200;   // Sets the DPI (the higher, the longer it takes)
        
        long start = System.nanoTime();
        
        BufferedReader pReader;
        int curPageNum = 0;   // Keeps track of where we are

        // We need make this more general so it doesn't fall flat :)

        while(true){
            ProcessBuilder builder = new ProcessBuilder( // Uses a CMD type command to run the conversion using ImageMagick
                "cmd.exe", 
                "/c", 
                "cd \"C:\\Users\\Joshua\\Desktop\\TempMaven\\Files\\\" && magick -density " + dpi + " testBatch.pdf[" + curPageNum + "] -quality 100 out.png"
            );
            builder.redirectErrorStream(true);
            Process p = builder.start(); // Starts the process of converting
            pReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            if(pReader.readLine() != null){
                break;                           // Just to check if there's an error/no more pages to be scanned
            }
            p.waitFor();                        // Program only continues after the process is complete
            curPageNum += numPages;
            
            
            
            
            // We then use the image named "out.png" in the program from this point
            
            
            
            
        }
        long endTime = System.nanoTime();
        
        System.out.println((endTime-start)/1000000000.0 + " seconds");  // Just prints out the time it took for the whole thing
        
        
        File file = new File("C:\\Users\\Joshua\\Desktop\\TempMaven\\Files\\out.png");
        file.delete();   // Deletes the file to clear up storage space on server side
        
    }
}
