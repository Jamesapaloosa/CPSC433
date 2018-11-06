import java.util.LinkedList;

public class FileData {
	LinkedList<courseItem> courses;
	LinkedList<courseItem> labs;
	String name;
	LinkedList<Slot> courseSlots;
	LinkedList<Slot> labSlots;
	LinkedList<CoursePair> incompatible;
	LinkedList<TimeCoursePair> unwanted;
	LinkedList<CoursePair> pair;
	LinkedList<TimeCoursePair> preferences;
	LinkedList<TimeCoursePair> preAssigned;
	
}