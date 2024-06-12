# SBLCP_local_terminal_GUI

How to run/edit this code on your computer?
1. Download Eclipse for Java.
2. Download Scene Builder.
3. Pull this code onto your local repo.
4. In Eclipse, import this project (no special build configurations should be needed, I might be wrong tho)
5. The file that generate the GUI is mainGUI.fxml. Please use SceneBuilder to edit that file if you want to have a life 🤣.
6. The file that controls the GUI (the backend of the GUI) is mainGUIcontroller.java. That file is long, how to read that file is detailed in the header comment. Please read that.
7. MainGUI.java is like the main function. It handles the opening and the closing of the window (JavaFX calls it Scene). Don't add more stuff to it unless you couldn't add it to mainGUIcontroller (like cleanup functions on window close).
8. The "util" package is where all the util classes live.
9. The "tests" package is where all the tests (auto calibration, measurement tests, endurance tests, etc) live.
10. Some libraries have to be specially included in module-info.java. Google that, I don't fully understand that part either.
