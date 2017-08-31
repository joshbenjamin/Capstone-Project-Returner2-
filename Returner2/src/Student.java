
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Joshua
 */
public class Student
{
    private String studentNumber;
    private ArrayList<Character> studentAnswers;
    private Double mark;
    private Double percentage;
    
    public Student(String studentNumber, ArrayList<Character> studentAnswers){
        this.studentNumber = studentNumber;
        this.studentAnswers = studentAnswers;
    }
    
    public void setMarkAndPerc(int maxMarks){
        
    }
    
    public void setMarkAndPerc(ArrayList<Character> correctAnswers){
        
    }
    
    @Override
    public String toString(){
        return this.studentNumber + ": " + this.percentage;
    }
}
