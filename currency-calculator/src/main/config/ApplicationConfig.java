package main.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ApplicationConfig {
    static final String FILE_NAME = "C:\\Users\\Nevels\\IdeaProjects\\Modsen\\currency-calculator\\src\\resourses\\application.properties";

    public double readFromFile() {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(FILE_NAME)) {
            prop.load(fis);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return Double.parseDouble(prop.getProperty("currency.dollar"));
    }
}
