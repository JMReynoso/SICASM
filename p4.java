//N01231512	COP3601

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class p4 {

	public static void main(String[] args) throws IOException{
		
		try {
			
			
			File SicOps=new File("SicOps.dat");
			Scanner scan = new Scanner(SicOps);
			sicTree sicInfo = new sicTree();
			//build sicOps tree
			while(scan.hasNextLine()) {
				
				String line = scan.nextLine();
				String[] split = line.split("\\s+");
				String inst=split[0];
				String opCode=split[1];
				int byteSize=Integer.parseInt(split[2]);

				//if line has inst, opcode, and bytesize, store in tree
				if(inst!=null && opCode!=null && byteSize!=-1) {
					sicInfo.insert(inst, opCode, byteSize);
				}
				//move to next line to ignore last number
			}
			scan.close();
			
		
			registerArray loaderInfo = new registerArray();
			//sicInfo and registerArray should be set
		
//////////////////////////////////////////////sic program/////////////////////////////////////////////////
		
			
			//sic file
			String filename=args[0];
			File file=new File(filename);		//args[0] if want to put file from command line
		
			//read sic file
			Scanner scanFileSize = new Scanner(file);
			//for array size
			int i=0;
			while(scanFileSize.hasNextLine()) {
				++i;
				scanFileSize.nextLine();
			}
			scanFileSize.close();
			
			hashTable theHashedArray = new hashTable(i);
			//i=file size
			
			literalStack literals = new literalStack();
			sicLines[] lines = new sicLines[4000];
			
			
			/*
			current objects: 
			sicInfo					(sif info tree, contains byte and opcode for each inst)
			loaderInfo				(leader info, ex: A, L, B, etc.)
			theHashedArray			(for labels)
			literalsQ 				(for literals)
			siclines - lines 		(for storage of each lines)
			
			*/
			//scan file again for pass 1
			Scanner scan2 = new Scanner(file);
			//execute pass 1
			pass1(lines, theHashedArray, sicInfo, loaderInfo, scan2, literals);
			//execute pass 2
			pass2(lines, theHashedArray, sicInfo, loaderInfo, literals);
			//make lst file
			makeLST(lines, filename);
			System.out.println("File made: " + filename+".lst");
			makeOBJ(lines, filename);
			System.out.println("File made: " + filename+ ".obj");

			
			
			scan2.close();

			
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
	}
	
	public static void makeLST(sicLines[] lines, String filename) throws IOException {
		FileWriter fw = new FileWriter(filename+".lst");
		BufferedWriter writer = new BufferedWriter(fw);
		 
		 
		 writer.write("Addr.\tOC\t\tLabel\tInst\tOperand");
		 writer.newLine();
		 writer.write("--------------------------------------------------------------------------");
		 writer.newLine();
		 for(int y=0; y<lines.length; y++) {
			 
			 if(lines[y]==null) {break;}
			 
				if(lines[y].getOp().equals("!")) {
					writer.write(inHex(lines[y].getAddress()) + "\t" + "" + "\t\t" +lines[y].getLabel() + "\t" + lines[y].getInst() + "\t" + lines[y].getOperand() + "\t");
					writer.newLine();
				}
				else if(lines[y].getInst().charAt(0)=='+') {
					writer.write(inHex(lines[y].getAddress()) + "\t" + lines[y].getOp() + "\t" +lines[y].getLabel() + "\t" + lines[y].getInst() + "\t" + lines[y].getOperand() + "\t");
					writer.newLine();
				}
				else if (lines[y].op.equals("im a failure")) {
					writer.write(inHex(lines[y].getAddress()) + "\t" + "" + "\t\t" +lines[y].getLabel() + "\t" + lines[y].getInst() + "\t" + lines[y].getOperand() + "\t");
					writer.newLine();
					writer.write("\t**Error: address too far in both BASE and PC Relative.**");
					writer.newLine();
				}
				else {
					writer.write(inHex(lines[y].getAddress()) + "\t" + lines[y].getOp() + "\t\t" +lines[y].getLabel() + "\t" + lines[y].getInst() + "\t" + lines[y].getOperand() + "\t");
					writer.newLine();
				}
			 
			 
		 }
		 
		 writer.close();
	}
	
	
	public static void makeOBJ(sicLines[] lines, String filename) throws IOException{
		
		String[] objectCodeArray = new String[lines.length];

		
		FileWriter fw = new FileWriter(filename+".obj");
		BufferedWriter writer = new BufferedWriter(fw);
		
		
		int startAddress=lines[0].getAddress();
		String startAddressInHex=inHex(startAddress);
		int len=startAddressInHex.length();
		String continueString="000000";
		StringBuffer newInitial = new StringBuffer(continueString);
		//new initial
		newInitial.replace(6-len, 6, startAddressInHex);
		
		objectCodeArray[0]=newInitial.toString();
		objectCodeArray[1]=continueString;
		
		//place object code into new array
		int y=2, o=1;
		String nextAddressInHex="";
		while(y<lines.length) {
			
			if(lines[o]==null) {break;}
			
			if(lines[o].getOp().equals("!")) {
				StringBuffer nextAddress = new StringBuffer(continueString);
				if(lines[o+1]!=null) {
					nextAddressInHex=inHex(lines[o+1].getAddress());
				}
				len=startAddressInHex.length();
				nextAddress.replace(6-len, 6, nextAddressInHex);
				
				objectCodeArray[y]="!";
				++y;
				objectCodeArray[y]=nextAddress.toString();
				++y;
				objectCodeArray[y]=continueString;
				++y;
			}
			else if(lines[o].getOp().equals("")) {
				--y;
				++y;
			}
			else {
				objectCodeArray[y]=lines[o].getOp();
				++y;
			}
			
			o++;
		}

		//change last 000000
		for(int s=y-1; s>0; s--) {
			//if last equals 000000, change
			if(objectCodeArray[s].equals(continueString)) {
				objectCodeArray[s]=newInitial.toString();
				break;
			}
		}
		
		//put into new file
		for(int f=0; f<objectCodeArray.length; f++) {		
			if(objectCodeArray[f]==null) {break;}
			
			writer.write(objectCodeArray[f]);
			writer.newLine();
		}
		writer.write("!");
		writer.close();
		
	}
	
	
	public static String inHex(int numInBase10) {
		int addressBase10=numInBase10;
		String addressBase16=Integer.toString(addressBase10, 16);
		
		return addressBase16.toUpperCase();
	}
	
	public static void pass1(sicLines[] lines, hashTable theHashedArray, sicTree sicInfo, registerArray loaderInfo, Scanner scan2, literalStack literals) {
		//first line scan
		int test = lines.length;
		String firstLine=scan2.nextLine();
		String[] firstLineSplit=firstLine.split("\\s+");
		String firstLable = firstLineSplit[0];
		String firstInst = firstLineSplit[1].toUpperCase();
		String firstOp = firstLineSplit[2];
		int j=0; //for siclines
		
		//convert start number to deci
		int currentAddress = Integer.parseInt(firstOp, 16);
		
		//if does not equal start, throw error
		if(!firstInst.equals("START") || firstOp.equals("")) {
			System.out.println("Error: No starting address or first instruction does say START.");
			System.out.println("Exiting program.");
			System.exit(0);
		}
		
		if(!firstLable.equals("")) {
			theHashedArray.insert(firstLable, currentAddress);
		}
		
		//sicLines(int address, String op, String label, String inst, String operand)
		sicLines stuff = new sicLines(currentAddress, "", firstLable, firstInst, firstOp);
		lines[j] = stuff;
		j++;
		//end of first line
		
		System.out.println("");
	
		String line, label, inst, operand;
		
		//scan through each line
		while(scan2.hasNextLine()){
			//get line
			line=scan2.nextLine();
			int len=line.split("\\s+").length;
			//if line is empty
			while(scan2.hasNextLine()) {
				if(line.equals("") || line.isEmpty()|| line.trim().isEmpty() || len<=1 || line.charAt(0)=='.') {
					line=scan2.nextLine();
					len=line.split("\\s+").length;
				}
				else break;
			}
			
			// when line is not empty, split
			String[] splited = line.split("\\s+");

			label = splited[0];								//label
			inst = splited[1].toUpperCase();				//inst
			if(splited.length<=2 || inst.equals("LTORG") || inst.equals("END")) {
				operand = "";						//operand	(convert to int if # is there)
			}
			else {
				operand = splited[2];
			}
			stuff = new sicLines(currentAddress, "", label, inst, operand);
			lines[j] = stuff;
			
			//if inst does not exist
			
			//if instruction is end, build literals and exit
			if(inst.equals("END")) {
				
				//make literal labels and addresses 
				while(!literals.isEmpty()) {
					j++;
					String currentLiteral=literals.getLiteral();
					theHashedArray.insert(currentLiteral, currentAddress);
					stuff = new sicLines(currentAddress, "", currentLiteral, "BYTE", currentLiteral);
					lines[j] = stuff;
					
					String[] split=currentLiteral.split("'");
					String literalData=split[1];
					int num=0, byteSize=0;;
					if(!split[0].equals("=C")) {
						//num address make
						num=Integer.parseInt(literalData,16);
						
						while (num > 0) {
						    num/= 100;
						    byteSize++;
						}
					}
					else {
						//character address make
						for(int h=0; h<=literalData.length(); h++) {
							if((h*2)%2==0) {
								byteSize++;
							}
						}
					}
					
					//fix current address based on byte
					currentAddress+=byteSize;
				
				}
				
				
				//check if label exist in operands
				for(int k=0; k<test; k++) {

					//if reached end of sicLines
					if(lines[k]==null || lines[k].getInst().equals("END")) {break;}

					
					String OperandToCheck=lines[k].getOperand();
					boolean isReg=false;
					if(!OperandToCheck.equals("")) {
						//if #
						if(OperandToCheck.charAt(0)=='#') {
							String[] split=OperandToCheck.split("#");
							String actualOperand=split[1];
							
							//if not a number, check to see if label exist
							if(isNum(actualOperand)==false) {
								if(theHashedArray.search(actualOperand)==null) { labelError(actualOperand);}
							}	
							else	continue;
						}
						//if @
						else if(OperandToCheck.charAt(0)=='@') {
							String[] split=OperandToCheck.split("@");
							String actualOperand=split[1];
							
							//if not a number, check to see if label exist
							if(isNum(actualOperand)==false) {
								if(theHashedArray.search(actualOperand)==null) { labelError(actualOperand);}
							}	
							else	continue;
						}
						//if +
						else if(OperandToCheck.charAt(0)=='+') {
							String[] split=OperandToCheck.split("+");
							String actualOperand=split[1];
							
							//if not a number, check to see if label exist
							if(isNum(actualOperand)==false) {
								if(theHashedArray.search(actualOperand)==null) { labelError(actualOperand);}
							}	
							else	continue;
						}
						//if *
						else if(OperandToCheck.charAt(0)=='*') {
							String[] split=OperandToCheck.split("*");
							String actualOperand=split[1];
							
							//if not a number, check to see if label exist
							if(isNum(actualOperand)==false) {
								if(theHashedArray.search(actualOperand)==null) { labelError(actualOperand);}
							}	
							else	continue;
						}
						
						//if operand is not a number
						else if(isNum(OperandToCheck)==false) {
							//if Y,X format
							boolean hasComma=false;
							for(int s=0; s<OperandToCheck.length(); s++) {
								if(OperandToCheck.charAt(s)==',') {
									hasComma=true;
								}
							}
							
							//if has comma, check to see if R1 is label
							if(hasComma==true) {
								String[] split = OperandToCheck.split(",");
								String R1=split[0];
								String R2=split[1];
								
								if(theHashedArray.search(R1)==null && loaderInfo.getRegister(R1)==null) { labelError(R1);}
								
								else if(theHashedArray.search(R2)==null && loaderInfo.getRegister(R2)==null) {labelError(R2);}
								
							}
							
							//if operand is a register
							else if(loaderInfo.getRegister(OperandToCheck)!=null) {
								isReg=true;
							}
							
							else if(theHashedArray.search(OperandToCheck)==null) { labelError(OperandToCheck);}
							else	continue;
						}
						
	
					}//end operand check
				}
				//go back to main
				return;
			}
			
			//if label is not null, insert label into hash array
			if(!label.equals("")) {
				theHashedArray.insert(label, currentAddress);
			}
			
			//if operand has =, put string into queue 
			if(operand!="" && operand.charAt(0)=='=') {
					literals.insert(operand);
			}
			
			//increment instruction if resw or word
			if(inst.equals("RESW")) {
				int operandInt=Integer.parseInt(operand);
				currentAddress=3*operandInt+currentAddress;
			}
			else if(inst.equals("RESB")) {
				int operandInt=Integer.parseInt(operand);
				currentAddress=operandInt+currentAddress;
			}
			else if(inst.equals("WORD")) {
				currentAddress+=3;
			}
			//if instruction is LTORG, put literal label after it, place into sic lines
			else if(inst.equals("LTORG")) {
				while(!literals.isEmpty()) {
					j++;
					String currentLiteral=literals.getLiteral();
					theHashedArray.insert(currentLiteral, currentAddress);
					stuff = new sicLines(currentAddress, "", currentLiteral, "BYTE", currentLiteral);
					lines[j] = stuff;
					
					String[] split=currentLiteral.split("'");
					String literalData=split[1];
					int num=0, byteSize=0;;
					if(!split[0].equals("=C")) {
						//num address make
						num=Integer.parseInt(literalData,16);
						
						while (num > 0) {
						    num/= 100;
						    byteSize++;
						}
					}
					else {
						//character address make
						for(int h=0; h<=literalData.length(); h++) {
							if((h*2)%2==0) {
								byteSize++;
							}
						}
					}
					
					//fix current address based on byte
					currentAddress+=byteSize;
				
				}
			}
			else if(sicInfo.search(inst)==null) {
				System.out.println("Error: mneumonic [" + inst + "] does not exist. Please check instructions for incorrect spelling. Will increment address by 0.");
				currentAddress+=0;
			}
			
			//if no lable or no resw/word, increment by instruction byte
			else {
			currentAddress+=sicInfo.search(inst).getByteSize();
			}

			j++;	//for sic line
		}//end while
		
		//close and print array
		scan2.close();
		System.out.println();
		theHashedArray.printArray();
	}
	
	public static boolean isNum(String string) {		
		try {  
			Double.parseDouble(string);  
			return true;
		} catch(NumberFormatException e){  
			return false;  
		}
	}
	
	public static void labelError(String label) {
		System.out.println("Error: label [" + label + "] does not exist. Please fix and run again.");
		System.out.println("Exiting program. +");
		System.exit(0);
	}
	
	//pass 2 
	public static void pass2(sicLines[] lines, hashTable theHashedArray, sicTree sicInfo, registerArray loaderInfo, literalStack literals) {
		int baseAddress=-1;
		int currentAddress=0;
		//create opcode for each line
		for(int d=1; d<lines.length; d++) {
			
			if(lines[d]==null) {	break;	}
			
			if(lines[d+1]!=null) {
				currentAddress=lines[d+1].getAddress();
			}

			String inst=lines[d].getInst();
			String operand=lines[d].getOperand();
			
			if(inst.equals("BASE")) {
				baseAddress = theHashedArray.search(operand).getAddress();
			}
			
			String ObjectCode = makeObjectCode(inst, sicInfo, operand, theHashedArray, loaderInfo, currentAddress, baseAddress);
			if(ObjectCode==null) {
				System.out.println("Error: instruction does not exist, object code will not be made.");
			}
			
			lines[d].op=ObjectCode;
			
		}
		
		//make lst file and object file
	}
	
	public static String makeObjectCode(String inst, sicTree sicInfo, String Operand, hashTable theHashedArray, registerArray loaderInfo, int currentAddress, int baseAddress) {

		//if byte, make object code into byte
		if(inst.equals("BYTE")) {
			
			if(Operand==null) {
				System.out.println("ERROR: no operand after BYTE.");
				return null;
			}
			
			String[] split = Operand.split("'");
			
			//if =X, make it into hex
			if(split[0].equals("=X")) {
				return split[1];
			}
			
			else if(split[0].equals("=C")) {
				
				int len = split[1].length();
				String charLit="";
				for(int a=0; a<len; a++) {
					char character = split[1].charAt(a);
					int val = (int) character;
					charLit+=Integer.toString(val);
				}
				
				return charLit;
			}
			
		}
		
		if(inst.equals("RSUB")) {
			return "4F0000";
		}
		
		if(inst.equals("*RSUB")) {
			return "4C0000";
		}
		
		String hexOpcode=sicInfo.search(inst).getopCode();
		String ObjectCode="";
		int byteSize=sicInfo.search(inst).getByteSize();
		
		//if inst does not exist
		if(sicInfo.search(inst)==null) {return null;}
		
		//if inst has no object code
		else if(inst.equals("RESW")) {
			return "!";
		}
		
		//assume that inst exist and has object code
		else {
			int opCodeinDeci=Integer.parseInt(hexOpcode, 16);
		
			//if inst is word
			if(inst.equals("WORD")) {
				String initial = "000000";
				StringBuffer newObjectcode = new StringBuffer(initial);
				
				int deciWord=Integer.parseInt(Operand);
				
				
				if(deciWord>0) {
					String word = inHex(deciWord);
					int len=word.length();
					newObjectcode.replace(6-len, 6, word);
					ObjectCode=newObjectcode.toString();
				}
				//if inst is negative word
				else {
					String word = negHex(deciWord*-1);
					String trimmed = word.substring(2, 8);
					ObjectCode=trimmed.toUpperCase();
				}
			}
			


			//if inst is 2 byte code (opcode + R1 + R2)
			else if(byteSize==2) {
				
				if(inst.equals("CLEAR")) {
					String r1=loaderInfo.getRegister(Operand).getRegisterNum();
					String r2="0";
					ObjectCode=hexOpcode+r1+r2;
				}
				
				else {
					String[] split = Operand.split(",");
					String r1=loaderInfo.getRegister(split[0]).getRegisterNum();
					String r2=loaderInfo.getRegister(split[1]).getRegisterNum();
					ObjectCode=hexOpcode+r1+r2;
				}
			}
			
			//----------------------- disp logic below here
			
			
			//if inst is 4 byte (+)
			else if(byteSize==4) {
				String initial = "00000000";
				StringBuffer newObjectcode = new StringBuffer(initial);
				
				//if operand is * i=1
				if(Operand.charAt(0)=='*') {
					opCodeinDeci+=1;
				}
				//if operand is # i=1
				else if(Operand.charAt(0)=='#') {
					opCodeinDeci+=1;
				}
				//if operand is @ n=1
				else if(Operand.charAt(0)=='@') {
					opCodeinDeci+=2;
				}
				//sic/xe default
				else {
					opCodeinDeci+=3;
				}
				
				String newOpcode=inHex(opCodeinDeci);
				int lenOp=newOpcode.length();
				//put new opcode in newObjectCode
				newObjectcode.replace(2-lenOp, 2, newOpcode);
				
				//put displacement in newObjectCode (if operand is not a number, calculate disp)				
				if(Operand.charAt(0)=='*') {
					String[] split=Operand.split("*");
					String actualOperand=split[1];
					
					//if not a number, calculate label displacement from current address
					if(isNum(actualOperand)==false) {
						//search label and get address
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						String dispInHex=inHex(labelAddress);
						//replace(max-size, max, var)
						int len=dispInHex.length();
						newObjectcode.replace(8-len, 8, dispInHex);
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(actualOperand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(8-len, 8, numInHex);
					}

				}
				
				else if(Operand.charAt(0)=='#') {
					String[] split=Operand.split("#");
					String actualOperand=split[1];
					
					//if not a number, calculate label displacement from current address
					if(isNum(actualOperand)==false) {
						//search label and get address
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						String dispInHex=inHex(labelAddress);
						//replace(max-size, max, var)
						int len=dispInHex.length();
						newObjectcode.replace(8-len, 8, dispInHex);
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(actualOperand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(8-len, 8, numInHex);
					}

				}
				
				else if(Operand.charAt(0)=='@') {
					String[] split=Operand.split("@");
					String actualOperand=split[1];
					
					//if not a number, calculate label displacement from current address
					if(isNum(actualOperand)==false) {
						//search label and get address
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						String dispInHex=inHex(labelAddress);
						//replace(max-size, max, var)
						int len=dispInHex.length();
						newObjectcode.replace(8-len, 8, dispInHex);
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(actualOperand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(8-len, 8, numInHex);
					}

				}
				
				else if(Operand.charAt(0)=='+') {
					String[] split=Operand.split("+");
					String actualOperand=split[1];
					
					//if not a number, calculate label displacement from current address
					if(isNum(actualOperand)==false) {
						//search label and get address
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						String dispInHex=inHex(labelAddress);
						//replace(max-size, max, var)
						int len=dispInHex.length();
						newObjectcode.replace(8-len, 8, dispInHex);
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(actualOperand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(8-len, 8, numInHex);
					}

				}
				
				//if nothing in the beginning 
				else {
					//if not a number, calculate label displacement from current address
					if(isNum(Operand)==false) {
						
						String label=Operand;
						
						//if index
						for(int g=0; g<Operand.length(); g++) {
							if(Operand.charAt(g)==',' && sicInfo.search(inst).getByteSize()>=3) {
								String[] split = Operand.split(",");
								label = split[0];
								
								//search label and get address
								int labelAddress=theHashedArray.search(label).getAddress();
								String dispInHex=inHex(labelAddress);
								//replace(max-size, max, var)
								int len=dispInHex.length();
								newObjectcode.replace(8-len, 8, dispInHex);
								break;
							}
							
							else {label=Operand;}
						}
						//search label and get address
						int labelAddress=theHashedArray.search(label).getAddress();
						String dispInHex=inHex(labelAddress);
						//replace(max-size, max, var)
						int len=dispInHex.length();
						newObjectcode.replace(8-len, 8, dispInHex);
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(Operand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(8-len, 8, numInHex);
					}
				}
				
				int thirdBit=1;
				
				for(int g=0; g<Operand.length(); g++) {
					if(Operand.charAt(g)==',' && sicInfo.search(inst).getByteSize()>=3) {
						thirdBit+=8;
					}
				}
				
				String thirdBitHex=inHex(thirdBit);
				newObjectcode.replace(2, 3, thirdBitHex);
				
				ObjectCode=newObjectcode.toString();
			}

			//default (3 bytes)
			else if(byteSize==3) {
				String initial = "000000";
				StringBuffer newObjectcode = new StringBuffer(initial);
				int disp=0;
				String dispInHex="";
				
				//if operand is * i=1
				if(Operand.charAt(0)=='*') {
					opCodeinDeci+=1;
				}
				//if operand is # i=1
				else if(Operand.charAt(0)=='#') {
					opCodeinDeci+=1;
				}
				//if operand is @ n=1
				else if(Operand.charAt(0)=='@') {
					opCodeinDeci+=2;
				}
				//sic/xe default
				else {
					opCodeinDeci+=3;
				}
				
				//convert new opcode into hex
				String newOpcode=inHex(opCodeinDeci);
				int lenOp=newOpcode.length();
				//put new opcode in newObjectCode
				newObjectcode.replace(2-lenOp, 2, newOpcode);
				
				
				//put displacement in newObjectCode (if operand is not a number, calculate disp)				
				if(Operand.charAt(0)=='*') {
					String[] split=Operand.split("*");
					String actualOperand=split[1];
					
					//if not a number, calculate label displacement from current address
					if(isNum(actualOperand)==false) {
						//search label and get address
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						disp=labelAddress-currentAddress;
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(actualOperand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(6-len, 6, numInHex);
					}

				}
				
				else if(Operand.charAt(0)=='#') {
					String[] split=Operand.split("#");
					String actualOperand=split[1];
					
					//if not a number, calculate label displacement from current address
					if(isNum(actualOperand)==false) {
						//search label and get address
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						disp=labelAddress-currentAddress;
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(actualOperand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(6-len, 6, numInHex);
					}

				}
				
				else if(Operand.charAt(0)=='@') {
					String[] split=Operand.split("@");
					String actualOperand=split[1];
					
					//if not a number, calculate label displacement from current address
					if(isNum(actualOperand)==false) {
						//search label and get address
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						disp=labelAddress-currentAddress;
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(actualOperand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(6-len, 6, numInHex);
					}

				}
				
				else if(Operand.charAt(0)=='+') {
					String[] split=Operand.split("+");
					String actualOperand=split[1];
					
					//if not a number, calculate label displacement from current address
					if(isNum(actualOperand)==false) {
						//search label and get address
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						disp=labelAddress-currentAddress;
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(actualOperand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(6-len, 6, numInHex);
					}

				}
				
				else {
					//if not a number, calculate label displacement from current address
					if(isNum(Operand)==false) {
						String label=Operand;
						
						//if index
						for(int g=0; g<Operand.length(); g++) {
							if(Operand.charAt(g)==',' && sicInfo.search(inst).getByteSize()>=3) {
								String[] split = Operand.split(",");
								label = split[0];
								
								//search label and get address
								int labelAddress=theHashedArray.search(label).getAddress();
								dispInHex=inHex(labelAddress);
								//replace(max-size, max, var)
								int len=dispInHex.length();
								newObjectcode.replace(6-len, 6, dispInHex);
								break;
							}
						}
						//search label and get address
						int labelAddress=theHashedArray.search(label).getAddress();
						disp=labelAddress-currentAddress;
						
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp*-1);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}
					}
					
					else {
						//convert number to hex
						int num=Integer.parseInt(Operand);
						String numInHex=inHex(num);
						//place number at end
						int len=numInHex.length();
						newObjectcode.replace(6-len, 6, numInHex);
					}
				}
				
				
				//if disp is too far, try base
				int thirdBit=0;

				if(disp>2047) {

					thirdBit+=4;
										
					if(Operand.charAt(0)=='*') {
						String[] split=Operand.split("*");
						String actualOperand=split[1];
						

						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						disp=labelAddress-baseAddress;
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp*-1);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}
					}
					
					else if(Operand.charAt(0)=='#') {
						String[] split=Operand.split("#");
						String actualOperand=split[1];
						
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						disp=labelAddress-baseAddress;
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp*-1);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}
					}

					else if(Operand.charAt(0)=='@') {
						String[] split=Operand.split("@");
						String actualOperand=split[1];
						

							//search label and get address
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						disp=labelAddress-baseAddress;
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp*-1);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}
					}

					else if(Operand.charAt(0)=='+') {
						String[] split=Operand.split("+");
						String actualOperand=split[1];
						
						
						int labelAddress=theHashedArray.search(actualOperand).getAddress();
						disp=labelAddress-baseAddress;
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp*-1);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}
					}
					
					else {
						//convert number to hex
						int labelAddress=theHashedArray.search(Operand).getAddress();
						disp=labelAddress-baseAddress;
						if(disp>=0) {
							dispInHex=inHex(disp);
							int len=dispInHex.length();
							newObjectcode.replace(6-len, 6, dispInHex);
						}
						else {
							dispInHex=negHex(disp*-1);
							String last3=dispInHex.substring(dispInHex.length()-3);
							newObjectcode.replace(6-3, 6, last3);
						}

					}
					
					//if disp ends up being more than 4095, return error
					if(disp>4095) {
						return "im a failure";
					}
				}
				
				//only if number
				else if(Operand.charAt(0)=='*') {
					String[] split=Operand.split("*");
					String actualOperand=split[1];
					if(isNum(actualOperand)==true) {thirdBit+=0;}
					else {thirdBit+=2;}
				}
				
				else if(Operand.charAt(0)=='#') {
					String[] split=Operand.split("#");
					String actualOperand=split[1];
					if(isNum(actualOperand)==true) {thirdBit+=0;}else {thirdBit+=2;}
				}
				
				else if(Operand.charAt(0)=='@') {
					String[] split=Operand.split("@");
					String actualOperand=split[1];
					if(isNum(actualOperand)==true) {thirdBit+=0;}else {thirdBit+=2;}
				}
				
				//PC relative (default)
				else {thirdBit+=2;}
				
				//if index is enabled, +=8
				for(int g=0; g<Operand.length(); g++) {
					if(Operand.charAt(g)==',' && sicInfo.search(inst).getByteSize()>=3) {
						thirdBit+=8;
					}
				}
				
				String thirdBitHex=inHex(thirdBit);
				newObjectcode.replace(2, 3, thirdBitHex);
				
				
				
				ObjectCode=newObjectcode.toString();

			}//end byte 3 check
		}//end ints check 		
		return ObjectCode.toUpperCase();
	}
	
	public static String negHex(int deci) {
		
		byte ones_comp = (byte)(deci ^ 0xFFFF);
		int twos_comp = ++ones_comp;
		String hex = Integer.toHexString(twos_comp);
		return hex;
		
	}
}

/////////////////////////////////////////////HASH DATA////////////////////////////////////////////

class data{
	
	private String lable;
	private int address;
	public boolean base=false;
	
	
	public data(String lable, int address){
		this.lable=lable;
		this.address=address;
	}
	
	public void setBaseTrue() {
		base=true;
	}
	
	public boolean isItBase() {
		return base;
	}
	
	public String getLable() {
		return lable;
	}
	
	public int getAddress() {
		return address;
	}
}


class hashTable{
	
	private data[] hashArray;
	private int hashArraySize;

	
	public hashTable(int arraySize){
		hashArraySize=getPrime(arraySize);
		hashArray = new data[hashArraySize];
	}
	
	public void insert(String lable, int address) {
		
		//hash key
		int index=hashIndex(lable);
		
		if(search(lable)==null) {
			
			//make new data object
			data stuff = new data(lable, address);
			
			//move in case of collision 
			while(hashArray[index]!=null) {
				++index;
				index%=hashArraySize;
			}
	
			//insert
			hashArray[index]=stuff;
		}
		

	}
	
	//for hash array size
	private int getPrime(int min) {
		for(int j = min+1; true; j++) 
			if( isPrime(j) ) 
				return j; 
	}

	
	private boolean isPrime(int n){
		for(int j=2; (j*j <= n); j++) 
			if( n % j == 0) 
				return false; 
		return true; 
	}
	
	
	public data search(String word) {
		int index=hashIndex(word);

		//hash search
		while(hashArray[index]!=null) {

			//if found, return.
			if(hashArray[index].getLable().equals(word)) {
				return hashArray[index];
			}
			
			//if not, move
			++index;
			index%=hashArraySize;
		}
		
		//can't find
		return null;
	}

	
	public int hashIndex(String word) {
		
		//convert string to ascii number
		int key=0, i;
		for (i=0; i<word.length(); i++) {
			key+=word.charAt(i);
		}
		
		//return hash number

		return key%hashArraySize;
	}
	
	public void printArray() {
	
		System.out.println("[Index, label, address]");
		for(int i=0; i<hashArraySize; i++) {

			if(hashArray[i]!=null) {
				
				
				int addressBase10=hashArray[i].getAddress();
				String addressBase16=Integer.toString(addressBase10, 16);
				
				
				System.out.println("[" +i+", "+hashArray[i].getLable()+", "+addressBase16.toUpperCase()+"]");
			}
			
		}
		
	}

}
/////////////////////////////////////////////HASH DATA////////////////////////////////////////////



/////////////////////////////////////////////SICOPS / REG  DATA////////////////////////////////////////////
class sicData{
	private String inst, opCode;
	private int byteSize;
	public sicData left, right;
	
	public sicData(String inst, String opCode, int byteSize) {
		this.inst=inst;
		this.opCode=opCode;
		this.byteSize=byteSize;
		left=null;
		right=null;
	}
	
	public String getInst() {
		return inst;
	}
	
	public String getopCode() {
		return opCode;
	}
	
	public int getByteSize() {
		return byteSize;
	}
	
	public void display() {
		System.out.println("[" + inst +", " + opCode + ", " + byteSize + "]");
	}
}


//turn into bi search tree
class sicTree{
	private sicData root;
	private sicData current;
	
	public sicTree() {
		root=null;
	}
	
	public void insert(String inst, String opCode, int byteSize) {
		//bi search insert
		sicData newData = new sicData(inst, opCode, byteSize);
		
		//if no root, use first insert as root
		if(root==null) {
			root=newData;
		}
		
		
		else {
			current = root;
			sicData parent;
			
			while(true) {
				parent=current;
				
				//go left
				if(inst.compareTo(current.getInst())<0) {
					current=current.left;
					if(current==null) {
						parent.left=newData;
						return;
					}
				}
				
				//go right
				else {
					current=current.right;
					if(current==null) {
						parent.right=newData;
						return;
					}
				}
			}
		}
	}//end insert
	
	//search(inst).getOpcode or search(inst).getByteSize
	public sicData search(String inst) {
		sicData current = root;
		
		while(!current.getInst().equals(inst)) {
			
			//if inst<0, go left
			if(inst.compareTo(current.getInst())<0) {
				current=current.left;
			}
			
			else{
				current=current.right;
			}
			
			if(current==null) {
				return null;
			}
		}
		
		return current;
	}
}

class registers{
	private String register, registerNum;
	
	public registers(String register, String registerNum) {
		this.register=register;
		this.registerNum=registerNum;
	}
	
	public String getRegister() {
		return register;
	}
	
	public String getRegisterNum() {
		return registerNum;
	}
	
}

class registerArray{
	private registers[] registerArray;
	private int max = 9;
	
	public registerArray() {
		registerArray = new registers[max];
		
		registerArray[0] = new registers("A","0");
		registerArray[1] = new registers("X","1");
		registerArray[2] = new registers("L","2");
		registerArray[3] = new registers("B","3");
		registerArray[4] = new registers("S","4");
		registerArray[5] = new registers("T","5");
		registerArray[6] = new registers("F","6");
		registerArray[7] = new registers("PC","8");
		registerArray[8] = new registers("SW","9");
	}
	
	//returns loader, so getLoader(find).getLoaderNum();
	public registers getRegister(String find) {
		
		for(int i=0; i<max; i++) {
			if(registerArray[i].getRegister().equals(find)) {return registerArray[i];}
		}
		
		return null;
	}
	
}
/////////////////////////////////////////////SICOPS / REG DATA////////////////////////////////////////////


////////////////////////////////////////////LITERALS/////////////////////////////////////////////////

class literalStack{
	
	private int maxSize=900, top;
	private String[] literalStack;
	
	
	public literalStack(){
		literalStack = new String[maxSize];
		top=-1;
	}
	
	public void insert(String literal) {
		literalStack[++top]=literal;
	}
	
	
	public String getLiteral() {
		return literalStack[top--];
	}
	
	public boolean isEmpty(){
		return (top==-1);
	}
}

///////////////////////////////////////////////////////////////////////////////////////

class sicLines{
	
	private String label, inst, operand; 
	private int address;
	public String op;
	
	sicLines(int address, String op, String label, String inst, String operand){
		this.address = address;
		this.op = op;
		this.label = label;
		this.inst = inst;
		this.operand=operand;
	}
	
	public int getAddress() {
		return address;
	}
	
	public String getOp(){
		return op;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getInst() {
		return inst;
	}
	
	public String getOperand() {
		return operand;
	}
}
