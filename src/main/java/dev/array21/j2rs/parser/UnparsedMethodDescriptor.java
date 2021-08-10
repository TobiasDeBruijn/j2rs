package dev.array21.j2rs.parser;

/**
 * Describes the form of a Method, with a name a return type and an array of parameters
 * @author Tobias de Bruijn
 */
public record UnparsedMethodDescriptor(String name, String returnType, String[] parameters) {}
