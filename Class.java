import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;

import com.sun.media.sound.InvalidDataException;

public class Class {
	//private String 	 school_name 		;
	//private String 	 class_name	 		;
	private short	 max_size	 	 	;
	private short 	 current_size	 	;
	private short [] volunteers  	 	;
	private short [] requested_skills	;
	private short [] compromised_skills	;
	private short [] current_skills		;

	public Class(short max_size, short[] requested_skills) throws InvalidDataException {
		super();
		if (max_size <=0 ){
			InvalidDataException IDE = new InvalidDataException("The max size of a class can not be less than one");
			throw IDE;
		}
		if (requested_skills == null){
			NullPointerException NPE = new NullPointerException("The class requests can not be a null pointer");
			throw NPE;
		}
		this.max_size 			= max_size;		
		this.requested_skills 	= requested_skills;
		this.current_size 		= 0 ;
		this.volunteers 		= new short [max_size];
		this.compromised_skills = new short [requested_skills.length];
		this.current_skills     = new short [requested_skills.length];
	}

	public void remove(short volunteer_id,short[] volunteer_skills) throws InvalidAlgorithmParameterException{
		if(isempty()){
			NullPointerException OME = new NullPointerException("Cannot remove from an empty class");
			throw OME;
		}
		if(volunteer_skills==null){
			NullPointerException NPE = new NullPointerException("volunteer_skills may not be null");
			throw NPE;
		}
		if(volunteer_skills.length!=this.requested_skills.length){
			InvalidAlgorithmParameterException IAP = new InvalidAlgorithmParameterException("volunteer_skills of incorrect length expected "+this.requested_skills.length+"got "+volunteer_skills.length);
			throw IAP;
		}
		int index = -1;
		for (int i = 0; i < current_size; i++) {
			if(volunteers[i]==volunteer_id){
				index = i ; 
				break;
			}
		}
		if (index==-1){
			NullPointerException NPE = new NullPointerException("volunteer_id was not in this class");
			throw NPE;
		}
		for (int i = index; i < current_size-1; i++) {
			volunteers[i]=volunteers[i+1];
		}
		for (int i = 0; i < volunteer_skills.length; i++) {
			current_skills[i] -= volunteer_skills [i];
		}
		this.current_size--;
	}
	
	public void assign(short new_volunteer,short[] volunteer_skills) throws InvalidAlgorithmParameterException{
		if(isfull()){
			ArrayIndexOutOfBoundsException AIOB = new ArrayIndexOutOfBoundsException("Cannot assign to a full class");
			throw AIOB;
		}
		if(new_volunteer<0){
			InvalidAlgorithmParameterException IAP = new InvalidAlgorithmParameterException("Cannot assign a negative volunteer id");
			throw IAP;
		}
		if(volunteer_skills==null){
			NullPointerException NPE = new NullPointerException("volunteer_skills may not be null");
			throw NPE;
		}
		if(volunteer_skills.length!=this.requested_skills.length){
			InvalidAlgorithmParameterException IAP = new InvalidAlgorithmParameterException("volunteer_skills of incorrect length expected "+this.requested_skills.length+"got "+volunteer_skills.length);
			throw IAP;
		}
		for (int i = 0; i < volunteer_skills.length; i++) {
			current_skills[i] += volunteer_skills [i];
		}
		volunteers[current_size++] = new_volunteer;
	}

	public String toString() {
		return "Class [max_size=" + String.format("%-2d",max_size) + ", current_size=" + String.format("%-2d",current_size) 
				+ ", volunteers=" + String.format("%-"+(volunteers.length*4+3)+"s",Arrays.toString(volunteers)) 
				+ ", requested_skills=" + String.format("%-"+(requested_skills.length*4+3)+"s",Arrays.toString(requested_skills))
				+ ", compromised_skills=" + String.format("%-"+(compromised_skills.length*4+3)+"s",Arrays.toString(compromised_skills)) 
				+ ", current_skills=" + String.format("%-"+(current_skills.length*4+3)+"s",Arrays.toString(current_skills)) + "]";
	}

	public boolean isfull(){
		return max_size==current_size;
	}
	public boolean isempty(){
		return current_size==0;
	}
	/**
	 * @return the max_size
	 */
	public short getMax_size() {
		return max_size;
	}

	/**
	 * @return the current_size
	 */
	public short getCurrent_size() {
		return current_size;
	}

	/**
	 * @return the volunteers
	 */
	public short[] getVolunteers() {
		return volunteers;
	}
	public short getVolunteers(int i) {
		return volunteers [i];
	}

	/**
	 * @return the requested_skills
	 */
	public short[] getRequested_skills() {
		return requested_skills;
	}
	public short getRequested_skills(int i) {
		return requested_skills[i];
	}
	/**
	 * @return the compromised_skills
	 */
	public short[] getCurrent_skills() {
		return current_skills;
	}
	public short getCurrent_skills(int i) {
		return current_skills[i];
	}

	/**
	 * @return the compromised_skills
	 */
	public short[] getCompromised_skills() {
		return compromised_skills;
	}
	public short getCompromised_skills(int i){
		return compromised_skills[i];
	}

	/**
	 * @param current_size the current_size to set
	 */
	public void setCurrent_size(short current_size) {
		this.current_size = current_size;
	}

	/**
	 * @param volunteers the volunteers to set
	 */
	public void setVolunteers(short[] volunteers) {
		this.volunteers = volunteers;
	}

	/**
	 * @param compromised_skills the compromised_skills to set
	 */
	public void setCompromised_skills(short[] compromised_skills) {
		this.compromised_skills = compromised_skills;
	}
	public void setCompromised_skills(int i ,short compromised_skill){
		this.compromised_skills[i] = compromised_skill;
	}

	public String get_summery() {
		return("Looked for    :"
				+String.format("%-"+requested_skills.length*2+3+"s",Arrays.toString(requested_skills))
				+"\nGot           :"
				+String.format("%-"+requested_skills.length*2+3+"s",Arrays.toString(current_skills)));
	}

}
