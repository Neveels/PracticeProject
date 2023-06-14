package nevels.config;

import nevels.exception.BusinessException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApplicationConfig {

    static final String FILE_NAME = "\\src\\main\\resources\\application.properties";
    final String CURRENCY_DOLLAR = "currency.dollar";

    public double readFromFile() {
        Properties prop = new Properties();
        String basePath = new File("").getAbsolutePath();
        try (FileInputStream fis = new FileInputStream(basePath + FILE_NAME)) {
            prop.load(fis);
        } catch (IOException e) {
            throw new BusinessException(e.getMessage());
        }
        return Double.parseDouble(prop.getProperty(CURRENCY_DOLLAR));
    }
}
