/**
 * @author Christian Roatis
 *
 */

import java.util.*;

public class Constr {

    static LinkedList<Timeslot> timeslots = new LinkedList<Timeslot>();
    static LinkedList<courseItem> items = new LinkedList<courseItem>();
  
//------------------------------------------------------------------------------------------------------------
//This section holds all functions that check the hard constraints of any given state
    /*
	// Ensure courseMax or labMax isn't violated in a current state
	private static Boolean maxAndOverlapCheck(State currentState){
		//state = currentState;
		//timeslots = state.timeSlots; 
		/*
		// Check every time-slot in a state to make sure no coursemax/labmax is violated, nor do any labs/tutorials share the same slot as their corresponding course
		for (int i=0; i < timeslots.size(); i++){	
			Timeslot currentSlot = timeslots.get(i);
			for(int j = 0; j < currentSlot.assignedItems.size(); j++){
				boolean validCourse = currentSlot.forCourses && (Arrays.stream(DataParser.validLecType).anyMatch(currentSlot.assignedItems.get(j).getLecVsTut()::equals) && currentSlot.assignedItems.get(j).getTutVLab() == "");
				boolean validTut = !currentSlot.forCourses && (Arrays.stream(DataParser.validTutType).anyMatch(currentSlot.assignedItems.get(j).getLecVsTut()::equals) || Arrays.stream(DataParser.validTutType).anyMatch(currentSlot.assignedItems.get(j).getTutVLab()::equals));
				if(!validCourse && !validTut)
					return false;
			}
		}
		
		return true;
	}
	*/
	//Makes sure all classes that are needed are contained in the stimeslots
	private static Boolean confirmAllClassesAssigned(State currentState){
		Timeslot timeslot;
		int LoopSize;
		LinkedList<courseItem> valuesToFind = (LinkedList<courseItem>)items.clone();
		boolean found;
		for(int j = 0; j < currentState.timeSlots.size(); j++){
			timeslot = currentState.timeSlots.get(j);
			LoopSize = timeslot.assignedItems.size();
			for(int k = 0; k < LoopSize; k++){
				found = false;
				for(int i = 0; i < valuesToFind.size(); i++){
					if(valuesToFind.get(i).isSameCourseItems(timeslot.assignedItems.get(k))){
						found = true;
						valuesToFind.remove(i);
						LoopSize = timeslot.assignedItems.size();
						break;
					}
				}
				if(!found)
					return false;
			}
		}
		
		return true;
	}
	
	//Goes through the code and makes sure that no class exists twice
	private static Boolean noDuplicates(State currentState) {
        timeslots = currentState.timeSlots;
        Timeslot fromSlot;
        Timeslot toSlot;
        courseItem fromCourse;
        courseItem toCourse;
        int compareToIndex;
        for(int i = 0; i < timeslots.size(); i++){
        	fromSlot = timeslots.get(i);
        	for(int j = 0; j < fromSlot.assignedItems.size(); j++){
        		fromCourse = fromSlot.assignedItems.get(j);
        		for(int k = i; k < timeslots.size(); k++){
        			toSlot = timeslots.get(k);
        			if (k == i)
        				compareToIndex = j + 1;
        			else 
        				compareToIndex = 0;
        			for(int l = compareToIndex; l < toSlot.assignedItems.size(); l++){
        				toCourse = toSlot.assignedItems.get(l);
        				if(fromCourse.isSameCourseItems(toCourse))
        					return false;
        					
        			}
        		}
        	}
        }
        return true;
        /*Christians version
        for (int i=0; i < timeslots.size(); i++){    
            Timeslot currentSlot = timeslots.get(i);
            for (int j = 0; j < currentSlot.assignedItems.size(); j++){
                currentItem = currentSlot.assignedItems.get(j);

                for (int k=0; k<items.size(); k++){
                    if(currentItem.isSameCourseItems(items.get(k))){
                        return false;
                    } 
                }
                items.add(currentItem);
            }
        }
        return true;
        */
    }

	private static Boolean tuesdayCourseCheck(State currentState){
		timeslots = currentState.timeSlots; 

		// Check every timeslot in a state to make sure no coursemax/labmax is violated, nor do any labs/tutorials share the same slot as their corresponding course
		for (int i=0; i < timeslots.size(); i++){	
			Timeslot currentSlot = timeslots.get(i);

			if((currentSlot.localSlot.day.equals("TU"))&& (currentSlot.localSlot.startTime.equals("11:00"))) {
				if ((currentSlot.forCourses == true)&&(currentSlot.assignedItems.size() > 0)){
					return false;
				}
			}

		}
		return true;
	}

	private static Boolean eveningLecCheck(State currentState){
		timeslots = currentState.timeSlots; 
		String[] eveningSlots = {"18:00", "18:30", "19:00", "20:00"};

		// Check every timeslot in a state to make sure no coursemax/labmax is violated, nor do any labs/tutorials share the same slot as their corresponding course
		for (int i=0; i < timeslots.size(); i++){	
			Timeslot currentSlot = timeslots.get(i);

			for (int j=0; j < currentSlot.assignedItems.size(); j++) {	
				if ((currentSlot.assignedItems.get(j).isALec == true) && (currentSlot.assignedItems.get(j).section.equals("09"))) {
					if (Arrays.stream(eveningSlots).anyMatch(currentSlot.localSlot.startTime::equals)){}
					else 
						return false;
				}
			}
		}
		return true;
	}


	// Check that no two 500 level courses are assigned in the same slot in any given state
	private static Boolean check500(State currentState){
		timeslots = currentState.timeSlots; 
		int count = 0;

		// Check every timeslot in a state to make sure no more than one 500 level class occupies any one timeslot
		for (int i=0; i < timeslots.size(); i++){	
			Timeslot currentSlot = timeslots.get(i);
			count = 0;

			for (int j=0; j < currentSlot.assignedItems.size(); j++) {
				if ((currentSlot.assignedItems.get(j).isALec == true) && (Integer.parseInt(currentSlot.assignedItems.get(j).number) >= 500) && (Integer.parseInt(currentSlot.assignedItems.get(j).number) < 600)) {
					count++;
					if (count > 1){
						return false;
					}
				}
			}
		}
		return true;
	}

	// Deal with the complicated CPSC 813/913 scheduling and overlap rules
	private static Boolean check13(State currentState){
		timeslots = currentState.timeSlots; 

		// Ensure CPSC 813 and 913 are scheduled only during the TU timeslot starting at 18:00.
		for (int i=0; i < timeslots.size(); i++){	
			Timeslot currentSlot = timeslots.get(i);

			for(int j = 0; j < currentSlot.assignedItems.size(); j++){
				
				// If CPSC 813 or 913 are not scheduled TU at 18:00, return false
				if(!(currentSlot.localSlot.day.equals("TU")) && !(currentSlot.localSlot.startTime.equals("18:00"))) {
					if ((currentSlot.assignedItems.get(j).isALec == true) && ((currentSlot.assignedItems.get(j).number.equals("813")) || (currentSlot.assignedItems.get(j).number.equals("913")))){
						return false;
					}
				}

				// If CPSC 813 is scheduled TU at 18:00 but so is any element of CPSC 313, return false
				else if (((currentSlot.localSlot.day.equals("TU")) && (currentSlot.localSlot.startTime.equals("18:00"))) && (currentSlot.assignedItems.get(j).number.equals("813"))){
					for(int k = 0; k < currentSlot.assignedItems.size(); k++) {
						if (currentSlot.assignedItems.get(k).number.equals("313")){
							return false;
						}
					}
				}
				
				// If CPSC 913 is scheduled TU at 18:00 but so is any element of CPSC 413, return false
				else if (((currentSlot.localSlot.day.equals("TU")) && (currentSlot.localSlot.startTime.equals("18:00"))) && (currentSlot.assignedItems.get(j).number.equals("913"))){
					for(int k = 0; k < currentSlot.assignedItems.size(); k++) {
						if (currentSlot.assignedItems.get(k).number.equals("413")){
							return false;
						}
					}
				}
			}
		}		
		return true;
	}

	// Deal with the CPSC 813 and 913 being scheduled outside allowed times
	private static Boolean schedule13(State currentState){
		timeslots = currentState.timeSlots; 

		for (int i=0; i < timeslots.size(); i++){	
			Timeslot currentSlot = timeslots.get(i);
			for(int j = 0; j < currentSlot.assignedItems.size(); j++){
				if(!(currentSlot.localSlot.day.equals("TU")) && !(currentSlot.localSlot.startTime.equals("18:00"))) {
					if ((currentSlot.assignedItems.get(j).isALec == true) && ((currentSlot.assignedItems.get(j).number.equals("813")) || (currentSlot.assignedItems.get(j).number.equals("913")))){
						return false;
					}
				}
			}
		}
		return true;
	}

		// Check incompatible classes aren't scheduled at the same times
	private static Boolean checkIncompatible(State currentState, LinkedList<CoursePair> incompClasses){
		timeslots = currentState.timeSlots; 
		int incompItems = 0;

		for (int i=0; i < timeslots.size(); i++){	

			for (int j=0; j < incompClasses.size(); j++){
				incompItems = 0;
				CoursePair cp = incompClasses.get(j);
				courseItem c1 = cp.getItemOne();
				courseItem c2 = cp.getItemTwo();

				for (int k=0; k < timeslots.get(i).assignedItems.size(); k++){
					courseItem item = timeslots.get(i).assignedItems.get(k);

					if(item.isSameCourseItems(c1)||item.isSameCourseItems(c2)){
						incompItems++;
						if(incompItems > 1)
							return false;
					}
				}
			}
		}
		return true;
	}
	
	private static Boolean checkPreassigned(State currentState, LinkedList<TimeCoursePair> preAssigned) {
		timeslots = currentState.timeSlots;
		
		for (int i=0; i < timeslots.size(); i++){	

			for (int j=0; j < preAssigned.size(); j++){

				TimeCoursePair pa = preAssigned.get(j);
				courseItem c = pa.getCourseItem();
				Slot s = pa.getTime();

				for (int k=0; k < timeslots.get(i).assignedItems.size(); k++){
					courseItem item = timeslots.get(i).assignedItems.get(k);

					if(item.isSameCourseItems(c)){
						if (!timeslots.get(i).localSlot.startTime.equals(s.startTime))
							return false;
					}
				}
			}
		}
		return true;
	}

	// Check that labs on Fridays don't overlap with any of their course sections 
	private static Boolean checkFridays(State currentState){
		timeslots = currentState.timeSlots; 
		LinkedList<Timeslot> fridayCourses = new LinkedList<Timeslot>();
		LinkedList<Timeslot> fridayLabs = new LinkedList<Timeslot>();
		
		// A linked list holding courses that start in the middle of a lab slot, in the format: Course Slot -> Lab Slot etc. 
		LinkedList<Timeslot> problemSlots = new LinkedList<Timeslot>();
		
		Timeslot currentCourseSlot;
		Timeslot currentLabSlot;
		
		// Filter all timeslots into two separate linked lists; one for Friday courses and one for Friday labs
		for (int i = 0; i < timeslots.size(); i++) {
			if((timeslots.get(i).localSlot.day.equals("FR")) && (timeslots.get(i).forCourses == false)) {
				fridayLabs.add(timeslots.get(i));
			}
			else if ((timeslots.get(i).localSlot.day.equals("FR")) && (timeslots.get(i).forCourses == true))
				fridayCourses.add(timeslots.get(i));
		}
		
		
		// For every Friday Lab Timeslot, ensure none of its assigned items overlap with a corresponding course
		for (int i=0; i < fridayLabs.size(); i++){		
			String[] splitStartTime = fridayLabs.get(i).localSlot.startTime.split(":");
			String startTime = splitStartTime[0];
			int labStart = Integer.parseInt(startTime);
			
			for (int j = 0; j < fridayCourses.size(); j++) {
				splitStartTime = fridayCourses.get(i).localSlot.startTime.split(":");
				startTime = splitStartTime[0];
				int courseStart = Integer.parseInt(startTime);
				
				if(courseStart == (labStart + 1)) {
					problemSlots.add(fridayCourses.get(j));
					problemSlots.add(fridayLabs.get(i));
				}
			}
		}
		// Loop to check if any of the problem slots house an overlap of a lab with its corresponding course
		for (int i=0; i< problemSlots.size(); i+=2 ) {
			currentCourseSlot = problemSlots.get(i);
			currentLabSlot = problemSlots.get(i+1);
			
			for(int j=0; j<currentCourseSlot.assignedItems.size(); j++) {
				for (int k=0; k<currentLabSlot.assignedItems.size(); k++) {
					if((currentCourseSlot.assignedItems.get(i).department.equals(currentLabSlot.assignedItems.get(j).department)) && (currentCourseSlot.assignedItems.get(i).number.equals(currentLabSlot.assignedItems.get(j).number))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	// Check that labs on Tuesdays don't overlap with any of their course sections 
	private static Boolean checkTuesdays(State currentState){
		timeslots = currentState.timeSlots; 
		LinkedList<Timeslot> tuesdayCourses = new LinkedList<Timeslot>();
		LinkedList<Timeslot> tuesdayLabs = new LinkedList<Timeslot>();
		
		// A linked list holding courses that start in the middle of a lab slot, in the format: Course Slot -> Lab Slot etc. 
		LinkedList<Timeslot> problemSlots = new LinkedList<Timeslot>();
		
		Timeslot currentCourseSlot;
		Timeslot currentLabSlot;
		
		// Filter all timeslots into two separate linked lists; one for Tuesday courses and one for Tuesday labs
		for (int i = 0; i < timeslots.size(); i++) {
			if((timeslots.get(i).localSlot.day.equals("TU")) && (timeslots.get(i).forCourses == false)) {
				tuesdayLabs.add(timeslots.get(i));
			}
			else if ((timeslots.get(i).localSlot.day.equals("TU")) && (timeslots.get(i).forCourses == true))
				tuesdayCourses.add(timeslots.get(i));
		}
		
		
		// For every Friday Lab Timeslot, ensure none of its assigned items overlap with a corresponding course
		for (int i=0; i < tuesdayLabs.size(); i++){		
			String[] splitTime = tuesdayLabs.get(i).localSlot.startTime.split(":");
			String startTime = splitTime[0];
			int labStart = Integer.parseInt(startTime);
			
			splitTime = tuesdayLabs.get(i).localSlot.endTime.split(":");
			String endTime = splitTime[0];
			int labEnd = Integer.parseInt(endTime);
			
			for (int j = 0; j < tuesdayCourses.size(); j++) {
				String[] courseStartTime = tuesdayCourses.get(i).localSlot.startTime.split(":");
				String[] courseEndTime = tuesdayCourses.get(i).localSlot.endTime.split(":");
				
				if((Integer.parseInt(courseStartTime[0]) == (labStart)) || (Integer.parseInt(courseEndTime[0]) == (labEnd))) {
					problemSlots.add(tuesdayCourses.get(j));
					problemSlots.add(tuesdayLabs.get(i));
				}
			}
		}
		// Loop to check if any of the problem slots house an overlap of a lab with its corresponding course
		for (int i=0; i< problemSlots.size(); i+=2 ) {
			currentCourseSlot = problemSlots.get(i);
			currentLabSlot = problemSlots.get(i+1);
			
			for(int j=0; j<currentCourseSlot.assignedItems.size(); j++) {
				for (int k=0; k<currentLabSlot.assignedItems.size(); k++) {
					if((currentCourseSlot.assignedItems.get(i).department.equals(currentLabSlot.assignedItems.get(j).department)) && (currentCourseSlot.assignedItems.get(i).number.equals(currentLabSlot.assignedItems.get(j).number))) {
						return false;
					}
				}
			}
		}
		return true;
	}
//------------------------------------------------------------------------------------------------------------
//This section holds all functions that check the hard constraints when attempting an assignment

	// Check that you won't have more than one 500 level course in a given timeslot
	private static Boolean eveningLecAssign(Timeslot timeSlot, courseItem item){
		String[] eveningSlots = {"18:00", "18:30", "19:00", "20:00"};

		if(item.isALec == true){
			String lecNum = item.section;
			if (lecNum.equals("09")){
				if (Arrays.stream(eveningSlots).anyMatch(timeSlot.localSlot.startTime::equals))
					return true;
				else 
					return false;
			}	
			
		}
		return true;
	}


	// Check every assignment in a timeslot to ensure there are no other 500 level courses currently assigned
	private static Boolean assign500(Timeslot timeSlot){
		for (int i=0; i < timeSlot.assignedItems.size(); i++)
			if ((timeSlot.assignedItems.get(i).isALec == true) && (Integer.parseInt(timeSlot.assignedItems.get(i).number) > 500) && (Integer.parseInt(timeSlot.assignedItems.get(i).number) < 600))
				return false;
		return true;
	}

	// When assigning either CPSC 813 or 913, it must be assigned to TU at 18:00
	private static Boolean assign13(Timeslot timeslot, courseItem item){
		if ((item.number.equals("813") && (item.isALec == true))){
			if ((!(timeslot.localSlot.day.equals("TU")) || !(timeslot.localSlot.startTime.equals ("18:00")))){	
				return false;
			}
		}
		
		else if (item.number.equals("913") && (item.isALec == true)){
			if (!(timeslot.localSlot.day.equals("TU")) || !(timeslot.localSlot.startTime.equals ("18:00"))){	
				return false;
			}
		}		
		return true;
	}
	
	// Check incompatible classes aren't scheduled at the same times
	private static Boolean checkIncompatibleAssign(Timeslot timeslot, courseItem item, LinkedList<CoursePair> incompClasses){
		int incompItems = 0;
		for (int i=0; i < timeslot.assignedItems.size(); i++){	
	
			for (int j=0; j < incompClasses.size(); j++){
				incompItems = 0;
				CoursePair cp = incompClasses.get(j);
				courseItem c1 = cp.getItemOne();
				courseItem c2 = cp.getItemTwo();
				
				if(item.isSameCourseItems(c1) || item.isSameCourseItems(c2)){
					incompItems++;
				}
				
				for (int k=0; k < timeslot.assignedItems.size(); k++){
					courseItem currentItem = timeslot.assignedItems.get(k);
	
					if(currentItem.isSameCourseItems(c1)||currentItem.isSameCourseItems(c2)){
						incompItems++;
						if(incompItems > 1)
							return false;
					}
				}
			}
		}
		return true;
	}



//------------------------------------------------------------------------------------------------------------
//This section holds all the complete check possiblities for Constr; includes Constr.assign, Constr.partial and Constr.final

	// Run Constr on a final solution
	public static Boolean finalCheck(State currentState, LinkedList<CoursePair> inc, LinkedList<TimeCoursePair> preAssigned){

		if(!confirmAllClassesAssigned(currentState))
			return false;
		//(maxAndOverlapCheck(state)) was removed
		if ((tuesdayCourseCheck(currentState)) && eveningLecCheck(currentState) && check500(currentState) && check13(currentState) && schedule13(currentState) && noDuplicates(currentState) && checkIncompatible(currentState, inc) && checkPreassigned(currentState, preAssigned) && checkFridays(currentState) && checkTuesdays(currentState))
			return true;
		return false;
	}
	

	// Run Constr on a partial solution
	public static Boolean partial(State currentState, LinkedList<CoursePair> inc, LinkedList<TimeCoursePair> preAssigned){
		
	//(maxAndOverlapCheck(state)) was removed
		if ((tuesdayCourseCheck(currentState)) && eveningLecCheck(currentState) && check500(currentState) && check13(currentState) && schedule13(currentState) && noDuplicates(currentState) && checkIncompatible(currentState, inc) && checkPreassigned(currentState, preAssigned) && checkFridays(currentState) && checkTuesdays(currentState)) 
			return true;
		return false;
	}

	
	// Run Constr on an assignment
	public static Boolean assign(Timeslot ts, courseItem ci, LinkedList<CoursePair> inc){
		if (eveningLecAssign(ts, ci) && assign500(ts) && assign13(ts, ci) && checkIncompatibleAssign(ts, ci, inc))
			return true;
		return false;

	}


}