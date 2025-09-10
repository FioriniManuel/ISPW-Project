package com.ispw.progettoispw.Controller.ControllerApplicativo;

import com.ispw.progettoispw.Enum.StorageOption;
import com.ispw.progettoispw.Factory.DaoFactory;

public class MainController {

    public  MainController(){}
    public void persistenza(){
        DaoFactory.setStorageOption(StorageOption.FILE);

    }

    public void memory() {
        DaoFactory.setStorageOption(StorageOption.INMEMORY);
    }
}
