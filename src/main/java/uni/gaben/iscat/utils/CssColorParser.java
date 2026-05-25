package uni.gaben.iscat.utils;

import javafx.scene.paint.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
     * Parses a CSS file resource and maps variable declarations to JavaFX Color states.
     * Strips structural styling noise, blocks comment pollution, and handles multi-format declarations.
     * * @param cssResourcePath Absolute file path to the resource stylesheet.
     * @return Map pairing variable names (without leading dashes) directly to extracted Color structures.
     */
    public static Map<String, Color> parseColors(String cssResourcePath) {
        Map<String, Color> colors = new HashMap<>();

        try (InputStream is = CssColorParser.class.getResourceAsStream(cssResourcePath)) {
            if (is == null) {
                System.err.println("CssColorParser Error: Target CSS file resource missing: " + cssResourcePath);
                return colors;
            }

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
        } catch (Exception e) {
            System.err.println("CssColorParser Critical Exception encountered processing stylesheet: " + cssResourcePath);
            e.printStackTrace();
        }
        return colors;
    }
}