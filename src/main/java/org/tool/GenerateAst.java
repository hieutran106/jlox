package org.tool;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        List<String> list = Arrays.asList(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right"
        );
        defineAst(outputDir, "Expr", list);
    }

    private static void defineAst(
            String outputDir,
            String baseName,
            List<String> types
    ) {

    }
}
