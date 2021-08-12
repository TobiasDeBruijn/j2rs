use jni::{InitArgsBuilder, JavaVM, JNIVersion};
use log::{info, error, trace, warn};
use clap::{App, Arg};
use std::path::PathBuf;

mod class_analyzer;

fn main() {
    let matches = App::new("j2rs")
        .version(std::env::var("CARGO_PKG_VERSION").unwrap().as_str())
        .author("Tobias de Bruijn <t.debruijn@array21.dev>")
        .about("Automatically create JNI abstractions for Java's standard library")
        .arg(Arg::with_name("v")
            .short("v")
            .multiple(true)
            .help("Sets the log level, can be applied multiple times. 0x = ERROR+WARN, 1x = INFO, 2x = TRACE"))
        .arg(Arg::with_name("java-home")
            .short("j")
            .takes_value(true)
            .value_name("java-home")
            .help("Set's the Java Home variable and add's libjvm to the path"))
        .arg(Arg::with_name("source")
            .short("s")
            .required(true)
            .value_name("source")
            .takes_value(true)
            .help("The source file or directory (recursive) to generate abstractions for"))
        .get_matches();

    match matches.occurrences_of("v") {
        0 => std::env::set_var("RUST_LOG", "warn"),
        1 => std::env::set_var("RUST_LOG", "info"),
        2 | _ => std::env::set_var("RUST_LOG", "trace")
    }

    env_logger::init();

    if let Some(java_home) = matches.value_of("java-home") {
        std::env::set_var("JAVA_HOME", java_home);

        #[cfg(windows)]
        {std::env::set_var("Path", format!("{}:{}", std::env::var("Path").unwrap(), r#"%JAVA_HOME%\bin\server"#))}

        #[cfg(not(windows))]
        {std::env::set_var("LD_LIBRARY_PATH", format!("{}/bin/server", java_home))}
    }

    let input_source = matches.value_of("source").unwrap();
    let input_source = PathBuf::from(input_source.replace("\"", "").replace("\\\\", "\\"));

    info!("Starting j2rs");

    let jvm_args = match InitArgsBuilder::new()
        .version(JNIVersion::V8)
        .option("-Xcheck:jni")
        .build() {
        Ok(a) => a,
        Err(e) => {
            error!("Failed to build JVM Init arguments: {:?}", e);
            std::process::exit(1);
        }
    };

    let jvm = match JavaVM::new(jvm_args) {
        Ok(jvm) => jvm,
        Err(e) => {
            error!("Failed to create JavaVM: {:?}", e);
            std::process::exit(1);
        }
    };

    let env = match jvm.attach_current_thread_as_daemon() {
        Ok(e) => e,
        Err(e) => {
            error!("Failed to attach to the JVM: {:?}", e);
            std::process::exit(1);
        }
    };

    info!("Attached to the JVM");

    if !input_source.exists() {
        error!("Provided source path '{}' does not exist", input_source.to_str().unwrap());
        std::process::exit(1);
    }

    if input_source.is_file() {
        let class_ident = class_analyzer::find_class_identifier(&input_source);
        let class_object = match env.find_class(class_ident.replace(".", "/")) {
            Ok(c) => c,
            Err(e) => {
                error!("Failed to load class '{}', is it on the classpath?", class_ident);
                std::process::exit(1);
            }
        };

    }
}