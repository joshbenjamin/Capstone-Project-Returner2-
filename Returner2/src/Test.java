
import java.util.ArrayList;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Joshua
 */
public class Test
{
    private Date date;
    private String course;
    private String type;
    private String convenor;
    private ArrayList<Integer> questionMaxMarks;
    private ArrayList<Character> correctQuizAnswers;

    public Test(Date date, String course, String type, String convenor, ArrayList<Integer> questionMaxMarks, ArrayList<Character> correctQuizAnswers)
    {
        this.date = date;
        this.course = course;
        this.type = type;
        this.convenor = convenor;
        this.questionMaxMarks = questionMaxMarks;
        this.correctQuizAnswers = correctQuizAnswers;
    }
    
    public String getType(){
        return this.type;
    }
    
}
