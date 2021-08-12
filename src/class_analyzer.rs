use std::fs::File;
use log::{error, warn, trace, info};
use std::path::Path;

pub fn find_class_identifier(path: &Path) -> String {
    let contents = match std::fs::read_to_string(path) {
        Ok(c) => c,
        Err(e) => {
            error!("Failed to read contents of source file to String: {:?}", e);
            std::process::exit(1);
        }
    };

    let mut in_comment = false;
    let mut class_name = String::default();
    let mut package_name = String::default();

    for line in contents.lines() {
        let trimmed_line = line.trim();
        if trimmed_line.starts_with("//") {
            continue;
        }

        if trimmed_line.starts_with("/*") || trimmed_line.starts_with("/**") {
            in_comment = true;
            continue;
        }

        if trimmed_line.starts_with("*/") {
            in_comment = false;
            continue;
        }

        if in_comment {
            continue;
        }

        if trimmed_line.starts_with("package") && class_name.is_empty() {
            let parts: Vec<&str> = trimmed_line.split(" ").collect();
            package_name = parts
                .get(1)
                .unwrap()
                .replace(";", "")
                .trim()
                .to_string();
        }

        if (trimmed_line.contains("class" ) || trimmed_line.contains("interface")) && class_name.is_empty() {
            let cleaned_up_name = trimmed_line
                .replace("public", "")
                .replace("private", "")
                .replace("protected", "")
                .replace("static", "")
                .replace("final", "")
                .replace("class", "")
                .replace("interface", "")
                .trim()
                .to_string();

            let parts: Vec<&str> = cleaned_up_name.split(" ").collect();

            let class_maybe_generic = *parts.get(0).unwrap();
            class_name = class_maybe_generic.split("<").collect::<Vec<&str>>().get(0).unwrap().to_string();

            if class_maybe_generic.contains("<") {
                let class_name_parts: Vec<&str> = class_maybe_generic.split("<").collect(); // Foo<A, B>
                let generics: Vec<&str> = class_name_parts      // 'Foo', 'A, B>'
                    .get(1)
                    .unwrap()                            // 'A', 'B>'
                    .replace(">", "")            // 'A, B'
                    .split(",")         // 'A', ' B'
                    .map(|g| g.trim()) // 'A', 'B'
                    .collect();
            }
        }
    }

    if class_name.is_empty() || package_name.is_empty() {
        error!("Class '{}' is invalid, it does not contain a package name or class name.", path.to_str().unwrap());
        error!("Found class name '{}' and package name '{}'", class_name, package_name);
        std::process::exit(1);
    }

    format!("{}.{}", package_name, class_name)
}