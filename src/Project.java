import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Project {
	static int clck;
	static int PC = 0;
	static int PCpipeline = 0;
	static int PCpipeline1 = 0;
	static int[] Memory = new int[2048];
	static int[] registerFile = new int[32];
	static int tempReg = 0;
	static int tempRegPipline = 0;
	static int instructionCount = 2;
	static int instruction = 0;
	static int opcode = 0;
	static int opcodePipline = 0;
	static int opcodePipline2 = 0;
	static int r1 = 0;
	static int r1Pipline = 0;
	static int r1Pipline1 = 0;
	static int r2 = 0;
	static int r3 = 0;
	static int shamt = 0;
	static int shamtPipeline = 0;
	static int immediate = 0;
	static int immediatePipline = 0;
	static int address = 0;
	static int addressPipeline = 0;
	static int readReg1 = 0;
	static int readReg2 = 0;
	static int readReg3 = 0;
	static int readReg1Pipline = 0;
	static int readReg2Pipline = 0;
	static int readReg3Pipline = 0;
	static int fetchCount = 0;
	static int decodeCount = 0;
	static int executeCount = 0;
	static int memoryCount = 0;
	static int writeCount = 0;
	static boolean decodeFlag = false;
	static boolean executeFlag = false;
	static boolean jumpFlag = false;
	static int blockDecode = 0;
	static int blockExecute = 0;
	static int blockMemory = 0;
	static int blockWrite = 0;
	static int finish = 0;
	static int tempPC = 0;
	static int jumpInt = 0;

	public static void pipeline() {
		for (int i = 1; finish < 7; i++) {
			if (PC < instructionCount )
				finish = 1;
			if (PC >= instructionCount ) {
				finish++;
			}
			System.out.println("Cycle No. " + i);
			// writeBack
			if (i >= 7 && i % 2 != 0 && (blockWrite <= 0 || blockWrite == 6) && finish <= 7) {
				writeBack(); // odd cycle and passed the 7th cycle so we write back the last function in MEM
				if (jumpFlag && 0 == jumpInt--) { // we make sure that last excuted function needs to jump
					jumpFlag = false;
				}
			}
			blockWrite--;
			// memory
			if (i >= 6 && i % 2 == 0 && blockMemory <= 0 && finish <= 6) {
				if (jumpFlag) {
					PC = tempPC;
					blockDecode = 1;
					blockExecute = 3;
					blockMemory = 6;
					blockWrite = 6;
				}
				memory();
			}
			blockMemory--;
			if (executeFlag) {
				System.out.println("Instruction " + executeCount + " is at the execute stage." + "\nINPUT: opcode = "
						+ opcodePipline + " PC = " + PCpipeline1 + " shamt= " + shamtPipeline + " immediate = "
						+ immediatePipline + " address = " + addressPipeline + "\nData in r1 = " + readReg1Pipline
						+ " Data in r2 = " + readReg2Pipline + " Data in r3 = " + readReg3Pipline);
				if (opcodePipline == 4 || opcodePipline == 7)
					System.out.println("OUTPUT: PC = " + tempPC + "\n");
				else if (opcodePipline != 10 && opcodePipline != 11) {
					System.out.println("OUTPUT: ALU Result = " + tempReg + "\n");
				} else {
					System.out.println();
				}
				executeFlag = false;
			}
			// execute
			if (i >= 4 && i % 2 == 0 && blockExecute <= 0 && finish <= 4) {
				execute();
				System.out.println("Instruction " + executeCount + " is at the execute stage." + "\nINPUT: opcode = "
						+ opcode + " PC = " + PCpipeline1 + " shamt= " + shamtPipeline + " immediate = "
						+ immediatePipline + " address = " + addressPipeline + "\nData in r1 = " + readReg1
						+ " Data in r2 = " + readReg2 + " Data in r3 = " + readReg3Pipline);
				if (opcode == 4 || opcode == 7)
					System.out.println("OUTPUT: PC = " + tempPC + "\n");
				else if (opcode != 10 && opcode != 11) {
					System.out.println("OUTPUT: ALU Result = " + tempReg + "\n");
				} else {
					System.out.println();
				}
				executeFlag = true;
			}
			blockExecute--;
			// decode
			if (decodeFlag) {
				System.out.println("Instruction " + decodeCount + " is at the decode stage.\nINPUT: instruction = "
						+ Integer.toBinaryString(instruction) + "\nOUTPUT: opcode = " + opcode + " r1 = " + r1
						+ " r2 = " + r2 + " r3 = " + r3 + " shamt= " + shamt + " immediate = " + immediate
						+ " address = " + address + "\nData in r1 = " + readReg1 + " Data in r2 = " + readReg2
						+ " Data in r3 = " + readReg3 + "\n");
				decodeFlag = false;
			}
			if (i % 2 == 0 && blockDecode <= 0 && finish <= 2) {
				decode();
				System.out.println("Instruction " + decodeCount + " is at the decode stage.\nINPUT: instruction = "
						+ Integer.toBinaryString(instruction) + "\nOUTPUT: opcode = " + opcode + " r1 = " + r1
						+ " r2 = " + r2 + " r3 = " + r3 + " shamt= " + shamt + " immediate = " + immediate
						+ " address = " + address + "\nData in r1 = " + readReg1 + " Data in r2 = " + readReg2
						+ " Data in r3 = " + readReg3 + "\n");
				decodeFlag = true;
			}
			blockDecode--;
			// fetch
			if (i % 2 != 0 && PC <= instructionCount - 1) {

				System.out.println("Instruction " + (decodeCount+1) + " is at the fetch stage.");
				fetch();
			}
			System.out.println("\n\n");

		}
		System.out.println("register files");
		for(int i = 0; i<registerFile.length;i++) {
			System.out.println("register number" + (i) +"="+ registerFile[i]);	
		}
		System.out.println("\n\n");
	}

	public static void parser(String nameProgram) throws IOException {

		String path = "src/" + nameProgram + ".txt";
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String[] s;
		instructionCount = (int) br.lines().count();
		br.close();
        br = new BufferedReader(new FileReader(file));
		String str;
		int index = 0;

		while ((str = br.readLine()) != null) {
			s = str.split(" ");
			String binaryInstruction = "";
			switch (s[0]) {
			case "ADD":
				binaryInstruction = "0000";
				break;
			case "SUB":
				binaryInstruction = "0001";
				break;
			case "MUL":
				binaryInstruction = "0010";
				break;
			case "MOVI":
				binaryInstruction = "0011";
				break;
			case "JEQ":
				binaryInstruction = "0100";
				break;
			case "AND":
				binaryInstruction = "0101";
				break;
			case "XORI":
				binaryInstruction = "0110";
				break;
			case "JMP":
				binaryInstruction = "0111";
				break;
			case "LSL":
				binaryInstruction = "1000";
				break;
			case "LSR":
				binaryInstruction = "1001";
				break;
			case "MOVR":
				binaryInstruction = "1010";
				break;
			case "MOVM":
				binaryInstruction = "1011";
				break;
			}
			int R1;
			int R2;
			int R3;
			int SHAMT;
			int ADDRESS;
			int IMM;

			// either R FORMAT or I format
			if (!s[0].equals("JMP")) {
				R1 = Integer.parseInt(s[1].replace("R", ""));
				for (int i = Integer.toBinaryString(R1).length(); i < 5; i++) {
					binaryInstruction += "0";
				}
				binaryInstruction += Integer.toBinaryString(R1);
				

				// r type instructions assign r3
				if (s[0].equals("ADD") || s[0].equals("SUB") || s[0].equals("MUL") || s[0].equals("AND")) {
					R2 = Integer.parseInt(s[2].replace("R", ""));
					for (int i = Integer.toBinaryString(R2).length(); i < 5; i++) {
						binaryInstruction += "0";
					}
					binaryInstruction += Integer.toBinaryString(R2);
					R3 = Integer.parseInt(s[3].replace("R", ""));
					for (int i = Integer.toBinaryString(R3).length(); i < 5; i++) {
						binaryInstruction += "0";
					}
					binaryInstruction += Integer.toBinaryString(R3);
					binaryInstruction += "0000000000000";
				}

				// for lsl and lsr the r3 value will be 0
				else if (s[0].equals("LSL") || s[0].equals("LSR")) {
					R2 = Integer.parseInt(s[2].replace("R", ""));
					for (int i = Integer.toBinaryString(R2).length(); i < 5; i++) {
						binaryInstruction += "0";
					}
					binaryInstruction += Integer.toBinaryString(R2);
					binaryInstruction += "00000";
					SHAMT = Integer.parseInt(s[3]);
					for (int i = Integer.toBinaryString(SHAMT).length(); i < 13; i++) {
						binaryInstruction += "0";
					}
					binaryInstruction += Integer.toBinaryString(SHAMT);
				}

				// i type instructions
				else {
					if(!s[0].equals("MOVI")) {
						R2 = Integer.parseInt(s[2].replace("R", ""));
						for (int i = Integer.toBinaryString(R2).length(); i < 5; i++) {
							binaryInstruction += "0";
						}
						binaryInstruction += Integer.toBinaryString(R2);
					IMM = Integer.parseInt(s[3]);
					if (IMM >= 0) {
						for (int i = Integer.toBinaryString(IMM).length(); i < 18; i++) {
							binaryInstruction += "0";
						}
						binaryInstruction += Integer.toBinaryString(IMM);
					} else
						binaryInstruction += Integer.toBinaryString(IMM).substring(14);
				}else {
					IMM = Integer.parseInt(s[2]);
					
					for (int i = 0; i < 5; i++) {
						binaryInstruction += "0";
					}
					
					if (IMM >= 0) {
						for (int i = Integer.toBinaryString(IMM).length(); i < 18; i++) {
							binaryInstruction += "0";
						}
						binaryInstruction += Integer.toBinaryString(IMM);
					} else
						binaryInstruction += Integer.toBinaryString(IMM).substring(14);
				}
			}
			}
			// jump instruction J FORMAT
			else {
				ADDRESS = Integer.parseInt(s[1]);
				for (int i = Integer.toBinaryString(ADDRESS).length(); i < 28; i++) {
					binaryInstruction += "0";
				}
				binaryInstruction += Integer.toBinaryString(ADDRESS);
			}

			Memory[index++] = (int) Long.parseLong(binaryInstruction, 2);
			
		}

	}

	public static void decode() {
		opcode = (instruction & 0b11110000000000000000000000000000) >>> 28;
		r1 = (instruction & 0b00001111100000000000000000000000) >>> 23;
		r2 = (instruction & 0b00000000011111000000000000000000) >>> 18;
		r3 = (instruction & 0b00000000000000111110000000000000) >>> 13;
		shamt = instruction & 0b00000000000000000001111111111111;
		immediate = (instruction & 0b00000000000000111111111111111111);
		address = instruction & 0b00001111111111111111111111111111;
		readReg1 = registerFile[r1];
		readReg2 = registerFile[r2];
		readReg3 = registerFile[r3];
		decodeCount = fetchCount;
		 int immediateTemp = immediate;
         immediateTemp = immediateTemp &0b00000000000000100000000000000000;
         immediateTemp = immediateTemp >>> 17;
        if(immediateTemp == 1){
            immediate = immediate | 0b11111111111111000000000000000000;
        }else{
            immediate = immediate & 0b00000000000000111111111111111111;
        }
//		PCpipeline = PC;
//		PC++;
	}

	public static void execute() {
		switch (opcode) {
		case 0:
			// add
			tempReg = readReg2 + readReg3;
			break;
		case 1:
			// subtract
			tempReg = readReg2 - readReg3;
			break;
		case 2:
			// multiply immediate
			tempReg = readReg2 * readReg3;
			break;
		case 3:
			// move immediate
			tempReg = immediate;
			break;
		case 4:
			// jump if not equal
			if (readReg1 == readReg2) {
				tempPC = PCpipeline + 1 + immediate;
				jumpFlag = true;
				jumpInt = 1;
			}
			break;
		case 5:
			// and
			tempReg = readReg2 & readReg3;
			break;
		case 6:
			// xor immediate
			tempReg = readReg2 ^ immediate;
			break;
		case 7:
			// jump
			int newadd = PC & 0b11110000000000000000000000000000;
			newadd = newadd | address;
			tempPC = newadd;
			jumpFlag = true;
			jumpInt = 1;
			break;
		case 8:
			// shift left logical
			tempReg = readReg2 << shamt;
			break;
		case 9:
			// shift right logical
			tempReg = readReg2 >>> shamt;
			break;
		case 10:
			// load
			tempReg = readReg2 + immediate;
			break;
		case 11:
			// store
			tempReg = readReg2 + immediate;
			break;
		}
		readReg1Pipline = readReg1;
		readReg2Pipline = readReg2;
		readReg3Pipline = readReg3;
		r1Pipline = r1;
		opcodePipline = opcode;
		shamtPipeline = shamt;
		addressPipeline = address;
		executeCount = decodeCount;
		PCpipeline1 = PCpipeline;
		immediatePipline = immediate;
	}

	public static void fetch() {
		fetchCount = PC + 1;
		instruction = Memory[PC];
		PCpipeline = PC;
		PC++;
	}

	public static void memory() {
		switch (opcodePipline) {
		case 10:
			// load
			tempReg = Memory[tempReg];
			break;
		case 11:
			// store
			Memory[tempReg] = readReg1Pipline;
			break;

		}
		tempRegPipline = tempReg;
		opcodePipline2 = opcodePipline;
		r1Pipline1 = r1Pipline;
		memoryCount = executeCount;
		System.out.println("Instruction " + memoryCount + " is at the memory stage." + "\nINPUT: opcode = "
				+ opcodePipline + " immediate = " + immediatePipline + "\nData in r1 = " + readReg1Pipline
				+ " Data in r2 = " + readReg2Pipline);
		if (opcodePipline == 10) {
			System.out.println("OUTPUT: Write Back Value = " + tempRegPipline);
		}
		if (opcodePipline == 11) {
			System.out.println("Memory Position " + (readReg2Pipline + immediatePipline) + " was modified to contain "
					+ readReg1Pipline + " at the memory stage");
		}
		System.out.println();

	}

	public static void writeBack() {
		System.out.println("Instruction " + memoryCount + " is at the write back stage.");
		if (r1Pipline1 != 0)
			switch (opcodePipline2) {
			case 0, 1, 2, 3, 5, 6, 8, 9, 10:
				registerFile[r1Pipline1] = tempRegPipline;
				System.out.println(
						"INPUT: Destination Register = " + r1Pipline1 + " Write Back Value = " + tempRegPipline);
				System.out.println("Register R" + r1Pipline1 + " was modified to contain " + tempRegPipline
						+ " at the write back stage.");
			}
		System.out.println();
	}

	public static void main(String[] args) throws IOException {
		parser("test");
		pipeline();		
	}
}
