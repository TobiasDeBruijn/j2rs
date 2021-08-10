package dev.array21.j2rs.parser;

public class SignatureBuilder {

	/**
	 * Build a Java function signature
	 * @param retType The return type
	 * @param params The method parameters
	 * @return The signature
	 */
	public static Signature buildSignature(Parameter retType, Parameter[] params) {
		StringBuilder sb = new StringBuilder();
		for(Parameter param : params) {
			sb.append(param.ty());
		}
		
		String sig = String.format("(%s)%s", sb.toString(), retType.ty());
		return new Signature(sig);
	}
}
