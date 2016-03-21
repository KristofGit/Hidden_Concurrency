package Parser;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.logging.XLogging;
import org.deckfour.xes.logging.XLogging.Importance;
import org.deckfour.xes.logging.XLoggingListener;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

import Helper.LogType;
import Helper.Tuple;
import Main.MainConfiguration;

public class DataExtraction {

	
	public static Tuple<List<Activity>,List<BProcess>> analyzeXES() {
		XLogging.setListener(new XLoggingListener() {
			
			@Override
			public void log(String arg0, Importance arg1) {
				System.out.println(arg1 + ":"+arg0);
			}
		});
		
		List<File> files = new ArrayList<File>();
		listf(MainConfiguration.folderPathXESData,files);	
	
		Tuple<List<Activity>,List<BProcess>> result = null;
		
		for(File xesFile : files)
		{
			if(!xesFile.getName().toLowerCase().contains("xes"))
			{
				continue;
			}
			
			XesXmlParser xesParser = new XesXmlParser();

			if(!xesParser.canParse(xesFile))
			{
				xesParser = new XesXmlGZIPParser();
				if (!xesParser.canParse(xesFile)) {
					throw new IllegalArgumentException("Unparsable log file: " + xesFile.getAbsolutePath());
				}
			}		
			
			try {
				
				if(MainConfiguration.logype == LogType.TeleClaim)
				{
					result = parseLogTELECLAIMS(xesParser, xesFile);

				}
				else if(MainConfiguration.logype == LogType.BPIC)
				{
					result = parseLogBPIC(xesParser, xesFile);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			//currently we only support one file (executions of the same activity are not merged if stored in multiple files!)
			//so top after the first
			break;
		}
		
		return result;
	}
	
	private static Tuple<List<Activity>,List<BProcess>> parseLogTELECLAIMS(XesXmlParser parser, File xesFile) throws Exception {
	/* Teleclaim allways has a start and then and complete event while the BPIC data only has complete events but stored the start date in the event data
	 */
		
		List<XLog> xLogs = parser.parse(xesFile);
		
		// key = concept:name
		HashMap<String, Activity> knownActivities = new HashMap<>();
		List<BProcess> extractedProcesses = new ArrayList<>();
		
        for (XLog xLog : xLogs) {
        	List<XEventClassifier> eventClassifiers = xLog.getClassifiers();
        	
        	for (XTrace trace : xLog) {
        		
        		XAttributeMap attributeMapTrace = trace.getAttributes();
				XAttribute tranceName = attributeMapTrace.get("concept:name");

				//Assumption 1 trace = 1 process instance
				//later duplicates will be filtered and one left process will become at least one test 
				
				BProcess currentProcess = null;
				
				/*
				 * There should be an even amount of traces, one start and then followed by an end event
				 * Last two events of a trace allways are dummy ones stating the call curation (concept name: end)
				 */
				
				Date lastStartDate = null;

				for (XEvent event : trace) {
					
					XAttributeMap attributeMap = event.getAttributes();
					
					// same activities are used in multiple resources and likely to different things (e.g. start a different application)
					// so separate them by the resource (e.g. sidnay or brisbane call center)
					XAttributeLiteral processModelIdentifier = (XAttributeLiteral)attributeMap.get("org:resource");    	
					XAttributeLiteral activityIdentifier = (XAttributeLiteral)attributeMap.get("concept:name");    	

					XAttributeLiteral activityCode = (XAttributeLiteral)attributeMap.get("concept:name");    	
					XAttributeLiteral lifecycle = (XAttributeLiteral)attributeMap.get("lifecycle:transition");    	

					String acticityName = processModelIdentifier.getValue() + " - " + activityIdentifier;
					
					if(!activityCode.getValue().equals("end"))
					{
						//start event
						if(lifecycle.getValue().equals("start"))
						{
							XAttributeTimestamp startTimestamp = (XAttributeTimestamp)attributeMap.get("time:timestamp");
							lastStartDate = startTimestamp.getValue();
						}
						else if(lifecycle.getValue().equals("complete"))
						{
							XAttributeTimestamp endTimestamp = (XAttributeTimestamp)attributeMap.get("time:timestamp");

							if(lastStartDate == null)
							{
								System.out.println("Got end value without start:"+acticityName);
							}
							else
							{
								
							
							Date lastEndDate = endTimestamp.getValue();
														 
							Activity activity = knownActivities.get(acticityName);
							
							if(activity == null)
							{
								activity = Activity.of(acticityName, processModelIdentifier.getValue());
								knownActivities.put(acticityName, activity);
							}
							
							if(currentProcess == null)
							{
								currentProcess = BProcess.of(activity);
							}
							currentProcess.addActivity(activity);
						
							Execution newExecution = Execution.of(lastStartDate, lastEndDate);
							activity.addExection(newExecution);
							
							}
							
							lastStartDate = null;
						}
						else
						{
							System.out.println("Unexpected lifecycle element detected:"+activityCode.getValue());
						}
					}					
				}
				
				extractedProcesses.add(currentProcess);
        	}
        }
        
        //filter processes based on the equality
        //because processes are build from traces (which are process instances) and so the same
        //process could be placed in the list multiple times (or process execution "way" (activity order))
        //later for each process or process execution way a test will be "constructed" or assumed as existing (using this as the test path)
        removeDuplicates(extractedProcesses);
                
        return Tuple.of(
        		(List<Activity>)new ArrayList<>(knownActivities.values()), 
        		extractedProcesses);
	}
	
	private static Tuple<List<Activity>,List<BProcess>> parseLogBPIC(XesXmlParser parser, File xesFile) throws Exception {
		
		List<XLog> xLogs = parser.parse(xesFile);
		
		// key = concept:name
		HashMap<String, Activity> knownActivities = new HashMap<>();
		List<BProcess> extractedProcesses = new ArrayList<>();
		
        for (XLog xLog : xLogs) {
        	List<XEventClassifier> eventClassifiers = xLog.getClassifiers();
        	
        	for (XTrace trace : xLog) {
        		
        		XAttributeMap attributeMapTrace = trace.getAttributes();
				XAttribute tranceName = attributeMapTrace.get("concept:name");

				//Assumption 1 trace = 1 process instance
				//later duplicates will be filtered and one left process will become at least one test 
				
				BProcess currentProcess = null;
				
				for (XEvent event : trace) {
					
					XAttributeMap attributeMap = event.getAttributes();
					
					XAttributeLiteral activityCode = (XAttributeLiteral)attributeMap.get("concept:name");    					
					XAttributeLiteral activityName = (XAttributeLiteral)attributeMap.get("activityNameEN");
					XAttributeTimestamp executionTime = (XAttributeTimestamp)attributeMap.get("time:timestamp");
					XAttributeLiteralImpl finishTime = (XAttributeLiteralImpl)attributeMap.get("dateFinished");
					
					//maybe this value can be used to identify different background systems
					//its based on the activity code
					String backgroundSystemIdentifier = pepareActivityCodeForBackgroundSystem(activityCode.getValue());
					//XAttributeLiteral monitoringResource = (XAttributeLiteral)attributeMap.get("monitoringResource");

					
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.ENGLISH);
				    Date finishDate =  df.parse(finishTime.getValue());  
				    Date startDate = executionTime.getValue();
				    
					//a large part of the data only has a very rough day based finish time
					//for now we are ignoring them
					//same applies for events with a rough start date
				    //and events which run almost forever (more then a week)
				    
					Activity activity = knownActivities.get(activityName.getValue());
					
					if(activity == null)
					{
						activity = Activity.of(activityName.getValue(), backgroundSystemIdentifier);
						knownActivities.put(activityName.getValue(), activity);
					}
					
					// creates processes on the traces and the event orders of the traces	
					// assuming that each trace is ordered on activity execution times
					if(currentProcess == null)
					{
						currentProcess = BProcess.of(activity);
					}
					currentProcess.addActivity(activity);
					
					if(startDate.getHours() == 0 || finishDate.getHours()==0)
					{
						continue;
					}
					else if(finishDate.getTime() - startDate.getTime() > TimeUnit.DAYS.toMillis(7))
					{
						continue;
					}
					
					if(finishDate.getHours()==0)
					{					
						//Execution newExecution = Execution.of(executionTime.getValue(), extendDate(executionTime.getValue()));
						//activity.addExection(newExecution);
					}
					else
					{					
						Execution newExecution = Execution.of(executionTime.getValue(), finishDate);
						activity.addExection(newExecution);
					}				
				}
				
				extractedProcesses.add(currentProcess);
        	}
        }
        
        //filter processes based on the equality
        //because processes are build from traces (which are process instances) and so the same
        //process could be placed in the list multiple times (or process execution "way" (activity order))
        //later for each process or process execution way a test will be "constructed" or assumed as existing (using this as the test path)
        removeDuplicates(extractedProcesses);
                
        return Tuple.of(
        		(List<Activity>)new ArrayList<>(knownActivities.values()), 
        		extractedProcesses);
	}
	
	private static String pepareActivityCodeForBackgroundSystem(String activityCode)
	{
		/* Basically this method strips away some parts of the concept:name to increase their similarity
		 * so 01_HOOFD_011_1 becomes 01_HOOFD_011
		 * The activity code identifies, somehow, the process and probably and we use it as some kind of hack to
		 * "determine" background systems
		 */
		
		String result = activityCode;
		
		int _counter = 0;
		
		// remove all characters after _
		for(int i=0;i<activityCode.length();i++)
		{
			char current = activityCode.charAt(i);
			
			if(current == '_')
			{
				_counter++;
			}
			
			if(_counter == 3)
			{
				result = activityCode.substring(0, i);
				break;
			}
		}
		
		return result;
	}
	
	
	// only based on hashcode and equals! which must be provded by the list content
	private static <T> void removeDuplicates(List<T> list) {
		Set<T> setItems = new HashSet<>(list);
		list.clear();
		list.addAll(setItems);
	}
	
	private static Date extendDate(Date inital)
	{
		Calendar  cal = new GregorianCalendar();
		cal.setTime(inital);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		
		return cal.getTime();
	}
	
	private static void listf(String directoryName, List<File> files) {
	    File directory = new File(directoryName);

	    // get all the files from a directory
	    File[] fList = directory.listFiles();
	    for (File file : fList) {
	        if (file.isFile()) {
	            files.add(file);
	        } else if (file.isDirectory()) {
	            listf(file.getAbsolutePath(), files);
	        }
	    }
	}
}
