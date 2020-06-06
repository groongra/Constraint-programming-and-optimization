package assignment1;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


public class PioneerData {
	
	private int n; //number of experiment types
	private int totalhours; //total number of hours available
	private int numBefores; //total number of 'before' relationships
	private int[] hours; //the hours required for each experiment type
	private int[] values; //the value of each experiment type
	private int[] totals; //the total number of experiments possible for each type
	private int[][] befores;
	private int maxHours; //the length of the longest experiment type
	private int maxValue; //the value of the most valuable experiment type
	private int maxTotal; //the number of the most available type
	
    public PioneerData(String filename) throws IOException {
    	/*
    	 * Assumes data is in file in the format specified in assignment description.
    	 */
	    Scanner scanner = new Scanner(new File(filename));
	    n = scanner.nextInt();
	    totalhours = scanner.nextInt();
	    numBefores = scanner.nextInt();
	    hours = new int[n];
	    values = new int[n];
	    totals = new int[n];
	    befores = new int[numBefores][2];
	    maxHours = 0;
	    maxValue = 0;
	    maxTotal = 0;
	    for (int i=0;i<n;i++){
	        hours[i] = scanner.nextInt();
	        values[i] = scanner.nextInt();
	        totals[i] = scanner.nextInt();
	        if (hours[i] > maxHours) maxHours = hours[i];
	        maxValue += values[i]*totals[i];
	        if (totals[i] > maxTotal) maxTotal = totals[i];
	        System.out.println(hours[i] + " " + values[i] + " " + totals[i]);
	    }
	    for (int i=0; i<numBefores; i++) {
	        befores[i][0] = scanner.nextInt();
	        befores[i][1] = scanner.nextInt();
	    }
	    scanner.close();
    }
    
    public int getNumTypes() {
    	return n;
    }
    
    public int getTotalHours() {
    	return totalhours;
    }
    
    public int getNumBefores() {
       return numBefores;
     }
     
    public int[] getHours() {
    	return hours;
    }
    
    public int[] getValues() {
    	return values;
    }
    
    public int[] getTotals() {
    	return totals;
    }
    
    public int[][] getBefores() {
       return befores;
    }
    public int getMaxHours() {
    	return maxHours;
    }
    
    public int getMaxValues() {
    	return maxValue;
    }

    public int getMaxTotals() {
    	return maxTotal;
    }
}
