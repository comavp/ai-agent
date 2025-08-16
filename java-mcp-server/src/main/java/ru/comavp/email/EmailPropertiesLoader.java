package ru.comavp.email;

import lombok.Getter;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
public class EmailPropertiesLoader {

    private Properties properties;
    private String userName;
    private String password;
    private String recipient;

    public EmailPropertiesLoader() {
        loadProperties();
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var interpolator = new StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup());

        userName = interpolator.replace(properties.getProperty("username"));
        password = interpolator.replace(properties.getProperty("password"));
        recipient = interpolator.replace(properties.getProperty("recipient"));
        properties.remove("username");
        properties.remove("password");
        properties.remove("recipient");
    }
}
