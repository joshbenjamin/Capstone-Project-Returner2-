import analysis.AssignResults;
import analysis.Student;
import conversion.PDF2Image;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import processing.PageScanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Returner2
{


    private static boolean isQuiz;

    private static ImagePlus ip;
    private static ImageProcessor imageProcessor;
    private static PageScanner ps;
    private static PDF2Image p2i;
    private static AssignResults assRes;

    public static void main(String[] args)
    {
        String fileName = System.getProperty("user.dir") + "/Returner2/" + args[0];          // Takes the arguments of fileName and textfile
        String testName = System.getProperty("user.dir") + "/Returner2/Papers/" + args[1];

        try
        {
            BufferedReader bufRead = new BufferedReader(new FileReader(testName)); // Reads the configuration fike
            PrintWriter writer = new PrintWriter("results.txt");                   // Used to write the results to a text file

            int numPages = Integer.parseInt(bufRead.readLine());
            isQuiz = bufRead.readLine().equalsIgnoreCase("Quiz");                  // Determines whether the paper is a quiz

            String lineFromFile = bufRead.readLine();
            lineFromFile = lineFromFile.substring(1, (lineFromFile.length()-1));   // Takes the data from the file and puts it into answers.
            String [] answersFromFile = lineFromFile.split(", ");                  // Also used to determine how many



            /*      !!!    Only for checking student answers against correct answers     !!!




            if (isQuiz)
            {
                //String[] answers = bufRead.readLine().substring(1, -1).split(", ");
                char[] correctAnswers = new char[tempResult.length];
                for (int i = 0; i < tempResult.length; i++)
                {
                    correctAnswers[i] = tempResult[i].charAt(0);
                }
            }
            //else if (type.equalsIgnoreCase("Test"))
            else
            {
                int[] maxMarks = new int[tempResult.length];
                for (int i = 0; i < tempResult.length; i++)
                {
                    maxMarks[i] = Integer.parseInt(tempResult[i]);
                }
            }


            */



            p2i = new PDF2Image(fileName, numPages);   // Calss the PDF2Image class to convert the inputted file

            try
            {
                ArrayList<String> pages = p2i.convert();  // Gets the converted file names

                for (int i = 0; i < pages.size(); i++)    // Iterates through the created files
                {
                    System.out.println(System.getProperty("user.dir") + "/" + pages.get(i));

                    ip = new ImagePlus(System.getProperty("user.dir") + "/" + pages.get(i));     // Calls the ImageProcessing classes
                    imageProcessor = ip.getProcessor();

                    ps = new PageScanner(isQuiz);
                    ps.setup("test", ip);
                    ps.run(imageProcessor);

                    int [][] PSStudentNumber = ps.getStudent();
                    int [][] PSMarks = ps.getPaper();




                    //PSStudentNumber = ps.getStudentNumber
                    //PSMarks = ps.getMarks


                    /*                      USED ONLY FOR TESTING


                    int[][] PSStudentNumber = {
                            {0,0,0,0,0,0,1000,1000,0},
                            {1000,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,1000},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,1000,1000,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,1000,0,0,0,0,0,0,0},
                            {0,0,0,0,1000,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,1000,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0},
                    };
                    int[][] PSMarks = {
                            {1000,0,1000,0,0,0,0,0,0,0,1000,1000,1000,1000,1000,1000},
                            {0,0,0,1000,1000,1000,0,0,0,0,0,0,0,0,0,0},
                            {0,1000,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,1000,1000,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,1000,1000,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                    };

                    */


                    assRes = new AssignResults(PSStudentNumber, PSMarks);

                    Student student = assRes.getStudent();

                    if(student.getName() != null){

                        if(isQuiz)   // Used to set the results for a student based on their marksheet
                        {
                            student.setResults(assRes.getQuizMarks(answersFromFile.length));
                            //writer.println(Arrays.toString(assRes.getQuizMarks(tempResult.length)));
                        }
                        else
                        {
                            student.setResults(assRes.getTestMarks(answersFromFile.length));
                        }
                    }

                    writer.println(student.toString());   // Writes the student details to the results textfile

                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            p2i.deleteFiles();    // Deletes the created files

            writer.println();
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}

