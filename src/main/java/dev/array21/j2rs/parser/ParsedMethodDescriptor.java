package dev.array21.j2rs.parser;

public record ParsedMethodDescriptor(boolean isStatic, String javaName, String sig, ReturnType rt, UnparsedMethodDescriptor umd) {}
