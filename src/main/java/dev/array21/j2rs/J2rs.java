package dev.array21.j2rs;

import java.io.File;
import java.io.IOException;

import dev.array21.j2rs.exceptions.NoArgumentException;
import dev.array21.j2rs.exceptions.NoArgumentValueException;
import dev.array21.j2rs.parser.ClassDescriptor;
import dev.array21.j2rs.parser.FileParser;
import dev.array21.j2rs.parser.NameProcessor;
import dev.array21.j2rs.parser.UnparsedMethodDescriptor;
import dev.array21.j2rs.parser.Parameter;
import dev.array21.j2rs.parser.ParameterParser;
import dev.array21.j2rs.parser.Signature;
import dev.array21.j2rs.parser.SignatureBuilder;

public class J2rs {
	
	private static boolean IS_DEBUG = false;
	
	public static void main(String[] args) {
		String sourceDirStr = null;
		
		if(args.length == 0) {
			throw new NoArgumentException("J2rs requires the argument '--source'");
		}
		
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch(arg) {
			case "--source":
				if(i+1 == args.length) {
					throw new NoArgumentValueException("Argument '--source' requires a value.");
				}
				
				sourceDirStr = args[++i];
				break;
			case "--verbose":
			case "--debug":
				IS_DEBUG = true;
				break;
			default:
				throw new IllegalArgumentException(String.format("Unknown argument '%s'", arg));
			}
		}
		
		if(sourceDirStr == null) {
			throw new NoArgumentException("Missing required argument '--source'");
		}
		
		File f = new File(sourceDirStr);
		if(!f.exists()) {
			throw new IllegalArgumentException(String.format("Source directory '%s' does not exist.", sourceDirStr));
		}
		
		if(f.isFile()) {
			Pair<ClassDescriptor, UnparsedMethodDescriptor[]> parsed;
			try {
				parsed = FileParser.parseFile(f);
			} catch(IOException e) {
				e.printStackTrace();
				return;
			}
			
			System.out.println(String.format("Method & Signatures in %s.%s:", parsed.a().packageName(), parsed.a().className()));
			for(UnparsedMethodDescriptor mCall : parsed.b()) {
				Parameter[] params = ParameterParser.parseParameters(mCall.parameters());
				Parameter retType = ParameterParser.parseParameter(mCall.returnType());
				
				Signature sig = SignatureBuilder.buildSignature(retType, params);
				System.out.println(String.format("%s: %s", NameProcessor.toSnakeCase(mCall.name()), sig.sig()));
			}
		}
	}
	
	public static void debug(Object o) {
		if(!IS_DEBUG) {
			return;
		}
		
		System.out.println(String.format("[DEBUG] %s", o.toString()));
	}
}
