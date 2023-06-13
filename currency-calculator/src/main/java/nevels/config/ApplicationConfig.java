package nevels.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApplicationConfig {
    static final String FILE_NAME = ".\\src\\main\\resources\\application.properties";
    final String CURRENCY_DOLLAR = "currency.dollar";

    public double readFromFile() {
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(FILE_NAME)) {
            prop.load(fis);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return Double.parseDouble(prop.getProperty(CURRENCY_DOLLAR));
    }
}