import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

public class PDFGeneratorHelper {

    public static void fillForms(List<String> selectedAMCs, Map<String, String> inputData) {
        try {
            JSONObject fullMapping = (JSONObject) new JSONParser().parse(new FileReader("amc_field_mapping.json"));

            for (String amc : selectedAMCs) {
                JSONObject fieldMap = (JSONObject) fullMapping.get(amc);
                if (fieldMap == null) {
                    System.out.println("❌ Mapping not found for AMC: " + amc);
                    continue;
                }

                File template = new File("templates/" + amc + ".pdf");
                if (!template.exists()) {
                    System.out.println("❌ Template not found: " + template.getAbsolutePath());
                    continue;
                }

                PDDocument document = PDDocument.load(template);
                PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
                if (acroForm == null) {
                    System.out.println("❌ No AcroForm in " + template.getName());
                    document.close();
                    continue;
                }

                for (Object keyObj : fieldMap.keySet()) {
                    String logicalKey = (String) keyObj;
                    String value = inputData.get(logicalKey);
                    if (value == null || value.trim().isEmpty()) continue;

                    String fieldSpec = (String) fieldMap.get(logicalKey);

                    // Handle multiple fields (e.g., "text18 and text18#1")
                    for (String rawFieldName : fieldSpec.split("and")) {
                        String fieldName = rawFieldName.trim();

                        if (fieldName.toLowerCase().startsWith("check box")) {
                            String[] parts = fieldName.split("#");
                            if (parts.length >= 2) {
                                String baseName = parts[0].trim();
                                String[] indices = parts[1].split(",");
                                for (String idx : indices) {
                                    String fullCheckBox = baseName + "#" + idx.trim();
                                    PDField field = acroForm.getField(fullCheckBox);
                                    if (field instanceof PDCheckBox) {
                                        if ("yes".equalsIgnoreCase(value) || value.equals(idx.trim())) {
                                            ((PDCheckBox) field).check();
                                        } else {
                                            ((PDCheckBox) field).unCheck();
                                        }
                                    }
                                }
                            }
                        } else {
                            PDField field = acroForm.getField(fieldName);
                            if (field != null) {
                                try {
                                    field.setValue(value);
                                } catch (Exception fe) {
                                    System.out.println("⚠️ Error setting field: " + fieldName + " - " + fe.getMessage());
                                }
                            } else {
                                System.out.println("🚫 Field not found: " + fieldName);
                            }
                        }
                    }
                }

                String clientName = inputData.getOrDefault("applicant name", "Client").replaceAll("[^a-zA-Z0-9]", "_");
                File outDir = new File("filled_forms/" + clientName);
                outDir.mkdirs();
                File outFile = new File(outDir, amc + "_Filled.pdf");
                document.save(outFile);
                document.close();

                System.out.println("✅ Saved: " + outFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.out.println("❌ Error while filling: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

