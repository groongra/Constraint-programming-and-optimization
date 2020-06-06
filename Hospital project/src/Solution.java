package assignment2;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.tools.ArrayUtils;

public class Residents119112856 {
	
	public static void main (String [] args) {
		
		String dataSourcePath="src\\assignment2\\residents0.txt";
		ResidentsReader residentsData = new ResidentsReader(dataSourcePath);
		
		int numResidents = residentsData.getNumResidents();
		int numShifts = residentsData.getNumShifts(); 
		int minimumNumberOfShifts = residentsData.getMinShifts();
		int[] minResidentsPerShift = residentsData.getMinResidents();
		int numQualifications = residentsData.getNumQuals();
		int[][] tutorialsMatrix = residentsData.getQualsOffered();
		int[][] tutorialsNeeded = residentsData.getQualsNeeded();
		int maximumNumberOfResidentsShifs = numResidents * numShifts;
		int maxNumSuccesiveShits = residentsData.getMaxBlock();
		int numRestsBetweenShifts = residentsData.getRestPeriod();
		int minNumUnassignedShifts = residentsData.getBreakPeriod();
		
		Model model = new Model("residents");
		
		IntVar[][] schedule = model.intVarMatrix(numResidents, numShifts, 0,1);
		IntVar[][] scheduleT = ArrayUtils.transpose(schedule);
		IntVar[] residentsPerShift = new IntVar[numShifts];
		IntVar TotalResidentsShifts = model.intVar("TotalResidentsShifs", 0, maximumNumberOfResidentsShifs);
		
		/*Constraints*/
		
		/*Ensure the minimum number of shifts per resident*/
		for (int resident = 0; resident < numResidents; resident++) {
			model.sum(schedule[resident], ">=", minimumNumberOfShifts).post();
		}
		
		/*Ensure the sum of residents in each shift must be greater or equal to the staff requirements*/
		for (int shift = 0; shift < numShifts; shift++) {
			model.sum(scheduleT[shift], ">=", minResidentsPerShift[shift]).post();
		}
		
		/*Ensure that the residentsPerShift array is composed of the sum of every resident per shift*/
		for (int shift = 0; shift < numShifts; shift++) {
			residentsPerShift[shift] = model.intVar("Shift"+shift, 0, numResidents); //how many possible residents
			model.sum(scheduleT[shift], "=", residentsPerShift[shift]).post();
		}	
		/*Ensure that the total number residents shifts is the sum of the residentsPorShift array*/
		model.sum(residentsPerShift, "=", TotalResidentsShifts).post();
		
		/* PseudoCode
		 *  Search for 1's in the column of the REQUIRED qualification Q(i) in tutorialsNeeded matrix 
		 *											
		 *	Q()		A B		If found means the resident should				Q(A)	0 0 1 0 0 0 0  *scalar
		 *	res0	0 1 	attend tutorial so add scalar constraint		res0	x x	x x x x x
		 *	res1	1 0  													 = 		r r r r r r r
		 *																	Constriant = Sum >= 1		
		 */
		
		/*Ensure that the each resident attends at least 1 of the required tutorials*/
		for(int resident=0; resident<numResidents; resident++) {
			for(int qualification=0; qualification<numQualifications; qualification++) {
				if(tutorialsNeeded[resident][qualification]==1) {
					model.scalar(schedule[resident], tutorialsMatrix[qualification], ">=", 1).post();
				}
			}
		}
		
		/* PseudoCode for constraint in case each resident requires all the tutorials 
		 * Search for 1's in the tutorials matrix 	Q(A)	0 0 1 0 0 0 0 
		 * 											Q(B)	0 0 0 0 1 0 0 
		 *	If found search for 1's in the column of the selected qualification Q(i) in tutorialsNeeded matrix 
		 *											Q()		A B	
		 *	If found add constraint to  			res0	0 1 
		 *	 the resident's shifts availability		res1	1 0 
		 *
		 *	for(int qualification=0; qualification<numQualifications; qualification++) {
				for (int shift = 0; shift < numShifts; shift++) {
					if(tutorialsMatrix[qualification][shift]==1) {
						for(int resident=0; resident<numResidents; resident++) {
							if(tutorialsNeeded[resident][qualification]==1) {
								model.arithm(schedule[resident][shift], "=", 1).post(); //Must attend tutorial
							}
						}
					}
				}
			}
		 */
	
		/*Ensure every resident gets at least p unassigned shifts in a row*/
		
		/*	Regex: (1|0)*(0{minNumUnassignedShifts})(1|0)*
		 *  Ensures that there is a break of "group of consecutive 0's" of length minNumUnassignedShifts
		 *  Example ensure at least a break of two days would mean (1|0)*(0{2})(1|0)* so 0 1 0 0 1 1 0 would be valid
		 */
		final String regex1 = "(1|0)*(0{"+minNumUnassignedShifts+"})(1|0)*"; 
		System.out.println("Regex1 =\t"+regex1);
		
		/*Ensure every resident gets a maximum of m assigned shifts in a row*/
		
		/*	The regex is created dynamically according to the maximum number of assigned shifts in a row
		 * 	Regex:	
		 *  	With m = 2 the regex is 0*(1{2}|1)(0+(1{2}?|1?))*
		 * 		With m = 3 the regex is 0*(1{3}|1{2}|1)(0+(1{3}?|1{2}?|1?))*
		 * 		with m = n the regex is 0*(1{n}|1{n-1}|...|1{1})(0+(1{n}?|1{n-1}?|...|1{1}?))*
		 * 
		 *  Ensures that there at most the number of sift blocks "group of consecutive 1's" have a length lesser than m
		 *  
		 *  Example with m = 2 the regex built is 0*(1{2}|1)(0+(1{2}?|1?))* which is equal to 0*(11|1)(0+(11?|1?))* 
		 *  	The first section of the regex allows the allocation of 0's and blocks of 1's bounded by length 2.
		 *  	The second section ensures that for following blocks there is a one or mor 0's in between groups of 1's 
		 *  	A valid string would be 0 1 0 0 1 1 0.
		 */
		final String regex2;
		String regexStart ="0*";
		String regexFirstSection = "(";
		String regexLastSection = "(0+("; 
		String regexEnd = ")*";
		for(int i = maxNumSuccesiveShits; i>0; i--) {
			if(i==1) {
				regexFirstSection += "1{"+i+"})";
				regexLastSection += "1{"+i+"}?)";
			} else {
				regexFirstSection += "1{"+i+"}|";
				regexLastSection += "1{"+i+"}?|";
			}
		}
		regex2 = regexStart + regexFirstSection + regexLastSection + regexEnd ;
		System.out.println("Regex2 =\t"+regex2);
		
		/*Ensure that each successive blocks of working shifts is separated by at least b unassigned shifts*/
		
		/*	The regex is created dynamically according to the minimum separation between shifts and maximum number of assigned shifts in a row 
		 * 	Regex: 
		 * with m = 3
		 *  	With b = 2 the regex is (0|01|10)*(((1{3}|1{2})0{2}(0*))*(1{3}|1{2})?)(0|01|10)*1?
		 * 		With b = 3 the regex is (0|01|10)*(((1{3}|1{2})0{3}(0*))*(1{3}|1{2})?)(0|01|10)*1?
		 * 		With m and b the regex is (0|01|10)*(((1{m}|1{m-1}..1{2})0{b}(0*))*(1{m}|1{m-1}..1{2})?)(0|01|10)*1?
		 * 
		 *  Ensures that there at most the number of sift blocks "group of consecutive 1's" have a length lesser than m
		 *  
		 *  Example with m = 2 and b = 4 the regex built is (0|01|10)*(((1{2}|1{1})0{6}(0*))*(1{2}|1{1})?)(0|01|10)*1?  which is equal to (0|01|10)*(((11|1)0000(0*))*(11|1)?)(0|01|10)*1? 
		 *  	The initial section of the regex allows for 0's or pairs of 0 and 1's 
		 *  	The first section of the regex allows blocks of 1's bounded by length 2 followed by at least b 0's.
		 *  	The second and third section ensures that the pattern is followed till the end
		 *  	A valid string would be 0 1 0 0 1 1 0.
		 */
		final String regex3;
				 
		String regexStart3 ="(0|01|10)*";
		String regexFirstSection3 = "(((";
		String regexLastSection3 = "("; 
		String regexEnd3 = "(0|01|10)*1?";
		for(int i = maxNumSuccesiveShits; i>1; i--) {
			if(i==2) {
				regexFirstSection3 += "1{"+i+"}";
				regexLastSection3 += "1{"+i+"}?";
			} else {
				regexFirstSection3 += "1{"+i+"}|";
				regexLastSection3 += "1{"+i+"}?|";
			}
		}
		regexFirstSection3 += ")0{"+numRestsBetweenShifts+"}(0*))*";
		regexLastSection3 += ")?)";
		regex3 = regexStart3 + regexFirstSection3 + regexLastSection3 + regexEnd3 ;
		System.out.println("Regex3 =\t"+regex3);
		System.out.println();
		
		/*Apply regex*/
		for(int resident=0; resident<numResidents; resident++) {
			model.regular(schedule[resident], new FiniteAutomaton(regex1)).post();
			model.regular(schedule[resident], new FiniteAutomaton(regex2)).post();
			model.regular(schedule[resident], new FiniteAutomaton(regex3)).post();
		}


		model.setObjective(model.MINIMIZE, TotalResidentsShifts);
		
		Solver solver = model.getSolver();
		
		/*Different search strategies tested*/
        
		//solver.setSearch(Search.domOverWDegSearch(residentsPerShift));
		//solver.setSearch(Search.inputOrderLBSearch(residentsPerShift));
		//solver.setSearch(Search.activityBasedSearch(residentsPerShift)); 		 
		//solver.setSearch(new ImpactBased(residentsPerShift, true));
			
	      int numsolutions = 0;
	      
	      while (solver.solve()) { //print the solution
	         numsolutions++;
	         System.out.println("Solution " + numsolutions + ":  --------------------------------------");
	         System.out.println("\tTotalShifts: "+TotalResidentsShifts.getValue());
	         for(int i=0; i<numResidents;i++) {
	        	 System.out.print("\t");
	        	 for(int j=0; j<numShifts;j++) {
	        		System.out.print(schedule[i][j].getValue()+" ");
	        	 }
	        	 System.out.println();
	         }
	      }
	      System.out.println();
	      solver.printStatistics();

	}
	
	/** REPORT:
	 * 
	 * Summary.
	 * I've modeled the problem as a boolean matrix in which each cell contains the information about the each resident's shift
	 * In this case the constraint satisfaction problem consist in filling the matrix in a way that satisfies the problem requirements.
	 * 
	 * Using the sum and scalar operations i can implement the first stage constraints (see comments above) These constraints define a series of required positions of 0's and 1's for the matrix.
	 * On the other hand the second stage constraints are more advanced because they represent a pattern problem. These requirements define a set of valid configurations for the binary sequences that represent each resident.
	 * To enforce the desired behavior i decided to use regular expressions (see above) so i have defined three different bit sequence patterns that the resident's schedule must follow.
	 * 
	 * Achievements.
	 * I've managed to obtain an abstract model that represents the constraint satisfaction problem both for stage one and stage two. 
	 * I've tested multiple search strategies and the algorithm always finds a valid solution if it exists. 
	 * 
	 * Problems.
	 * Optimization has been a challenge. Larger instances of the problem augment the search space exponentially. 
	 * To execute the model i tried using activity search because i thought it would improve the runtime but when compared to default search the execution failed and it might be because missaligned search objectives.  
	 * 
	 * I believe the model can be optimized by:
	 * 	- Breaking symmetries for instances of the problem with symmetric variable values.
	 *  - Increase the number of constraints to allow ArchConsistency and other solving algorithms to speed the inference
	 * 	
	 * Conclusions. 
	 * The assignment has reinforced my understanding in constraints modeling and helped understand how to "attack" a CSP "large" problem.
	 * The model i implemented manages to represent and solve the schedule issues the hospital has with residents, minimizing the total number of shifts needed. 
	 * Although it may lack the best optimiztion because of the reasons already presented i bealive its a valid and neat aproach, and what's more important, a good step twords a better understanding and solution for the problem. 
	 *
	 * Lucas Gonzalez 119112856.
	 */
	
	

}
