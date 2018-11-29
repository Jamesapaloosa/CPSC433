
public class Slot {
	String startTime;
	String endTime;
	String day;
	int Max;
	int Min;
	
	public Slot(int inMax, int inMin, String inStart, String inDay) {
		Max = inMax;
		Min = inMin;
		startTime = inStart;
		
		day = inDay;
	}
	
	public String getEndTime(){
		return endTime;
	}
	
	public void setEndTime(String endTimeIn){
		endTime = endTimeIn;
	}
	
	public String getStartTime() {
		return startTime;
	}
	
	public String getDay() {
		return day;
	}
	
	public int getMax() {
		return Max;
	}
	
	public int getMin() {
		return Min;
	}
	
	//Method for making sure that two slots are the same, does not compare max and min because those are not always relevant
	public Boolean isSameSlot(Slot OtherSlot){
		if(!OtherSlot.getStartTime().equals(this.startTime))
			return false;
		if(!OtherSlot.getDay().equals( this.day))
			return false;
		return true;
	}
}
