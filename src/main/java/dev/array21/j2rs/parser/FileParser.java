package dev.array21.j2rs.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import dev.array21.j2rs.Pair;

public class FileParser {
	
	/**
	 * Parse a File for it's ClassDescriptor and all MethodDescriptors in it
	 * 
	 * @param f The file to parse
	 * @return A Pair containing the parsed ClassDescriptor and an array of UnparsedMethodDescriptor
	 */
	public static Pair<ClassDescriptor, UnparsedMethodDescriptor[]> parseFile(File f) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(f));
			
		AtomicReference<String> packageName = new AtomicReference<>();
		AtomicReference<String> className = new AtomicReference<>();
		
		AtomicBoolean inComment = new AtomicBoolean(false);
		
		br.lines().forEach(line -> {
			String trimmedLine = line.trim();
			if(trimmedLine.startsWith("//")) {
				return;
			}
			
			if(trimmedLine.startsWith("/*") || trimmedLine.startsWith("/**")) {
				inComment.set(true);
				return;
			}
			
			if(trimmedLine.startsWith("*/")) {
				inComment.set(false);
				return;
			}
			
			if(inComment.get()) {
				return;
			}
						
			if(trimmedLine.startsWith("package") && className.get() == null) {
				String[] parts = trimmedLine.split(Pattern.quote(" "));
				packageName.set(parts[1]
						.replace(";", "")
						.trim());
				return;
			}
			
			if(trimmedLine.contains(" class ") && className.get() == null) {
				String partiallyCleanedName = trimmedLine
						.replace("public", "")
						.replace("private", "")
						.replace("protected", "")
						.replace("static", "")
						.replace("final", "")
						.replace("class", "")
						.trim();
				
				String[] parts = partiallyCleanedName.split(Pattern.quote(" "));
				className.set(parts[0]);
				return;
			}
		});
		
		br.close();
		
		if(packageName.get() == null || className.get() == null) {
			throw new IllegalStateException(String.format("Unable to find package (%s) and class name (%s) for File '%s'", packageName.get(), className.get(), f.getAbsolutePath()));
		}
		
		Class<?> clazz;
		try {
			clazz = Class.forName(String.format("%s.%s", packageName.get(), className.get()));
		} catch(ClassNotFoundException e) {
			throw new IllegalStateException(String.format("Unable to load class '%s.%s'. It could not be found.", packageName.get(), className.get()));
		}
		
		HashMap<String, UnparsedMethodDescriptor> methodCalls = new HashMap<>();
		HashMap<String, Integer> methodNameOccurence = new HashMap<>();
		
		for(Method method : clazz.getDeclaredMethods()) {
			Class<?>[] paramTypes = method.getParameterTypes();
			String[] params = new String[paramTypes.length];
			for(int i = 0; i < params.length; i++) {
				params[i] = paramTypes[i].getName();
			}
			
			String methodName = method.getName();
			Integer occurenceCount = methodNameOccurence.merge(methodName, 1, Integer::sum);
			
			if(methodCalls.containsKey(methodName)) {
				methodName = String.format("%s_%d", methodName, occurenceCount);
			}
			
			UnparsedMethodDescriptor mc = new UnparsedMethodDescriptor(methodName, method.getReturnType().getName(), params);
			methodCalls.put(methodName, mc);
		}
		
		UnparsedMethodDescriptor[] methodCallArr = methodCalls.values().toArray(new UnparsedMethodDescriptor[0]);
		ClassDescriptor cd = new ClassDescriptor(packageName.get(), className.get());
		return new Pair<>(cd, methodCallArr);
	}
}
