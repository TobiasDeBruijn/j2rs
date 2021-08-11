package dev.array21.j2rs.parser;

/**
 * Describes a class, with a name and a package
 * @author Tobias de Bruijn
 *
 */
public record ClassIdentifier(String packageName, String className, String[] generics) {}
