package assignment1;
import java.io.IOException;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import assignment1.PioneerData;

public class Pioner119112856 {
	
	public static void main(String[] args) throws IOException  {
		
		String dataSourcePath="src\\assignment1\\pioneer0.txt";
		
		Model model = new Model("pioneer");
		
		PioneerData pioneerData = new PioneerData(dataSourcePath);
		System.out.println("Number of experiment types = "+pioneerData.getNumTypes());
		System.out.println("Number of available hours = "+pioneerData.getTotalHours());
		System.out.println();
			
		//Create variables
		
		/* pioneerLoad is an array that represents the experiment load. 
		 * Each cell indicates the number of experiments of that type that will be carried 
		 * The domain of each cell ranges from 0 to mi = maximum number of experiments of the type[{0...m0}][{0...m1}][...][0...m n-1] 
		 */
		IntVar[] pioneerLoad = new IntVar[pioneerData.getNumTypes()]; 
		for (int exptType = 0; exptType < pioneerData.getNumTypes(); exptType++) {
			pioneerLoad[exptType] = model.intVar("pioneerLoad", 0, pioneerData.getTotals()[exptType]); 
		}
		//total time spent in the selected experiments
		IntVar totalTimeSpent = model.intVar("totalTimeSpent", 0, pioneerData.getTotalHours());
		IntVar totalValueGained = model.intVar("totalValueGained", 0, pioneerData.getMaxValues());
		
		//Constraints
		/*The knapsack problem or rucksack problem is a problem in combinatorial optimization: 
		 * Given a set of items, each with a weight and a value, determine the number of each item to include in a collection so 
		 * that the total weight is less than or equal to a given limit and the total value is as large as possible.*/
		
		//model.knapsack(occurrences, weightSum, energySum, weight, energy)
		model.knapsack(pioneerLoad, totalTimeSpent, totalValueGained, pioneerData.getHours(), pioneerData.getValues()).post();
		
		IntVar pair1,pair2;
		for(int i=0; i<pioneerData.getNumBefores();i++) {
			pair1 = pioneerLoad[pioneerData.getBefores()[i][0]];
			pair2 = pioneerLoad[pioneerData.getBefores()[i][1]];
			model.arithm(pair1, ">=", pair2).post();
		}
		
		// Solve the problem
		
		Solver solver = model.getSolver();
				
		//varying search strategy to speed things up
		        
		//solver.setSearch(Search.domOverWDegSearch(load));
		//solver.setSearch(Search.inputOrderLBSearch(load));
		solver.setSearch(Search.activityBasedSearch(pioneerLoad)); 		 
		//solver.setSearch(new ImpactBased(load, true));
		//solver.setSearch(new ImpactBased(load, 2,3,10, 0, false));
				
		// State totalValueGained variable to be maximised
		model.setObjective(Model.MAXIMIZE, totalValueGained);
		
		int numsolutions = 0;
//		if (solver.solve()) {
			while (solver.solve()) { //print the solution
				numsolutions++;
		        System.out.println("Solution " + numsolutions + ":  --------------------------------------");

		        //next code block interrogates the variables and gets the current solution
				System.out.print("types:   ");
				for (int type = 0; type<pioneerData.getNumTypes(); type++) {
					System.out.print("\t" + type);
				}
				System.out.println();

				System.out.print("PioneerLoad:   ");
				for (int type = 0; type<pioneerData.getNumTypes(); type++) {
					System.out.print("\t[" + pioneerLoad[type].getValue()+"]");
				}
				System.out.println();
				
				System.out.print("ExptValue:   ");
				for (int type = 0; type<pioneerData.getNumTypes(); type++) {
					System.out.print("\t[" + pioneerData.getValues()[type]+"]");
				}
				System.out.println();					
				System.out.println("totalTimeSpent: " + totalTimeSpent.getValue());
				System.out.println("totalValueGained: " + totalValueGained.getValue());
		    }
		//Note - last solution generated is the optimal one
		
		System.out.println();	
		solver.printStatistics();
			

	}

}
