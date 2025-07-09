import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;


import java.util.*;

public class Main extends Application {

    private final Map<String, TextField> fieldInputs = new LinkedHashMap<>();
    private final List<String> labels = Arrays.asList(
        "First_Sole_Applicant_Name_as_per_PAN", "PAN", "Date_of_Birth", "Email", "Mobile",
        "Bank_Account_Number", "Bank_Name", "IFSC_Code", "MICR_Code"
    );

    private final Map<String, List<String>> dropdowns = Map.of(
        "Account_Type", List.of("SAVINGS", "CURRENT", "NRE", "NRO", "OTHER"),
        "Gender", List.of("Male", "Female", "Other"),
        "Income_Slab", List.of("Below_1_Lakh", "1_5_Lakhs", "5_10_Lakhs", "10_25_Lakhs", "Above_25_Lakhs", "Above_1_Crore")
    );

    private final Map<String, ComboBox<String>> dropdownInputs = new HashMap<>();

    @Override
    public void start(Stage stage) {
        VBox form = new VBox(10);
        form.setPadding(new Insets(15));

        for (String label : labels) {
            Label lbl = new Label(label);
            TextField tf = new TextField();
            fieldInputs.put(label, tf);
            form.getChildren().addAll(lbl, tf);
        }

        for (String label : dropdowns.keySet()) {
            Label lbl = new Label(label);
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll(dropdowns.get(label));
            combo.setValue(dropdowns.get(label).get(0));
            dropdownInputs.put(label, combo);
            form.getChildren().addAll(lbl, combo);
        }

        Button submit = new Button("Generate Mirae Form");
        submit.setOnAction(e -> {
            Map<String, String> data = new HashMap<>();
            fieldInputs.forEach((k, v) -> data.put(k, v.getText()));
            dropdownInputs.forEach((k, v) -> data.put(k, v.getValue()));
            PDFGeneratorHelper.fillMiraeForm(data);
        });

        form.getChildren().add(submit);

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 400, 600);
        stage.setScene(scene);
        stage.setTitle("Mutual Fund Form Filler - Mirae");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
