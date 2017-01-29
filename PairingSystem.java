import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.StageStyle;


public class PairingSystem {
	static Volunteers [] volunteers;
	static Class  	  [] classrooms;
	static String 	  [] skills;
	static String 	  [] class_headings;
	static String 	  [] class_names; 
	static int 	 	  [] class_skill_indexs;
	static short 	  [] starting_skill_demand;
	static short 	  [] starting_skill_supply;
	static String 	  [] volunteer_headings;
	static short 	  [] volunteer_skill_indexs;
	static short 	  [] volunteer_class_indexs;
	static CellStyle  [] styles;
	static short 	  [] colors = {40,41,42,43,44,45,46,47,49,50,51,52,53,71,80,26,27,31,24,29,48,10,11,13,14,15,22,23,25,30,33,34,35,54,55};
	static Workbook  	 workbook;
	static int 			 number_skills;
	static int 			 number_classrooms;
	static int 			 number_volunteers;
	static String 		 log;

	/**
	 * The actual algorithm used to make matches according to the volunteer's preferences
	 * @param file_path	The fileName passed from the GUI determining where the source data is
	 * @return			An error/success string depending on performance
	 * @throws InvalidAlgorithmParameterException
	 * @throws IOException 
	 */
	public static String pairingSystem(String file_path) throws InvalidAlgorithmParameterException, IOException{

		log = "File Path: " + file_path + "\n\n";
		try {

			workbook  = Read_file (file_path);
			Read_bacic_infomation (workbook);
			Read_class_list		  (workbook);
			Read_volunteer_list	  (workbook);

			boolean asked [][] = new boolean [number_classrooms][number_volunteers];
			boolean end   []   = new boolean [number_classrooms];

			int [] skill_demand  = get_demand(classrooms,number_skills);
			int [] skill_supply  = get_supply(volunteers,number_skills);

			while(pair_once(asked,classrooms,volunteers,number_skills,end));

			log += log_sets();
			System.out.println(log);

			return ("0 " + description(skill_demand, skill_supply));

		} catch (Exception e) {
			printError(e, log);
			String temp = e.getClass().getSimpleName()+"  "+e.getMessage();
			errorBox("Error: "+e.getMessage(), "Error: Ignite Mentoring");
			StackTraceElement a[] = e.getStackTrace();
			for (int i = 0; i < a.length; i++) {
				temp = temp+"\n\tat "+a[i].getMethodName()+(a[i].getLineNumber()>-1?" line "+a[i].getLineNumber():"")+(a[i].getFileName()!=null?" "+a[i].getFileName():"");
			}
			return ("1 " + temp + "\n");
		}
	}

	private static String log_sets() {
		int [] skill_demand  =get_demand(classrooms,number_skills );
		int [] skill_supply = get_supply(volunteers,number_skills);

		String out = "\nDemand: \t";
		out += (" "+Arrays.toString (skill_demand))+"\nSupply: \t";
		out += (" "+Arrays.toString (skill_supply)) +"\n\nClasses";

		for (int i = 0; i < classrooms.length; i++) {
			out+=("\n\nClass ID "+String.format("%2d ",i+1)+" maxsize "+String.format("%-3d\n\n",classrooms[i].getMax_size()));
			out+=(classrooms[i].get_summery()+"\n");


			for (int j = 0; j < classrooms[i].getCurrent_size()-1; j++) {
				out+=(String.format("volunteersID %3d",classrooms[i].getVolunteers(j)+1)+" "+volunteers[classrooms[i].getVolunteers(j)].toStringOneIndexed()+"\n");
			}
		}
		out+="\n\nVolunteers\n";
		for (int j = 0; j < volunteers.length; j++) {
			out+=(String.format("volunteersID %3d",j+1)+" "+volunteers[j].toStringOneIndexed()+"\n");
		}
		out += "\n";
		return out;
	}
	private static String description(int [] starting_skill_demand , int[] starting_skill_supply) {

		int max = 0;
		for (int i = 0; i < skills.length; i++){
			max = Math.max(skills[i].length(), max);
		}
		for(int i = 0; i < skills.length; i++){
			while (skills[i].length()<=max){
				skills[i]=skills[i]+" ";
			}
		}

		String out = "Supply:\n\n";
		for (int i = 0; i < starting_skill_supply.length; i++) {
			out = out +"  "+skills[i]+"\t: "+starting_skill_supply[i]+"\n";
		}
		out += " \n Demand:\n\n";
		for (int i = 0; i < starting_skill_demand.length; i++) {
			out = out +"  "+skills[i]+"\t: "+starting_skill_demand[i]+"\n";
		}
		boolean schoolmissing = false;
		for (int i = 0; i < classrooms.length; i++) {
			for (int j = 0; j < starting_skill_supply.length; j++) {
				if(classrooms[i].getRequested_skills(j)-classrooms[i].getCurrent_skills(j)>0){
					schoolmissing = true;
					j = starting_skill_supply.length;
					i = classrooms.length;
				}
			}
		}

		if(schoolmissing){	
			out += "\n\nThe following schools are missing a set of requested skills"+":\n\n";
			for (int i = 0; i < classrooms.length; i++) {
				int [] missing = new int [number_skills];
				boolean full = true;
				for (int j = 0; j < starting_skill_supply.length; j++) {
					missing[j] = classrooms[i].getRequested_skills(j)-classrooms[i].getCurrent_skills(j);
					if(missing[j] > 0){
						full = false;

						if(!full){
							out = out + "  "+class_names[i]+"\n";
							for (int j2 = 0; j2 < missing.length; j2++) {
								if(missing[j2]>0)
									out = out +"\t"+skills[j2]+"\t: "+missing[j2]+"\n";
							}
							out = out + "\n";
						}
					}
				}

			}
		}

		String vol = "\t";
		int number_of_unsed = 0 ;
		for (int i = 0; i < volunteers.length; i++) {
			if(volunteers[i].getClassid()==-1){
				vol = vol + "\n\t volunteers Id"+i+"\n";
				number_of_unsed++;
			}
		}
		if (number_of_unsed == 0)
			out = out + "\nAll volunteers were allocated\n"+vol;
		else if (number_of_unsed == 1)
			out = out + "\n" + vol + " is the only unallocated volunteer\n";
		else
			out = out + "\nThere are "+number_of_unsed+" unallocated volunteers:\n"+vol;

		return out;
	}

	//XSSF
	@SuppressWarnings("deprecation")
	public static void makeStyles (Workbook newWorkbook){
		styles = new CellStyle [number_classrooms];

		for (int j = 0; j < number_classrooms; j++) {

			short  myColor = colors[j%colors.length];

			styles [j] = newWorkbook.createCellStyle();
			styles [j].setFillForegroundColor(myColor);
			styles [j].setFillPattern(CellStyle.SOLID_FOREGROUND);

		}

	}

	public static void save (String file_path) throws Exception{
		if(file_path.endsWith(".xls")){
			HSSFWorkbook newfile1  = new HSSFWorkbook();
			makeStyles (newfile1);
			HSSF_save_volunteers       (newfile1,workbook.getSheetAt(2));
			HSSF_save_volunteers_unused(newfile1,workbook.getSheetAt(2));
			save_class (newfile1,workbook.getSheetAt(1),workbook.getSheetAt(2));
			FileOutputStream fileOut;

			try {
				fileOut = new FileOutputStream(file_path);
				newfile1.write(fileOut);
				fileOut.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				printError(e,log);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				printError(e,log);
				e.printStackTrace();
			}
		}else if (file_path.endsWith(".xlsx")){
			XSSFWorkbook newfile2  = new XSSFWorkbook();
			makeStyles (newfile2);
			HSSF_save_volunteers       (newfile2,workbook.getSheetAt(2));
			HSSF_save_volunteers_unused(newfile2,workbook.getSheetAt(2));
			save_class (newfile2,workbook.getSheetAt(1),workbook.getSheetAt(2));
			FileOutputStream fileOut;

			try {
				fileOut = new FileOutputStream(file_path);
				newfile2.write(fileOut);
				fileOut.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				printError(e,log);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				printError(e,log);
				e.printStackTrace();
			}			
		}
	}

	public static void HSSF_save_volunteers (Workbook newWorkbook,Sheet oldsheet) throws Exception {
		Sheet newsheet = newWorkbook.createSheet("Volunteers");
		Row   row 	   = newsheet.createRow(0);

		Font font 		= newWorkbook.createFont();
		font.setBold(true);
		font.setFontHeight((short)280);


		CellStyle style = newWorkbook.createCellStyle();
		style.setFont(font);

		row.createCell(0);
		row.getCell(0).setCellValue("Volunteers");
		row.getCell(0).setCellStyle(style);	

		row = newsheet.createRow(2);
		Row oldrow = oldsheet.getRow(0);

		boolean print [] = new boolean [volunteer_headings.length];
		for (int i = 0; i < volunteer_class_indexs.length; i++) {
			print[volunteer_class_indexs[i]]=true;
		}
		for (int i = 0; i < volunteer_skill_indexs.length; i++) {
			print[volunteer_skill_indexs[i]]=true;
		}


		int k = 0;
		for (int i = 0; i < volunteer_headings.length; i++) {
			if(!print[i]){
				row.createCell(k++).setCellValue(volunteer_headings[i]);
			}
		}
		row.createCell(k++).setCellValue("Class");

		for (int j = 0; j < number_volunteers; j++) {
			row = newsheet.createRow(3+j);
			oldrow = oldsheet.getRow(1+j);


			if(volunteers[j].getClassid()!=-1){
				row.createCell(k);
				row.getCell(k++).setCellStyle(styles[volunteers[j].getClassid()]);
				k = 0;
				for (int i = 0; i < volunteer_headings.length; i++) {
					if(!print[i]){
						row.createCell(k).setCellValue(Read_String_no_data(oldrow.getCell(i)));
						if(volunteers[j].getClassid()!=-1)
							row.getCell(k).setCellStyle(styles[volunteers[j].getClassid()]);
						k++;
					}
				}
				row.createCell(k).setCellValue(class_names[volunteers[j].getClassid()]);
				if(volunteers[j].getClassid()!=-1)
					row.getCell(k).setCellStyle(styles[volunteers[j].getClassid()]);
				k++;
			}
		}

	}
	public static void HSSF_save_volunteers_unused (Workbook newWorkbook,Sheet oldsheet) throws Exception {
		Sheet newsheet = newWorkbook.createSheet("Unused Volunteers");
		Row   row 	   = newsheet.createRow(0);

		Font font 		= newWorkbook.createFont();
		font.setBold(true);
		font.setFontHeight((short)280);


		CellStyle style = newWorkbook.createCellStyle();
		style.setFont(font);

		row.createCell(0);
		row.getCell(0).setCellValue("Unused Volunteers");
		row.getCell(0).setCellStyle(style);	

		row = newsheet.createRow(2);
		Row oldrow = oldsheet.getRow(0);

		boolean print [] = new boolean [volunteer_headings.length];
		for (int i = 0; i < volunteer_class_indexs.length; i++) {
			print[volunteer_class_indexs[i]]=true;
		}
		for (int i = 0; i < volunteer_skill_indexs.length; i++) {
			print[volunteer_skill_indexs[i]]=true;
		}


		int k = 0;
		for (int i = 0; i < volunteer_headings.length; i++) {
			if(!print[i]){
				row.createCell(k++).setCellValue(volunteer_headings[i]);
			}
		}

		for (int j = 0; j < number_volunteers; j++) {
			if(volunteers[j].getClassid()==-1){
				row = newsheet.createRow(3+j);
				oldrow = oldsheet.getRow(1+j);
				k = 0;
				for (int i = 0; i < volunteer_headings.length; i++) {
					if(!print[i]){
						row.createCell(k).setCellValue(Read_String_no_data(oldrow.getCell(i)));
						k++;
					}
				}
			}
		}
	}
	public static void save_class (Workbook newWorkbook,Sheet oldsheet,Sheet oldvsheet) throws Exception{
		Sheet newsheet = newWorkbook.createSheet("Classes");

		boolean printclass [] = new boolean [class_headings.length];
		for (int i = 0; i < class_skill_indexs.length; i++) {
			printclass[class_skill_indexs[i]]=true;
		}
		boolean print [] = new boolean [volunteer_headings.length];
		for (int i = 0; i < volunteer_class_indexs.length; i++) {
			print[volunteer_class_indexs[i]]=true;
		}
		for (int i = 0; i < volunteer_skill_indexs.length; i++) {
			print[volunteer_skill_indexs[i]]=true;
		}

		int k = 0;
		for (int i = 0; i < class_headings.length; i++) {
			Row   row 	   = newsheet.createRow(k++);
			int kr = 0 ;
			row.createCell(kr++).setCellValue("Name");
			row.getCell(kr-1).setCellStyle(styles[i]);
			for (int j = 0; j < printclass.length; j++) {
				if(!printclass[j]){
					row.createCell(kr++).setCellValue(class_headings[j]);
					row.getCell(kr-1).setCellStyle(styles[i]);
				}

			}
			row 	   = newsheet.createRow(k++);
			kr = 0;

			row.createCell(kr++).setCellValue(class_names[i]);
			row.getCell(kr-1).setCellStyle(styles[i]);
			for (int j = 0; j < printclass.length; j++) {
				if(!printclass[j]){
					row.createCell(kr++).setCellValue(Read_String_no_data(oldsheet.getRow(i+1).getCell(j)));
					row.getCell(kr-1).setCellStyle(styles[i]);
				}
			}
			row 	   = newsheet.createRow(k++);
			kr= 0 ;
			for (int v = 0; v < volunteer_headings.length; v++) {
				if(!print[v]){
					row.createCell(kr++).setCellValue(volunteer_headings[v]);

					row.getCell(kr-1).setCellStyle(styles[i]);
				}
			}
			kr= 0 ;
			for (int j = 0; j < classrooms[i].getCurrent_size(); j++) {
				row = newsheet.createRow(k++);
				kr=0;
				Row oldrow = oldvsheet.getRow(classrooms[i].getVolunteers(j)+1);
				for (int iv = 0; iv < volunteer_headings.length; iv++) {
					if(!print[iv]){
						row.createCell(kr++).setCellValue(Read_String_no_data(oldrow.getCell(iv)));

						row.getCell(kr-1).setCellStyle(styles[i]);
					}
				}

			}
			row 	   = newsheet.createRow(k++);
		}

	}

	private static void 	Read_bacic_infomation	(Workbook workbook) throws Exception {

		Cell cell = workbook.getSheetAt(0).getRow(1).getCell(0);
		number_skills 	  = Read_int(cell,workbook.getSheetName(0),1,0,false,-1);
		log += "number_skills     set to "+number_skills+" \n";
		if(number_skills<1){
			throw new Exception("number_skills was set to "+number_skills);
		}

		cell = workbook.getSheetAt(0).getRow(3).getCell(0);
		number_classrooms = Read_int(cell,workbook.getSheetName(0),3,0,false,-1);
		log += "number_classrooms set to "+number_classrooms+" \n";
		if(number_classrooms<1){
			throw new Exception("number_classrooms was set to "+number_classrooms);
		}

		cell = workbook.getSheetAt(0).getRow(5).getCell(0);
		number_volunteers = Read_int(cell,workbook.getSheetName(0),5,0,false,-1);
		log += "number_volunteers set to "+number_volunteers+" \n";
		if(number_volunteers<1){
			throw new Exception("number_volunteers was set to "+number_volunteers);
		}

		log += "\nSkill names are :\n";
		skills = new String [number_skills];
		for (int i = 0; i < number_skills; i++) {
			cell = workbook.getSheetAt(0).getRow(1).getCell(1+i);
			skills[i] = Read_String(cell,workbook.getSheetName(0),1,1+i);
			log += "\t\""+skills[i]+"\"\n";
		}

		for (int i = 0; i < number_skills; i++) {
			for (int j = 0; j < number_skills; j++) {
				if(i!=j){
					if(skills[i].compareTo(skills[j])==0){
						throw new Exception("Two or more skills are named "+skills[i]);
					}
				}
			}
		}
		log += "\n";
	}
	private static void 	Read_class_list			(Workbook workbook) throws Exception {
		classrooms = new Class  [number_classrooms];
		class_names= new String [number_classrooms]; 
		Sheet sheet  = workbook.getSheetAt(1);
		Read_class_heading(sheet,workbook.getSheetName(1),workbook.getSheetName(0));
		log+="\nClasses\n";
		for (int i = 0; i < number_classrooms; i++) {
			Row row = sheet.getRow(1+i);
			class_names[i] = Read_String(row.getCell(class_skill_indexs[number_skills+1]), workbook.getSheetName(1), i, class_skill_indexs[number_skills+1]);
			short max_size = (short)Read_int(row.getCell(class_skill_indexs[number_skills]),workbook.getSheetName(1),i+1,class_skill_indexs[number_skills],false,-1);
			short[] requested_skills = new short [number_skills];
			for (int j = 0; j < number_skills; j++) {
				requested_skills[j] = (short)Read_int(row.getCell(class_skill_indexs[j]),workbook.getSheetName(1),i+1,class_skill_indexs[j],true,0);
			}
			classrooms[i] = new  Class( max_size, requested_skills);
			log += classrooms[i].toString()+"\n";
		}

		for (int i = 0; i < number_classrooms; i++) {
			for (int j = 0; j < number_classrooms; j++) {
				if(i!=j){
					if(class_names[i].compareTo(class_names[j])==0){
						throw new Exception("Two or more classes are named "+skills[i]);
					}
				}
			}
		}
	}
	private static void 	Read_class_heading		(Sheet sheet , String sheet_name ,String sheet2_name) throws Exception {
		Row row = sheet.getRow(0);
		int i = 0;
		while (row.getCell(i)!=null){
			i++;
		}
		class_headings = new String [i];
		for (int j = 0; j < class_headings.length; j++) {
			class_headings [j] = Read_String(row.getCell(j),sheet_name,0,j);
		}

		class_skill_indexs = new int [number_skills+2];
		class_skill_indexs[number_skills+1] = -1;
		for (i = 0; i < class_headings.length; i++) {
			System.out.println(class_headings[i] );
			if(class_headings[i].compareTo("Name")==0){
				class_skill_indexs[number_skills+1] = i;
			}
		}
		if(class_skill_indexs[number_skills+1]==-1){
			throw new Exception(sheet_name+" must have a column named \"Name\"");
		}

		class_skill_indexs[number_skills] = -1;
		for (i = 0; i < class_headings.length; i++) {
			if(class_headings[i].compareTo("Max Size")==0){
				class_skill_indexs[number_skills] = i;
			}
		}
		if(class_skill_indexs[number_skills]==-1){
			throw new Exception(sheet_name+" must have a column named \"Max Size\"");
		}

		for (i = 0; i < number_skills; i++) {
			class_skill_indexs[i] = -1;
			for (int j = 0; j < class_headings.length; j++) {
				if(class_headings[j].compareTo(skills[i])==0){
					class_skill_indexs[i] = j;
				}
			}
			if(class_skill_indexs[i]==-1){
				throw new Exception(sheet_name+" must have a column named \""+skills[i]+"\" to match "+sheet2_name);
			}
		}
	}
	private static void 	Read_volunteer_list		(Workbook workbook) throws Exception {
		volunteers = new Volunteers[number_volunteers];
		Sheet sheet  = workbook.getSheetAt(2);
		log+="\nVolunteers\n";
		Read_volunteer_heading(sheet,workbook.getSheetName(2),workbook.getSheetName(0),workbook.getSheetName(1));
		for (int i = 0; i < number_volunteers; i++) {
			Row row = sheet.getRow(1+i);
			short[] volunteer_skills 		= new short [number_skills];
			short[] volunteer_preferences 	= new short [number_classrooms];
			for (int j = 0; j < volunteer_preferences.length; j++) {
				volunteer_preferences[j] = (short)Read_int(row.getCell(volunteer_class_indexs[j]),workbook.getSheetName(1),i+1,volunteer_class_indexs[j],true,-1);
			}
			for (int j = 0; j < volunteer_skills.length; j++) {
				volunteer_skills[j] = (short)Read_int(row.getCell(volunteer_skill_indexs[j]),workbook.getSheetName(1),i+1,volunteer_skill_indexs[j],true,0);
			}
			volunteers[i]= new Volunteers(volunteer_skills, volunteer_preferences);
			log+=volunteers[i].toString()+"\n";
		}
		log+="\n\n";
	}
	private static void 	Read_volunteer_heading	(Sheet sheet , String sheet_name ,String sheet2_name,String sheet3_name) throws Exception {
		Row row = sheet.getRow(0);
		int i = 0;
		while (row.getCell(i)!=null){
			i++;
		}
		volunteer_headings = new String [i];
		for (int j = 0; j < volunteer_headings.length; j++) {
			volunteer_headings [j] = Read_String(row.getCell(j),sheet_name,0,j);
		}

		volunteer_skill_indexs = new short [number_skills];

		for (i = 0; i < number_skills; i++) {
			volunteer_skill_indexs[i] = -1;
			for (int j = 0; j < volunteer_headings.length; j++) {
				if(volunteer_headings[j].compareTo(skills[i])==0){
					volunteer_skill_indexs[i] = (short)j;
				}
			}
			if(volunteer_skill_indexs[i]==-1){
				throw new Exception(sheet_name+" must have a column named \""+skills[i]+"\" to match "+sheet2_name);
			}
		}

		volunteer_class_indexs = new short [number_classrooms];
		for (i = 0; i < number_classrooms; i++) {
			volunteer_class_indexs[i] = -1;
			for (int j = 0; j < volunteer_headings.length; j++) {
				if(volunteer_headings[j].compareTo(class_names[i])==0){
					volunteer_class_indexs[i] = (short)j;
				}
			}
			if(volunteer_class_indexs[i]==-1){
				throw new Exception(sheet_name+" must have a column named \""+class_names[i]+"\" to match "+sheet3_name);
			}
		}
	}
	@SuppressWarnings("deprecation")
	private static String 	Read_String				(Cell cell, String sheet_name, int row, int column) throws Exception {
		if(cell==null||cell.getCellType()==Cell.CELL_TYPE_BLANK){
			throw new Exception("There is no value in the cell at "+new CellAddress(row,column).formatAsString()+" in sheet"+ sheet_name);
		}
		if(cell.getCellType()==Cell.CELL_TYPE_STRING){
			return cell.getStringCellValue();
		}else if (cell.getCellType()==Cell.CELL_TYPE_BOOLEAN){
			return cell.getBooleanCellValue()+"";
		}else if (cell.getCellType()==Cell.CELL_TYPE_FORMULA){
			return cell.getCellFormula()+"";
		}else{
			throw new Exception("There is no value of a known type in the cell at "+new CellAddress(row,column).formatAsString()+" in sheet"+ sheet_name);
		}
	}
	@SuppressWarnings("deprecation")
	private static String 	Read_String_no_data			(Cell cell) throws Exception {
		if(cell==null||cell.getCellType()==Cell.CELL_TYPE_BLANK){
			return "No data";
		}
		if(cell.getCellType()==Cell.CELL_TYPE_STRING){
			return cell.getStringCellValue();
		}else if (cell.getCellType()==Cell.CELL_TYPE_BOOLEAN){
			return cell.getBooleanCellValue()+"";
		}else if (cell.getCellType()==Cell.CELL_TYPE_FORMULA){
			return cell.getCellFormula()+"";
		}else if (cell.getCellType()==Cell.CELL_TYPE_NUMERIC)										 {
			return ""+cell.getNumericCellValue();
		}else{
			return "No data";
		}
		
	}
	@SuppressWarnings("deprecation")
	private static int 		Read_int				(Cell cell,String sheet_name,int row, int column,boolean replace,int no_data_replacement) throws Exception{
		if(cell==null){
			if(replace){
				return no_data_replacement;
			}
			throw new Exception("There is no value in the cell at "+new CellAddress(row,column).formatAsString()+" in sheet"+ sheet_name);
		}
		if(cell.getCellType()!= Cell.CELL_TYPE_NUMERIC){
			if(cell.getCellType()==Cell.CELL_TYPE_STRING){
				try{  
					return Integer.parseInt(cell.getStringCellValue());  
				}
				catch(NumberFormatException nfe)  
				{  
					throw new Exception("The cell at "+cell.getAddress().formatAsString()+" in sheet"+ sheet_name+" is not a number");
				} 
			}
		}else{
			return (int) cell.getNumericCellValue();
		}
		throw new Exception("The cell at "+cell.getAddress().formatAsString()+" in sheet"+ sheet_name+" is not a number");
	}

	private static Workbook 	Read_file		(String file_path) throws Exception {
		if(file_path.endsWith(".xlsx")){
			return  Read_file_XSSF(file_path);
		}else{
			return  Read_file_HSSF(file_path);
		}
	}
	private static XSSFWorkbook Read_file_XSSF	(String file_path) throws Exception {
		if(!file_path.endsWith(".xlsx")){
			Exception e = new  Exception("File isn't the right type");
			throw e ;
		}

		FileInputStream fileInputStream;
		fileInputStream = new FileInputStream(file_path);
		XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);	

		fileInputStream.close();
		return workbook;
	}
	private static HSSFWorkbook Read_file_HSSF	(String file_path) throws Exception {
		if(!file_path.endsWith(".xls")){
			Exception e = new  Exception("File isn't the right type");
			throw e ;
		}

		FileInputStream fileInputStream;
		fileInputStream = new FileInputStream(file_path);
		HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);	

		fileInputStream.close();
		return workbook;
	}

	/**
	 * A method to determine the demand of all skills present
	 * @param classrooms	An array of classrooms which have demand for skills
	 * @param number_skills	The count of skills present, used to set loop bounds
	 * @return				An integer array representing the demand for each skill (represented by the index) corresponding to the common skill string
	 */
	private static int [] get_demand(Class	[] classrooms, int number_skills ) {
		int [] skill_demand = new int [number_skills];
		for (int i = 0; i < classrooms.length; i++) {
			for (int j = 0; j < number_skills; j++) {
				skill_demand[j] += classrooms[i].getCurrent_skills(j);
				skill_demand[j] -= classrooms[i].getCompromised_skills(j);
				if(skill_demand[j]<0){
					skill_demand[j]=0;
				}
			}
		}
		return skill_demand;
	}
	/**
	 * A method to determine the supply of all skills present across all volunteers
	 * @param volunteers	An array of volunteers each of which may or may not have skills
	 * @param number_skills	The count of skills present, used to set loop bounds
	 * @return				An integer array of skills supplied, in the same index format as the standard skill string
	 */
	private static int [] get_supply(Volunteers	[] volunteers, int number_skills ) {
		int [] skill_supply = new int [number_skills];
		for (int i = 0; i < volunteers.length; i++) {
			for (int j = 0; j < number_skills; j++) {
				if(volunteers[i].getClassid()==-1){
					skill_supply[j] += volunteers[i].getSkill_value(j);
				}
			}
		}
		return skill_supply;
	}
	/**
	 * A method used to remove the skill value of a volunteer from the skill total of a classroom, part of the removal process of a volunteer
	 * @param classroom		The array of classes
	 * @param volunterr_id	The ID of the volunteer to be removed
	 * @param volunteer		The array of volunteers 
	 * @param number_skills	A count of the number of skills in play
	 * @return				Return the new score of the classroom after the volunteer has been removed
	 * TODO: I need a little help understanding what this actually does
	 */
	private static int score_remove(Class classroom[], int volunteer_id ,Volunteers[] volunteer,int number_skills){
		int score = 0 ;
		for (int i = 0; i < number_skills; i++) {
			int d = classroom[volunteer[volunteer_id].getClassid()].getRequested_skills(i);
			int c = classroom[volunteer[volunteer_id].getClassid()].getCurrent_skills(i);
			int r = volunteer[volunteer_id].getSkill_value(i);

			if(!(c-r>=d)){
				score+=d-(c-r);
			}
		}
		return score;
	}
	/**
	 * Another mysterious method used to add a volunteer's skill value to the class's total
	 * @param classroom
	 * @param class_id
	 * @param volunterr_id
	 * @param volunteer
	 * @param number_skills
	 * @return
	 * TODO: Check equality > 0, is >=0 in score_remove method
	 */
	private static int score_add(Class classroom[],int class_id, int volunteer_id ,Volunteers[] volunteer,int number_skills){
		int score = 0 ;
		for (int i = 0; i < number_skills; i++) {
			int d = classroom[class_id].getRequested_skills(i);
			int c = classroom[class_id].getCurrent_skills(i);
			int a = volunteer[volunteer_id].getSkill_value(i);

			if(c+a<=d){
				score+=a;
			}else if (c>=d){

			}else {
				score+=a-(c+a-d);
			}
		}
		return score;
	}
	/**
	 * A method to find the next classroom to allocate to 
	 * @param classroom		An array of classrooms to look at
	 * @param volunteer		The array of volunteers to choose from
	 * @param number_skills	The number of skills in play
	 * @param end			A boolean array of whether a classroom has been totally supplied or not
	 * @return	The integer index of the next classroom to allocate to
	 */
	private static int next_classroom (Class [] classroom ,Volunteers [] volunteer,int number_skills,boolean [] end){
		int max = 0;
		int max_index = 0;
		for (int i = 0; i < classroom.length; i++) {
			int temp = 0 ; 
			for (int j = 0; j < number_skills; j++) {
				temp += classroom[i].getRequested_skills(j) - classroom[i].getCurrent_skills(j)- classroom[i].getCompromised_skills(j);
				if(classroom[i].isfull()||end[i]){
					temp = Integer.MIN_VALUE;
				}
			}
			if(max_index==0 && i==0){
				max = temp ;
				max_index = i;
			}
			if(max<temp) {
				max = temp ;
				max_index = i;
			}
		}
		if(max ==  Integer.MIN_VALUE)
			return -1;
		return max_index;
	}

	private static boolean pair_once(boolean [][] asked , Class [] classroom ,Volunteers [] volunteer,int number_skills,boolean [] end) throws InvalidAlgorithmParameterException {
		int class_id = next_classroom(classroom, volunteer, number_skills,end);
		
		if(class_id!=-1){
			log+= ("Class "+String.format("%-4d",(class_id+1))+"\n");
			PriorityQueue<Integer[]> best_volunteer = new PriorityQueue<Integer[]>(new DescendingQueueComparator());

			for (int i = 0; i < volunteer.length; i++) {
				if(volunteer[i].getPreferences(class_id)!=-1){
					if(volunteer[i].getClassid()!=class_id){
						Integer[] tempscore = new Integer[2];
						tempscore[0]	 = score_add(classroom, class_id, i, volunteer, number_skills);
						tempscore[1]	 = i;
						best_volunteer.add(tempscore);
					}
				}
			}
			log += ("\t"+String.format("%-4d", best_volunteer.size()+1)+" volunteers that could improve the score of this class");
			while (!best_volunteer.isEmpty()){
				Integer[] temp = best_volunteer.poll();
				int volunteer_id = temp [1];
				if(volunteer[volunteer_id].getClassid()==-1){
					log +=("\n\tvolunteer_id "+String.format("%-4d", volunteer_id+1)+" was added to class "+String.format("%-4d",(class_id+1))+ "\n");
					classroom[class_id].assign((short)volunteer_id,volunteer[volunteer_id].getSkill_value());
					volunteer[volunteer_id].setClassid(class_id);
					asked[class_id][volunteer_id]=true;
					return true;
				}else{
					int new_pref = volunteer[volunteer_id].getPreferences(class_id);
					int old_pref = volunteer[volunteer_id].getPreferences(volunteer[volunteer_id].getClassid());
					int add_score = score_add(classroom, class_id,volunteer_id, volunteer, number_skills);
					int rem_score = score_remove(classroom, volunteer_id, volunteer, number_skills);
					if((new_pref<old_pref)&&add_score-rem_score>=0||add_score-rem_score>0){
						log += ("\n\t"+String.format("%-4d", volunteer_id+1)+" was removed from "+String.format("%-4d",volunteer[volunteer_id].getClassid()+1)+" and added to class "+class_id + "\n");
						classroom[volunteer[volunteer_id].getClassid()].remove((short)volunteer_id, volunteer[volunteer_id].getSkill_value());
						classroom[class_id].assign((short)volunteer_id,volunteer[volunteer_id].getSkill_value());
						volunteer[volunteer_id].setClassid(class_id);
						asked[class_id][volunteer_id]=true;
						return true;
					}
				}
				// comment out if too much

			}
			log+=("\n\tNo possible configuration that will fill all classes. Compromises were made to the class "+String.format("%-4d",(class_id+1))+"\n");
			for (int i = 0; i < number_skills; i++) {
				classroom[class_id].setCompromised_skills(i,(short) (classroom[class_id].getRequested_skills(i)-classroom[class_id].getCurrent_skills(i)-classroom[class_id].getCompromised_skills(i)));
				if(classroom[class_id].getCompromised_skills(i)<0){
					classroom[class_id].setCompromised_skills(i,(short) 0);
				}
			}

			end[class_id]=true;
			System.out.println(classroom[class_id].getCompromised_skills());

			return true;
		}else{
			//this means that the pairing is done
			return false;
		}
	}

	public String returnLog() {
		return PairingSystem.log+"\n\n";
	}
	
	//A method that generates an error message in GUI
	public static void errorBox(String errorMessage, String titleBar){
		/* By specifying a null headerMessage String, we cause the dialog to
           not have a header */
		errorBox(errorMessage, titleBar, null);
	}
	
	//A method that generates an error message along with a header message in GUI
	public static void errorBox(String errorMessage, String titleBar, String headerMessage){
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(titleBar);
		alert.setHeaderText(headerMessage);
		alert.setContentText(errorMessage);
		alert.initStyle(StageStyle.UTILITY);
		alert.showAndWait();
	}


	//A method to create the log file according to the date and time if an error was thrown
	public static void printError(Exception e , String log) {
		e.printStackTrace();
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH_mm_ss");
		Calendar calobj = Calendar.getInstance();
		String s = "Ignite_Error_Log "+df.format(calobj.getTime())+".txt";
		File f = new File (s);
		try {
			PrintWriter pw = new PrintWriter(f);
			pw.write(log+"\n\n\n\n");
			e.printStackTrace(pw);
			pw.close();
		} catch (FileNotFoundException ep) {
			ep.printStackTrace();
		}
		System.out.println(f.getPath());
	}
}
/**
 * A simple comparator used in the priority queue. Very standard.
 */
class DescendingQueueComparator implements Comparator<Integer[]>{
	public int compare(Integer[] arg0, Integer[] arg1) {
		return arg1[0] -arg0[0];
	}
}