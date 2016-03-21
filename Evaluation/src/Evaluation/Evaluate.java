package Evaluation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import BusinessObjects.Unit;
import Faults.ArtificalFault;
import Helper.MyFileWriter;
import Main.MainConfiguration;
import Prioritize.TestGroupProrization;
import Tests.TestGroup;

public class Evaluate {

	/* Determine the priozation drawbacks (how much longer it takes to cover all faults) using APFD
	 * Determine how much longer the test suite would have to execute for the random approach
	 */
	
	public <T> void determineAPFDForRandomApproach(RandomPriorization<T> priorization, List<ArtificalFault> faults)
	{
		MyFileWriter writer = new MyFileWriter(MainConfiguration.pathEvaluationSelectionRANDOMTestGroupPRIORIZATION);

		for(int i=0;i<MainConfiguration.amountOfRepeatingsOfEvaluationRANDOMTestGroupPRIORIZATION;i++)
		{
			long totalAvailableTestCount = priorization.notYetReturnedTestCasesCount();
			
			// amount of tests which were needed to identify all faults
			long testCountUsed = 0;
			long faultCount = faults.size(); // amount of faults which must be found
			
			List<ArtificalFault> faultsLeft = new ArrayList<>(faults);
			List<Long> faultDetectedTestPosition = new ArrayList<>();
		
			while(priorization.hasTestsLeft() && !faultsLeft.isEmpty())
			{
				testCountUsed++;
				
				T testGroup = priorization.nextTestGroupToExecute();
			
				//now check if the test group is able to cover at least one of the faults
				Iterator<ArtificalFault> faultIter = faultsLeft.iterator();
				
				while(faultIter.hasNext())
				{
					ArtificalFault nextFault = faultIter.next();
					
					if(testGroup instanceof TestGroup)
					{
						
						if(coversFaul((TestGroup)testGroup, nextFault))
						{
							faultIter.remove();
							faultDetectedTestPosition.add(testCountUsed);
						}	
					}
					else if(testGroup instanceof RandomTestGroup)
					{
						
						if(coversFaul((RandomTestGroup)testGroup, nextFault))
						{
							faultIter.remove();
							faultDetectedTestPosition.add(testCountUsed);
						}
					}
					else
					{
						System.out.println("Wrong data format for evaluation of random priorization");
					}
				}			
			}
			
			long missedFaultsCount = faultsLeft.size();		
			
			System.out.println("Total Fault Count: " + faults.size());
			System.out.println("Not detected Fault Count: "+missedFaultsCount);
			System.out.println("TestCount Prioritized: " + priorization.alreadyReturnedTestCasesCount());
			System.out.println("TestCount Left: " + priorization.notYetReturnedTestCasesCount());

			double apdf = calcualteAPDF(totalAvailableTestCount, faultCount, faultDetectedTestPosition);
			System.out.println(i+") APDF: " + apdf);
			
			String faultDetectionOrder = "Detection Order: ";
			for(long eachPosition : faultDetectedTestPosition)
			{
				faultDetectionOrder+=eachPosition + ", ";
			}

			System.out.println(faultDetectionOrder);
			writer.writeNewLine(Double.toString(apdf));
			
			priorization.resetPrioritization();
		}
	}
	
	public void determineAPFDForProposeApproach(TestGroupProrization priorization, List<ArtificalFault> faults)
	{
		long totalAvailableTestCount = priorization.notYetReturnedTestCasesCount();
		
		// amount of tests which were needed to identify all faults
		long testCountUsed = 0;
		long faultCount = faults.size(); // amount of faults which must be found
		
		List<ArtificalFault> faultsLeft = new ArrayList<>(faults);
		List<Long> faultDetectedTestPosition = new ArrayList<>();
		
		while(priorization.hasTestsLeft() && !faultsLeft.isEmpty())
		{
			testCountUsed++;
			
			TestGroup testGroup = priorization.nextTestGroupToExecute();
		
			//now check if the test group is able to cover at least one of the faults
			Iterator<ArtificalFault> faultIter = faultsLeft.iterator();
			
			while(faultIter.hasNext())
			{
				ArtificalFault nextFault = faultIter.next();
				
				if(coversFaul(testGroup, nextFault))
				{
					faultIter.remove();
					faultDetectedTestPosition.add(testCountUsed);
				}
			}			
		}
		
		long missedFaultsCount = faultsLeft.size();		
		
		System.out.println("Total Fault Count: " + faults.size());
		System.out.println("Not detected Fault Count: "+missedFaultsCount);
		System.out.println("TestCount Prioritized: " + priorization.alreadyReturnedTestCasesCount());
		System.out.println("TestCount Left: " + priorization.notYetReturnedTestCasesCount());
		System.out.println("TestCount Total: " + (priorization.notYetReturnedTestCasesCount()+priorization.alreadyReturnedTestCasesCount()));
		System.out.println("Execution Time Selected Tests: " + determineExecutionTime(priorization.getAlreadyExecutedTestGroups()));
		System.out.println("Execution Time All Tests: " + (determineExecutionTime(priorization.getAlreadyExecutedTestGroups())+
				determineExecutionTime(priorization.getAvailableTestGroups())));

		System.out.println("APDF: " + calcualteAPDF(totalAvailableTestCount, faultCount, faultDetectedTestPosition));
		
		String faultDetectionOrder = "Detection Order: ";
		for(long eachPosition : faultDetectedTestPosition)
		{
			faultDetectionOrder+=eachPosition + ", ";
		}

		System.out.println(faultDetectionOrder);
	}
	
	//milliseconds!
	private long determineExecutionTime(List<TestGroup> testGroups)
	{
		long result = 0;
		
		for(TestGroup eachTestGroup:testGroups)
		{
			result+=eachTestGroup.getConcurrentestGroupExecutionTime();
		}
		
		return result;
	}
	
	private boolean coversFaul(TestGroup testGroup, ArtificalFault fault)
	{
		boolean result = false;
		
		List<Unit> coveredUnits = testGroup.getSpecificallyTestedConcurrencyUnits();
		
		// both units must be executed at the same time to result in an concurrency issue
		result = coveredUnits.contains(fault.getFaultUnitOne()) &&
				coveredUnits.contains(fault.getFaultUnitTwo());
		
		return result;
	}
	
	private boolean coversFaul(RandomTestGroup testGroup, ArtificalFault fault)
	{
		boolean result = false;
		
		List<Unit> coveredUnits = testGroup.getConcurrentlyTestedUnits();
		
		// both units must be executed at the same time to result in an concurrency issue
		result = coveredUnits.contains(fault.getFaultUnitOne()) &&
				coveredUnits.contains(fault.getFaultUnitTwo());
		
		return result;
	}
	

	private double calcualteAPDF(long testCount, long faultCount, List<Long> positions)
	{		
		long positionsSum = 0l;
		
		for(long eachPosition : positions)
		{
			positionsSum+=eachPosition;
		}
			
		return 1.0 - (((double)positionsSum)/(testCount*faultCount)) + (1.0/(2.0*testCount));			
	}
}
