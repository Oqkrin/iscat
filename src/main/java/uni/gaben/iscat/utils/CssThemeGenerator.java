package uni.gaben.iscat.utils;

import java.io.*;
import java.util.List;

public class CssThemeGenerator {

    /**
     * Reads the base internal CSS, replaces accent colors, and writes a new physical file.
     */
    public static File createDynamicStylesheet(String baseResourcePath, List<String> hexColors) {
        String p = hexColors.size() > 0 ? hexColors.get(0) : "#ff0000";
        String s = hexColors.size() > 1 ? hexColors.get(1) : "#ff6200";
        String t = hexColors.size() > 2 ? hexColors.get(2) : "#ffa600";

        try {
            // Read from internal JAR resources
            InputStream is = CssThemeGenerator.class.getResourceAsStream(baseResourcePath);
            if (is == null) throw new FileNotFoundException("Base CSS not found: " + baseResourcePath);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            // Create a temporary file on the OS
            File tempCss = File.createTempFile("iscat-dynamic-theme-", ".css");
            tempCss.deleteOnExit(); // Auto-cleanup when game closes

            BufferedWriter writer = new BufferedWriter(new FileWriter(tempCss));
            String line;

            while ((line = reader.readLine()) != null) {
                // Intercept and rewrite the specific CSS variables
                if (line.trim().startsWith("-accent-primary:")) {
                    writer.write("    -accent-primary: " + p + ";\n");
                } else if (line.trim().startsWith("-accent-secondary:")) {
                    writer.write("    -accent-secondary: " + s + ";\n");
                } else if (line.trim().startsWith("-accent-tertiary:")) {
                    writer.write("    -accent-tertiary: " + t + ";\n");
                } else {
                    writer.write(line + "\n");
                }
            }

            writer.close();
            reader.close();
            return tempCss;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}