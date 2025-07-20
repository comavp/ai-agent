package ru.comavp;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
@Setter
public class GigaChatProperties {

    private String authKey;

    public GigaChatProperties() {
        Properties properties = getProperties();
        authKey = properties.getProperty("authkey");
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringSubstitutor interpolator = new StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup());
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            String resolvedValue = interpolator.replace(value);
            properties.setProperty(key, resolvedValue);
        }

        return properties;
    }
}
