package dev.array21.j2rs.parser;

public record ClassDescriptor(
		String packageName, 
		String className, 
		ParsedMethodDescriptor[] pmd,
		String[] generics) {}
