import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;
import java.util.List;

public class FieldLister {
    public static void main(String[] args) throws Exception {
        PDDocument document = PDDocument.load(new File("templates/Mirae.pdf"));
        PDAcroForm form = document.getDocumentCatalog().getAcroForm();
        List<PDField> fields = form.getFields();

        for (PDField field : fields) {
            System.out.println("Field: " + field.getFullyQualifiedName());
        }

        document.close();
    }
}
