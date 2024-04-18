package pl.pw.calculator.rating.service;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.math.BigDecimal;

public class ApplicationProperties {

    private static final String WEIGHT_INDEX_PROPERTY_KEY = "rating.weight.index";
    private static final String DIFFERENCE_INDEX_PROPERTY_KEY = "rating.difference.index";

    public BigDecimal getWeightIndex() {
        return getBigDecimalValue( WEIGHT_INDEX_PROPERTY_KEY );
    }

    public BigDecimal getRatingDifferenceIndex() {
        return getBigDecimalValue( DIFFERENCE_INDEX_PROPERTY_KEY );
    }

    private @NotNull BigDecimal getBigDecimalValue(String key) {
        PropertiesConfiguration config = new PropertiesConfiguration();
        try {
            config.read( new FileReader( "src/main/resources/application.properties" ) );
        } catch (Exception e) {
            throw new IllegalStateException( String.format( "Error while reading property: %s", key ), e );
        }

        return config.getBigDecimal( key );
    }
}
