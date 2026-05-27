package uni.gaben.iscat.utils;

import javafx.scene.paint.Color;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Robust utility for extracting custom CSS color variables (-variable-name: value;)
 * and compiling them directly into usable JavaFX runtime Color mappings.
 */
public class CssColorParser {

    /**
     * Parses a CSS file resource from the internal application JAR path and maps variable declarations.
     * * @param cssResourcePath Absolute file path to the resource stylesheet.
     * @return Map pairing variable names (without leading dashes) directly to extracted Color structures.
     */
    public static Map<String, Color> parseColors(String cssResourcePath) {
        try (InputStream is = CssColorParser.class.getResourceAsStream(cssResourcePath)) {
            if (is == null) {
                System.err.println("CssColorParser Error: Target CSS file resource missing: " + cssResourcePath);
                return new HashMap<>();
            }
            return parseFromStream(is);
        } catch (Exception e) {
            System.err.println("CssColorParser Critical Exception encountered processing internal stylesheet: " + cssResourcePath);
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Parses a physical CSS file located externally on disk (e.g., from a temp folder) and maps variable declarations.
     * * @param externalCssFile The physical File handle pointing to the stylesheet on the operating system.
     * @return Map pairing variable names (without leading dashes) directly to extracted Color structures.
     */
    public static Map<String, Color> parseExternalColors(File externalCssFile) {
        if (externalCssFile == null || !externalCssFile.exists()) {
            System.err.println("CssColorParser Error: Target external CSS file missing or null.");
            return new HashMap<>();
        }

        try (FileInputStream fis = new FileInputStream(externalCssFile)) {
            return parseFromStream(fis);
        } catch (Exception e) {
            System.err.println("CssColorParser Critical Exception encountered processing external stylesheet: " + externalCssFile.getAbsolutePath());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Shared processing pipeline. Consumes any generic InputStream and extracts variables.
     */
    private static Map<String, Color> parseFromStream(InputStream is) throws IOException {
        Map<String, Color> colors = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean inMultiLineComment = false;

        // Permits flexible spaces, trailing semi-colons, hex variations, and parenthetical color wrappers
        Pattern pattern = Pattern.compile("-([a-zA-Z0-9-]+)\\s*:\\s*(#[0-9a-fA-F]{3,8}|rgba?\\([^)]+\\))\\s*;?");

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Multi-line block comment routing safety filters
            if (line.contains("/*")) inMultiLineComment = true;
            if (inMultiLineComment) {
                if (line.contains("*/")) {
                    inMultiLineComment = false;
                    line = line.substring(line.indexOf("*/") + 2).trim();
                } else {
                    continue;
                }
            }

            // Skip pure line comments or structural padding brackets
            if (line.isEmpty() || line.startsWith("*") || line.startsWith("//") || line.equals("}") || line.equals(".root {")) {
                continue;
            }

            Matcher m = pattern.matcher(line);
            if (m.find()) {
                String varName = m.group(1);
                String colorVal = m.group(2);
                try {
                    colors.put(varName, Color.web(colorVal));
                } catch (IllegalArgumentException e) {
                    System.err.println("CssColorParser Error: Parsing failure on format '" + colorVal + "' inside variable: " + varName);
                }
            }
        }
        return colors;
    }
}