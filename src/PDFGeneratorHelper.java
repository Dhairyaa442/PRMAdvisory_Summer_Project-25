import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PDFGeneratorHelper {

    public static void fillMiraeForm(Map<String, String> inputData) {
        try {
            // Load Mirae PDF template
            PDDocument document = PDDocument.load(new File("templates/Mirae.pdf"));
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();

            if (form == null) {
                System.out.println("No AcroForm found in template.");
                document.close();
                return;
            }

            // Load field mappings for Mirae from JSON
            JSONObject mapping = loadMapping("Mirae");

            for (Object key : mapping.keySet()) {
                String label = (String) key;
                String fieldName = (String) mapping.get(label);
                String value = inputData.get(label);

                if (value == null || fieldName == null) continue;

                PDField field = form.getField(fieldName);
                if (field == null) {
                    System.out.println("Field not found: " + fieldName);
                    continue;
                }

                if (field instanceof PDCheckBox) {
                    PDCheckBox checkBox = (PDCheckBox) field;
                    if (value.equalsIgnoreCase("Yes") || value.equalsIgnoreCase("On") || value.equalsIgnoreCase("true")) {
                        checkBox.check();
                    } else {
                        checkBox.unCheck();
                    }
                } else {
                    field.setValue(value);
                }
            }

            // Make sure the filled fields remain editable
            //form.flattenFields(false);

            // Save to client's folder
            String clientName = inputData.getOrDefault("First_Sole_Applicant_Name_as_per_PAN", "UnknownClient").replaceAll("\\s+", "_");
            String outputDir = "filled_forms/" + clientName;
            Files.createDirectories(Paths.get(outputDir));
            String outputPath = outputDir + "/Mirae_Filled.pdf";

            document.save(outputPath);
            document.close();

            System.out.println("✅ Mirae form filled and saved to: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject loadMapping(String amcName) throws Exception {
        FileReader reader = new FileReader("amc_field_mapping.json");
        JSONParser parser = new JSONParser();
        JSONObject root = (JSONObject) parser.parse(reader);
        reader.close();

        return (JSONObject) root.get(amcName);
    }
}
