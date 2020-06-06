package assignment2;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


public class ResidentsReader {

   private int numResidents; //number of residents
   private int numShifts; //number of shifts
   private int numQuals; //number of qualifications being offered
   private int[] minResidents; //minimum residents required for each shift
   private int[][] qualsOffered; //quals offered per shift (quals x shifts)
   private int[][] qualsNeeded; //quals required by residents (residents x quals)
   private int maxBlock; //the maximum shifts able to be worked on in a row
   private int restPeriod; //the number of shifts that must be off after a worked block
   private int breakPeriod; //number of off shifts that must be together at least once
   private int minShifts; //the minimum number of shifts to be worked by each resident
   
   public ResidentsReader(String filename) {
      
      Scanner scanner = null;
      try {
         scanner = new Scanner(new File(filename));
         numResidents = scanner.nextInt();
         numShifts = scanner.nextInt();
         numQuals = scanner.nextInt();
         maxBlock = scanner.nextInt();
         restPeriod = scanner.nextInt();
         breakPeriod = scanner.nextInt();
         minShifts = scanner.nextInt();
         
         minResidents  = new int[numShifts];
         qualsOffered  = new int[numQuals][numShifts];
         qualsNeeded  = new int[numResidents][numQuals];

         for (int shift = 0; shift < numShifts; shift++) {
            minResidents[shift] = scanner.nextInt();
         }  
         
         for (int qual=0;qual<numQuals;qual++){
            for (int shift=0;shift<numShifts;shift++) {
               qualsOffered[qual][shift] = scanner.nextInt();
            }
         }  

         for (int resident=0;resident<numResidents;resident++) {
            for (int qual=0;qual<numQuals;qual++){
               qualsNeeded[resident][qual] = scanner.nextInt();
            }
         }  
         
      }
      catch (IOException e) {
         System.out.println("File error:" + e);
      }
   }
       
   public int getNumResidents() { return numResidents; }
   public int getNumShifts() { return numShifts; }
   public int getNumQuals() { return numQuals; }
   public int[] getMinResidents() { return minResidents; }
   public int[][] getQualsOffered() { return qualsOffered; }   
   public int[][] getQualsNeeded() { return qualsNeeded; }  
   public int getMaxBlock() { return maxBlock; }   
   public int getRestPeriod() { return restPeriod; }
   public int getBreakPeriod() { return breakPeriod; }
   public int getMinShifts() { return minShifts; }
}
