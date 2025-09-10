module com.ispw.progettoispw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires com.google.gson;


    opens com.ispw.progettoispw to javafx.fxml;
    exports com.ispw.progettoispw;
    opens com.ispw.progettoispw.entity to com.google.gson, javafx.fxml;
    exports com.ispw.progettoispw.entity;



    exports com.ispw.progettoispw.Controller.ControllerGrafico;
    opens com.ispw.progettoispw.Controller.ControllerGrafico to javafx.fxml;

    exports com.ispw.progettoispw.Enum;
    opens com.ispw.progettoispw.Enum to javafx.fxml;





    // se Gson tocca anche altri package (es. DTO/DAO) aprili allo stesso modo
    opens com.ispw.progettoispw.Dao to com.google.gson;
    exports com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;
    opens com.ispw.progettoispw.Controller.ControllerGrafico.Alternative to javafx.fxml;
}