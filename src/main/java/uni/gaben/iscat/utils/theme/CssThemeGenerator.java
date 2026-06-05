package uni.gaben.iscat.utils;

import java.io.*;
import java.util.List;

public class CssThemeGenerator {

    public static File createDynamicStylesheet(String baseResourcePath, List<String> hexColors) {
        String p = hexColors.size() > 0 ? hexColors.get(0) : "#ff0000";
        String s = hexColors.size() > 1 ? hexColors.get(1) : "#ff6200";
        String t = hexColors.size() > 2 ? hexColors.get(2) : "#ffa600";
        String bg = hexColors.size() > 3 ? hexColors.get(3) : "#010203"; // 4th color assignment

        try {
            InputStream is = CssThemeGenerator.class.getResourceAsStream(baseResourcePath);
            if (is == null) throw new FileNotFoundException("Base CSS not found: " + baseResourcePath);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            File tempCss = File.createTempFile("iscat-dynamic-theme-", ".css");
            tempCss.deleteOnExit();

            BufferedWriter writer = new BufferedWriter(new FileWriter(tempCss));
            String line;

            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("-accent-primary:")) {
                    writer.write("    -accent-primary: " + p + ";\n");
                } else if (trimmed.startsWith("-accent-secondary:")) {
                    writer.write("    -accent-secondary: " + s + ";\n");
                } else if (trimmed.startsWith("-accent-tertiary:")) {
                    writer.write("    -accent-tertiary: " + t + ";\n");
                } else if (trimmed.startsWith("-bg-primary:")) { // Core backdrop variable overwrite
                    writer.write("    -bg-primary: " + bg + ";\n");
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