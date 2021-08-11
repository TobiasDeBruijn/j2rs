package dev.array21.j2rs.generator;

import java.util.regex.Pattern;

import dev.array21.j2rs.parser.ClassDescriptor;
import dev.array21.j2rs.parser.ParsedMethodDescriptor;

public class ClassBuilder {

	private static final String structTemplate = 
			  	"pub struct %NAME%<'a> {"
			+ 	"	inner: 	Object<'a>,"
			+	" 	env:	&'a JNIEnv"
			+	"	%GENERICS%"
			+	"}";
	
	private static final String intoJObjectPtrTemplate = 
				"impl<'a> Into<*mut _jobject> for %NAME%<'a> {"
			+	"	fn into(self) -> *mut _jobject {"
			+	"		self.inner.inner.into_inner()"
			+	"	}"
			+	"}";
	
	private static final String implTemplate = 
				"impl<'a> %NAME%<'a> {"
			+	"	pub fn new(env: &'a JNIEnv<'a>, object: Object<'a>, class: Class<'a>) -> Self {"
			+	"		Self {"
			+	"			inner: object,"
			+	"			class,"
			+	"			env"
			+	"		}"
			+ 	"	}"
			+	"\n"
			+	"	%METHODS%"
			+	"}";
	
	private static final String staticMethodTemplate =
				"	pub fn %RUSTY_NAME%(env: &'a JNIEnv<'a> %PARAM_WITH_TYPES%) -> Result<%RET_TYPE%> {"
			+	"		let ret = env.call_static_method(\"%FULL_CLASS_NAME%\", \"%JAVA_NAME\", \"%SIG%\", &[%PARAM_NAMES%])?;"
			+	"		%HANDLE_RET_TYPE%"
			+	"	}";
	
	private static final String methodTemplate = 
				"	pub fn %RUSTY_NAME%(&self %PARAM_WITH_TYPES) -> Result<%RET_TYPE%> {"
			+	"		let ret = env.call_method(self.inner.inner, \"%JAVA_NAME%\", \"%SIG%\", &[%PARAM_NAMES%])"
			+	"		%HANDLE_RET_TYPE%"
			+	"	}";
	
	public static String toRust(ClassDescriptor cd) {
		String structGenerics = "";
		for(String generic : cd.generics()) {
			String fieldName = String.format("%s_class", generic);
			structGenerics += String.format("\t%s:\t Class<'a>,\n", fieldName);
		}
		
		String struct = structTemplate
				.replace("%NAME%", cd.className())
				.replace("%GENERICS%", structGenerics);
		
		String intoJobjectImpl = intoJObjectPtrTemplate.replace("%NAME%", cd.className());
		
		for(ParsedMethodDescriptor method : cd.pmd()) {
			String[] parameters = new String[method.umd().parameters().length];
			String[] parameterNames = new String[method.umd().parameters().length];
			for(int i = 0; i < method.umd().parameters().length; i++) {
				String paramType = method.umd().parameters()[i];
				String paramTypeWithLt = String.format("%s<'a>", paramType);
				
				String paramName = String.format("arg%d", i);
				parameters[i] = String.format("%s: %s", paramName, paramTypeWithLt);
				parameterNames[i] = paramName;
			}
			
			if(method.isStatic()) {
				String paramWithTypes = "";
				if(parameters.length > 0) {
					paramWithTypes = String.format(", %s", String.join(", ", parameters));
				}
				
				String methodTemplated = staticMethodTemplate
						.replace("%RUSTY_NAME%", NameProcessor.toSnakeCase(method.javaName()))
						.replace("%PARAM_WITH_TYPES", paramWithTypes)
						.replace("%RET_TYPE%", getRetType(method))
						.replace("%FULL_CLASS_NAME%", String.format("%s.%s", cd.packageName(), cd.className()).replace('.', '/'))
						.replace("%JAVA_NAME%", method.javaName())
						.replace("%SIG%", method.sig())
						.replace("%PARAM_NAMES%", String.join(", ", parameterNames))
						.replace("%HANDLE_RET_TYPE%", handleRetType(method));
			}
		}
	}
	
	private static String handleRetType(ParsedMethodDescriptor pmd) {
		String ret = "";
		switch(pmd.rt()) {
		case BOOLEAN:
			ret = "Ok(ret.z()?)";
			break;
		case BYTE:
			ret = "Ok(ret.b()?)";
			break;
		case INTEGER:
			ret = "Ok(ret.i()?)";
			break;
		case LONG:
			ret = "Ok(ret.j()?)";
			break;
		case DOUBLE:
			ret = "Ok(ret.d()?)";
			break;
		case FLOAT:
			ret = "Ok(ret.f()?)";
			break;
		case CHAR:
			ret = "Ok(ret.c()?)";
			break;
		case SHORT:
			ret = "Ok(ret.s()?)";
			break;
		case VOID:
			ret = "Ok(())";
			break;
		case BOOLEAN_ARRAY:
			ret = 
				"let mut buf = Vec::new();"
			+	"env.get_boolean_array_region(ret.l()?.into_inner(), 0, &mut buf)?;"
			+	"Ok(buf)";
			break;
		case BYTE_ARRAY:
			ret =
				"let mut buf = Vec::new();"
			+	"env.get_byte_array_region(ret.l()?.into_inner(), 0, &mut buf)?;"
			+ 	"Ok(buf)";
			break;
		case CHAR_ARRAY:
			ret =
				"let mut buf = Vec::new();"
			+	"env.get_char_array_region(ret.l()?.into_inner(), 0, &mut buf)?;"
			+	"Ok(buf)";
			break;
		case DOUBLE_ARRAY:
			ret =
				"let mut buf = Vec::new();"
			+	"env.get_double_array_region(ret.l()?.into_inner(), 0, &mut buf)?;"
			+	"Ok(buf)";
			break;
		case FLOAT_ARRAY:
			ret = 
				"let mut buf = Vec::new();"
			+	"env.get_float_array_region(ret.l()?.into_inner(), 0, &mut buf)?;"
			+	"Ok(buf)";
			break;
		case INTEGER_ARRAY:
			ret =
				"let mut buf = Vec::new();"
			+	"env.get_integer_array_region(ret.l()?.into_inner(), 0, &mut buf)?;"
			+	"Ok(buf)";
			break;
		case LONG_ARRAY:
			ret = 
				"let mut buf = Vec::new();"
			+	"env.get_long_array_region(ret.l()?.into_inner(), 0, &mut buf)?;"
			+	"Ok(buf)";
			break;
		case SHORT_ARRAY:
			ret =
				"let mut buf = Vec::new();"
			+	"env.get_short_array_region(ret.l()?.into_inner(), 0, &mut buf)?;"
			+	"Ok(buf)";
			break;
		case OBJECT:
			ret = "Ok(ret.l()?)";
			break;
		case OBJECT_ARRAY:
			String unprocessedRet =
				"let object = ret.l()?;"
			+	"let arr_len = env.get_array_length(object.into_inner())?;"
			+	"let ret_class = Class::for_name(env, %RET_CLASS_PATH%)?;"
			
			+	"let mut buf = Vec::new();"
			+	"for i in 0..arr_len {"
			+	"	let elem = env.get_object_array_element(object.into_inner(), i)?;"
			+	"	let obj = Object::new(env, elem, ret_class.clone());"
			+	"	let ret_object = %RET_CLASS%::new(env, obj);"
			+	"	buf.push(ret_object);"
			+	"}"
			
			+	"Ok(buf)";
			
			String[] classNameParts = pmd.umd().returnType().split(Pattern.quote("."));
			ret = unprocessedRet
					.replace("%RET_CLASS%", classNameParts[classNameParts.length -1])
					.replace("%RET_CLASS_PATH%", pmd.umd().returnType().replace("[", "").replace('.', '/'));
			break;
		}
	}
	
	private static String getRetType(ParsedMethodDescriptor pmd) {
		String retType = null;
		switch(pmd.rt()) {
		case BOOLEAN:
			retType = "bool";
			break;
		case BYTE:
			retType = "u8";
			break;
		case CHAR:
			retType = "char";
			break;
		case DOUBLE:
			retType = "f64";
			break;
		case FLOAT:
			retType = "f32";
			break;
		case INTEGER:
			retType = "i32";
			break;
		case LONG:
			retType = "i64";
			break;
		case SHORT:
			retType = "i16";
			break;
		case VOID:
			retType = "()";
			break;
		case BOOLEAN_ARRAY:
			retType = "Vec<bool>";
			break;
		case BYTE_ARRAY:
			retType = "Vec<u8>";
			break;
		case CHAR_ARRAY:
			retType = "Vec<char>";
			break;
		case DOUBLE_ARRAY:
			retType = "Vec<f64>";
			break;
		case FLOAT_ARRAY:
			retType = "Vec<f32>";
			break;
		case INTEGER_ARRAY:
			retType = "Vec<i32>";
			break;
		case LONG_ARRAY:
			retType = "Vec<i64>";
			break;
		case SHORT_ARRAY:
			retType = "Vec<i16>";
			break;
		case OBJECT:
		case OBJECT_ARRAY:
			// The unparsed returnType is of format e.g Ljava.lang.Object; or for arrays [Ljava.lang.Object;
			String[] retTypeParts = pmd.umd().returnType().split(Pattern.quote("."));
			retType = retTypeParts[retTypeParts.length - 1].replace(";", "");
		}
		
		return retType;
	}
}
