package dev.array21.j2rs.parser;

import java.util.regex.Pattern;

public enum ReturnType {
	VOID,
	OBJECT,
	BOOLEAN,
	BYTE,
	INTEGER,
	LONG,
	FLOAT,
	DOUBLE,
	SHORT,
	CHAR,
	
	OBJECT_ARRAY,
	BOOLEAN_ARRAY,
	BYTE_ARRAY,
	INTEGER_ARRAY,
	LONG_ARRAY,
	FLOAT_ARRAY,
	DOUBLE_ARRAY,
	SHORT_ARRAY,
	CHAR_ARRAY;
	
	public static ReturnType fromString(String sig) {
		String[] sigParts = sig.split(Pattern.quote("("));
		String retSig = sigParts[1];
		switch(retSig) {
		case "V":
			return VOID;
		case "Z":
			return BOOLEAN;
		case "B":
			return BYTE;
		case "I":
			return INTEGER;
		case "J":
			return LONG;
		case "F":
			return FLOAT;
		case "D":
			return DOUBLE;
		case "S":
			return SHORT;
		case "C":
			return CHAR;
		case "[Z":
			return BOOLEAN_ARRAY;
		case "[B":
			return BYTE_ARRAY;
		case "[I":
			return INTEGER_ARRAY;
		case "[J":
			return LONG_ARRAY;
		case "[F":
			return FLOAT_ARRAY;
		case "[D":
			return DOUBLE_ARRAY;
		case "[S":
			return SHORT_ARRAY;
		case "[C":
			return CHAR_ARRAY;
		default:
			if(retSig.startsWith("[")) {
				return OBJECT_ARRAY;
			} else {
				return OBJECT;
			}
		}
	}
}
