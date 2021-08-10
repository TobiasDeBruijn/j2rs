package dev.array21.j2rs.parser;

import java.util.LinkedList;
import java.util.List;

public class ParameterParser {
	
	/**
	 * Parse a parameter and 'translate' it to signature-types
	 * @param parameter The parameter
	 * @return The parsed parameter
	 */
	public static Parameter parseParameter(String parameter) {
		String parsed;
		switch(parameter) {
		case "void":
			parsed = "V";
			break;
		case "byte":
			parsed = "B";
			break;
		case "boolean":
			parsed = "Z";
			break;
		case "int":
			parsed = "I";
			break;
		case "float":
			parsed = "F";
			break;
		case "double":
			parsed = "D";
			break;
		case "long":
			parsed = "J";
			break;
		case "short":
			parsed = "S";
			break;
		case "char":
			parsed = "C";
			break;
		default:
			if(!parameter.startsWith("[")) {
				parsed = String.format("L%s;", parameter);
			} else {
				parsed = parameter;
			}
			
			break;
		}
		
		return new Parameter(parsed);
	}
	
	/**
	 * Parse an array of unparsed String parameters into an array of parsed Paramters
	 * @param strParams The unparsed parameters
	 * @return The parsed parameters
	 */
	public static Parameter[] parseParameters(String[] strParams) {
		if(strParams.length == 0) {
			return new Parameter[0];
		}
		
		List<Parameter> params = new LinkedList<>();
		for(int i = 0; i < strParams.length; i++) {
			params.add(parseParameter(strParams[i]));
		}
		
		return params.toArray(new Parameter[0]);
	}
}
