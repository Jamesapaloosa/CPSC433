import java.util.LinkedList;

public class FileData {
	LinkedList<courseItem> courses;
	LinkedList<courseItem> labs;
	String name;
	LinkedList<Slot> courseSlots;
	LinkedList<Slot> labSlots;
	LinkedList<NotCompatible> incompatible;
}
