
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.*;

public class Main extends Application {
    private final Map<String, TextField> inputFields = new HashMap<>();
    private final Map<String, CheckBox> amcCheckboxes = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Load AMCs from JSON
        List<String> availableAMCs = loadAvailableAMCs();

        // AMC Checkboxes
        TitledPane amcPane = new TitledPane();
        amcPane.setText("Select AMCs");
        VBox amcBox = new VBox(5);
        for (String amc : availableAMCs) {
            CheckBox cb = new CheckBox(amc);
            amcCheckboxes.put(amc, cb);
            amcBox.getChildren().add(cb);
        }
        amcPane.setContent(amcBox);
        root.getChildren().add(amcPane);

        // Input Fields (populate from union of fields)
        Set<String> allFields = loadAllLogicalFields();
        GridPane fieldGrid = new GridPane();
        fieldGrid.setHgap(10);
        fieldGrid.setVgap(8);
        int row = 0;
        for (String label : allFields) {
            Label lbl = new Label(label + ":");
            TextField tf = new TextField();
            inputFields.put(label, tf);
            fieldGrid.add(lbl, 0, row);
            fieldGrid.add(tf, 1, row);
            row++;
        }
        root.getChildren().add(fieldGrid);

        Button submit = new Button("Generate Filled PDFs");
        submit.setOnAction(e -> {
            List<String> selectedAMCs = new ArrayList<>();
            for (String amc : availableAMCs) {
                if (amcCheckboxes.get(amc).isSelected()) {
                    selectedAMCs.add(amc);
                }
            }
            Map<String, String> inputData = new HashMap<>();
            for (Map.Entry<String, TextField> entry : inputFields.entrySet()) {
                inputData.put(entry.getKey(), entry.getValue().getText());
            }
            PDFGeneratorHelper.fillForms(selectedAMCs, inputData);
        });
        root.getChildren().add(submit);

        primaryStage.setTitle("Unified Mutual Fund Form Filler");
        primaryStage.setScene(new Scene(root, 850, 700));
        primaryStage.show();
    }

    private List<String> loadAvailableAMCs() {
        List<String> amcs = new ArrayList<>();
        try {
            JSONObject fullMapping = (JSONObject) new JSONParser().parse(new FileReader("amc_field_mapping.json"));
            for (Object key : fullMapping.keySet()) {
                amcs.add((String) key);
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading AMCs: " + e.getMessage());
        }
        return amcs;
    }

    private Set<String> loadAllLogicalFields() {
        Set<String> allFields = new TreeSet<>();
        try {
            JSONObject fullMapping = (JSONObject) new JSONParser().parse(new FileReader("amc_field_mapping.json"));
            for (Object key : fullMapping.keySet()) {
                JSONObject fieldMap = (JSONObject) fullMapping.get(key);
                for (Object fieldKey : fieldMap.keySet()) {
                    allFields.add((String) fieldKey);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading field names: " + e.getMessage());
        }
        return allFields;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
