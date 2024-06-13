@echo off

REM Proceed with running the program
for /R %%I in (SBLCP_Local_Terminal*.jar) do (
    echo Running: %%I
    java --module-path .\lib\javafx-sdk-20.0.1\lib --add-modules javafx.controls,javafx.fxml -jar "%%I"
)
