import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.*;

public class Main extends Application {

    private final Map<String, TextField> fieldInputs = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(15));

        // 1. AMC checkbox selection
        Label amcLabel = new Label("Select AMCs:");
        HBox amcBox = new HBox(10);
        CheckBox axisCheck = new CheckBox("Axis");
        CheckBox miraeCheck = new CheckBox("Mirae");
        CheckBox iciciCheck = new CheckBox("ICICI");
        CheckBox hdfcCheck = new CheckBox("HDFC");
        CheckBox sbiCheck = new CheckBox("SBI");
        CheckBox tataCheck = new CheckBox("TATA");
        CheckBox kotakCheck = new CheckBox("KOTAK");
        amcBox.getChildren().addAll(axisCheck, miraeCheck, iciciCheck, hdfcCheck,sbiCheck, tataCheck, kotakCheck);

        // 2. Field container
        VBox formFields = new VBox(10);
        formFields.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(formFields);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);

        // 3. Load all unique field labels from all AMCs
        try {
            JSONObject fullMap = (JSONObject) new JSONParser().parse(new FileReader("amc_field_mapping.json"));
            Set<String> allFields = new LinkedHashSet<>();
            for (Object amc : fullMap.keySet()) {
                JSONObject amcMap = (JSONObject) fullMap.get(amc);
                allFields.addAll(amcMap.keySet());
            }

            for (String label : allFields) {
                HBox row = new HBox(10);
                Label lbl = new Label(label + ":");
                lbl.setMinWidth(250);
                TextField input = new TextField();
                row.getChildren().addAll(lbl, input);
                formFields.getChildren().add(row);
                fieldInputs.put(label, input);
            }

        } catch (Exception e) {
            e.printStackTrace();
            formFields.getChildren().add(new Label("❌ Failed to load field mappings."));
        }

        // 4. Button to generate forms
        Button generateBtn = new Button("Generate PDFs");
        generateBtn.setOnAction(e -> {
            List<String> selectedAMCs = new ArrayList<>();
            if (axisCheck.isSelected()) selectedAMCs.add("Axis");
            if (miraeCheck.isSelected()) selectedAMCs.add("Mirae");
            if (iciciCheck.isSelected()) selectedAMCs.add("ICICI");
            if (hdfcCheck.isSelected()) selectedAMCs.add("HDFC");
            if (sbiCheck.isSelected()) selectedAMCs.add("SBI");
            if (tataCheck.isSelected()) selectedAMCs.add("TATA");
            if (kotakCheck.isSelected()) selectedAMCs.add("KOTAK");

            if (selectedAMCs.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select at least one AMC.");
                alert.show();
                return;
            }

            Map<String, String> formData = new HashMap<>();
            for (Map.Entry<String, TextField> entry : fieldInputs.entrySet()) {
                formData.put(entry.getKey(), entry.getValue().getText());
            }

            PDFGeneratorHelper.fillForms(selectedAMCs, formData);
        });

        HBox buttonBar = new HBox(15, generateBtn);
        mainContainer.getChildren().addAll(amcLabel, amcBox, scrollPane, buttonBar);

        Scene scene = new Scene(mainContainer, 800, 700);
        primaryStage.setTitle("PRM Advisory");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
