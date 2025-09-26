import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.cos.COSName;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class PDFGeneratorHelper {

    // Set the base directory of your project (change if needed)
    private static final String BASE_DIR = System.getProperty("user.dir") + File.separator;
    private static final String TEMPLATE_DIR = BASE_DIR + "templates" + File.separator;
    private static final String OUTPUT_DIR = BASE_DIR + "filled_forms" + File.separator;
    private static final String MAPPING_FILE = BASE_DIR + "amc_field_mapping.json";

    public static void fillForms(List<String> selectedAMCs, Map<String, String> formData) {
        try {
            // Load mapping JSON file from file system
            File mappingFile = new File(MAPPING_FILE);
            if (!mappingFile.exists()) {
                throw new FileNotFoundException("Mapping file not found: " + mappingFile.getAbsolutePath());
            }

            Reader reader = new InputStreamReader(new FileInputStream(mappingFile), StandardCharsets.UTF_8);
            JSONObject fullMapping = (JSONObject) new JSONParser().parse(reader);
            reader.close();

            // Create output folder for the client
            String clientName = formData.getOrDefault("Applicant name", "Client");
            File clientFolder = new File(OUTPUT_DIR + clientName.replaceAll("\\s+", "_"));
            if (!clientFolder.exists()) clientFolder.mkdirs();

            for (String amc : selectedAMCs) {
                JSONObject amcMapping = (JSONObject) fullMapping.get(amc);
                if (amcMapping == null) {
                    System.err.println("No mapping found for " + amc);
                    continue;
                }

                String templatePath = TEMPLATE_DIR + amc + ".pdf";
                File templateFile = new File(templatePath);
                if (!templateFile.exists()) {
                    System.err.println("Template not found: " + templatePath);
                    continue;
                }

                PDDocument pdfDoc = PDDocument.load(templateFile);
                PDAcroForm acroForm = pdfDoc.getDocumentCatalog().getAcroForm();

                if (acroForm == null) {
                    System.err.println("No AcroForm found in template: " + templatePath);
                    pdfDoc.close();
                    continue;
                }

                PDResources dr = acroForm.getDefaultResources();
                if (dr == null) {
                    dr = new PDResources();
                    acroForm.setDefaultResources(dr);
                }

                acroForm.setDefaultAppearance("/Helv 8 Tf");

                for (Object keyObj : amcMapping.keySet()) {
                    String logicalName = (String) keyObj;
                    String fieldName = (String) amcMapping.get(logicalName);
                    String value = formData.get(logicalName);

                    if (value == null || value.isEmpty()) continue;

                    PDField field = acroForm.getField(fieldName);
                    if (field == null) {
                        System.err.println("Field not found: " + fieldName);
                        continue;
                    }

                    try {
                        if ("Btn".equals(field.getCOSObject().getNameAsString(COSName.FT))) {
                            field.setValue(value.equalsIgnoreCase("Yes") ? "Yes" : "Off");
                        } else {
                            field.setValue(value);
                        }
                    } catch (Exception e) {
                        System.err.println("Error setting field " + fieldName + ": " + e.getMessage());
                    }
                }

                String outputFilePath = clientFolder.getAbsolutePath() + File.separator + amc + "_filled.pdf";
                pdfDoc.save(outputFilePath);
                pdfDoc.close();
                System.out.println("Saved filled form: " + outputFilePath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
