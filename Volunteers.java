import java.util.Arrays;

public class Volunteers {
	private int classid;
	
	private short [] skill_value;
	private short [] preferences;
	
	public Volunteers(short [] skill_value,short [] preferences) {
		this.classid = -1;
		this.preferences = preferences;
		this.skill_value = skill_value;
	}
	
	
	public String toString() {
		return "Volunteers [classid=" + String.format("%-3d",classid)+ ", skill_value=" 
				+ String.format("%-"+(skill_value.length*4+3)+"s",Arrays.toString(skill_value)) 
				+ ", preferences=" + String.format("%-"+(preferences.length*4+3)+"s",Arrays.toString(preferences)) + "]";
	}

	int getClassid() {
		return classid;
	}

	void setClassid(int classid) {
		this.classid = classid;
	}

	short[] getSkill_value() {
		return skill_value;
	}
	short getSkill_value(int i) {
		return skill_value[i];
	}

	short[] getPreferences() {
		return preferences;
	}
	short getPreferences(int i) {
		return preferences[i];
	}


	public String toStringOneIndexed() {
		return "Volunteers [classid=" + String.format("%-3d",classid+1)+ ", skill_value=" 
				+ String.format("%-"+(skill_value.length*4+3)+"s",Arrays.toString(skill_value)) 
				+ ", preferences=" + String.format("%-"+(preferences.length*4+3)+"s",Arrays.toString(preferences)) + "]";
	}
	
	
}
