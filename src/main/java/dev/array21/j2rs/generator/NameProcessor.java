package dev.array21.j2rs.generator;

/**
 * Process method names to convert them to be more Rust-like
 * @author Tobias de Bruijn
 *
 */
public class NameProcessor {

	/**
	 * Convert a name to it's snake_case counterpart
	 * @param name The name
	 * @return The converted name
	 */
	public static String toSnakeCase(String name) {		
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}
}
