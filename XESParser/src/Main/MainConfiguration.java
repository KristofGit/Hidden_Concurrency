package Main;

import Helper.LogType;

public class MainConfiguration {

	
	// type of the log that should be analyzed
	public static final LogType logype = LogType.BPIC;
	
	// path were a BPIC or TeleClaim XES log can be found
	public static final String folderPathXESData = "/home/XesData/";

	// percent, uses to shrink and expand the dates to check for almost overlapping
	public static final double almostOverlappingIndicator = 0.1;
	
	// if it should be ignored if an unit overlapps with executions of itself (if the same unit is executed concurrently at the same time)
	public static final boolean ignoreOwnOverlappings = true;
	
	// reduce the amount of produces overlapping connections by filtering out the no overlapping data early
	public static final boolean earlyFilterOutNoOverlappingsState = true;

	// used to calculate the concurrency risk that two units are executed concurrently
	public static final double valueCompleteOverlap = 1;
	public static final double valuePartlyOverlap = 0.8;
	public static final double valueNearlyOverlap = 0.1; //if a overlap could likely have ocured but was not observed in reality
	
	// how many units can be added, at most, to each group unit
	// uncomment the appropriate section for TeleClaim/BPIC data
	// FOR TELECLAIM
	//public static final long maximumGroupDistribution = 2;
	//public static final long maximumGroupLevels = 2; //maximum amount of stacked Units

	// FOR BPIC
	public static final long maximumGroupDistribution = 5;
	public static final long maximumGroupLevels = 3; //maximum amount of stacked Units
	
	//how much minimal risk a unit must "observe" to be executed concurrently with the group extension present 
	//so that its considered to be added to a group
	//should prevent unnecessary big groups because groups are not extended if its very unlikely that a concurrent
	//execution will occure
	public static final double minRiskPerGroupExtension=0.01;
	//Note: With group distri of 2 and group levels of 3 will we get a maximum of 15 Units per group
	
	
	//EVALUATION
	//in percent of all units, the units with the highest risk of an concurrent execution will
	//be taken
	public static final double artificalFaultyUnits = 0.05;
	public static final int maxArtificalFaultCount = 11;
	public static final int minArtificalFaultCount = 3;
	public static final double amountMultiplierForRandomTestGroups = 3.0; //how much random test groups should be generated for each test case
	
	// were to save the output of the LOAD TEST test group generation (amount of test groups and their execution time)
	public static final String pathEvaluationSelectionRANDOMTestGroupGeneration = "/home/randomSelection";
	public static final int amountOfRepeatingsOfEvaluationRANDOMTestGroupGeneration = 100;
	
	// were to save the output 
	public static final String pathEvaluationSelectionRANDOMTestGroupPRIORIZATION = "/home/randomPriorization";
	public static final int amountOfRepeatingsOfEvaluationRANDOMTestGroupPRIORIZATION = 100;
}

