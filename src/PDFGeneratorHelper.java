import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.cos.COSName;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PDFGeneratorHelper {

    private static final String TEMPLATE_DIR = "templates/";
    private static final String OUTPUT_DIR = "filled_forms/";
    private static final String MAPPING_FILE = "amc_field_mapping.json";

    public static void fillForms(List<String> selectedAMCs, Map<String, String> formData) {
        try {
            JSONObject fullMapping = (JSONObject) new JSONParser().parse(new FileReader(MAPPING_FILE));
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
                PDDocument pdfDoc = PDDocument.load(new File(templatePath));
                PDAcroForm acroForm = pdfDoc.getDocumentCatalog().getAcroForm();

                if (acroForm == null) {
                    System.err.println("No AcroForm found in template: " + templatePath);
                    pdfDoc.close();
                    continue;
                }

                // Fix for missing default resources (for setting font)
                PDResources dr = acroForm.getDefaultResources();
                if (dr == null) {
                    dr = new PDResources();
                    acroForm.setDefaultResources(dr);
                }

                // Set default appearance with Helvetica font if needed
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
                        if (field.getCOSObject().getNameAsString(COSName.FT).equals("Btn")) {
                            // Handle checkbox
                            field.setValue(value.equalsIgnoreCase("Yes") ? "Yes" : "Off");
                        } else {
                            // Handle text
                            field.setValue(value);
                        }
                    } catch (Exception e) {
                        System.err.println("Error setting field " + fieldName + ": " + e.getMessage());
                    }
                }

                String outputFilePath = clientFolder.getAbsolutePath() + "/" + amc + "_filled.pdf";
                pdfDoc.save(outputFilePath);
                pdfDoc.close();
                System.out.println("Saved filled form: " + outputFilePath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
