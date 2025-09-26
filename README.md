# PRMApp

Compile commands:
 javac --release 21 \
  --module-path "$JAVAFX" \
  --add-modules javafx.controls,javafx.fxml \
  -cp "lib/*" \
  -d out @sources.txt

run commands:
java --module-path "$JAVAFX" --add-modules javafx.controls,javafx.fxml \
  -cp "out:lib/*" Main
  