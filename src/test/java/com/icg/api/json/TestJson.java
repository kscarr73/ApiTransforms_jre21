package com.icg.api.json;

import com.progbits.api.exception.ApiException;
import com.progbits.api.json.UTF8JsonParser;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestJson {

    String strJsonTest = """
                         {
                            "MyField": "ThisTest",
                            "OtherField": "ThisValue",
                            "AValue": true,
                            "IValue": 2134,
                            "FValue": 123.31,
                            "MyIntArray": [ 2134, 1234, 1234, 51313 ],
                            "MyStrArray": [ "This", "That", "Other" ]
                         }
                         """;
    @Test()
    public void testJson() {
        UTF8JsonParser json = new UTF8JsonParser(strJsonTest);

        try {
            String keyName = null;
            
            while (json.nextToken() != UTF8JsonParser.Event.END_OBJECT) {
                UTF8JsonParser.Event currEvent = json.currentEvent();

                switch (currEvent) {
                    case KEY_NAME -> {
                        keyName = json.getString();
                        
                    }

                    case VALUE_STRING -> {
                        // Nothing to do
                    }

                    case VALUE_TRUE -> {
                        // Nothing to do
                    }

                    case VALUE_NUMBER_INT -> {
                        // Nothing to do
                    }

                    case VALUE_NUMBER_FLOAT -> {
                        // Nothing to do
                    }

                    default -> {
                        // Nothing to do
                    }
                }
            }
        } catch (ApiException ex) {

        }
    }
    
    @Test
    public void run100() {
        for (var x = 0; x<100;x++) {
            testJson();
        }
    }
    
    @Test
    public void run1000() {
        for (var x = 0; x<1000;x++) {
            testJson();
        }
    }
    
    @Test
    public void run10000() {
        for (var x = 0; x<10000;x++) {
            testJson();
        }
    }
    
    @Test()
    public void run100000() {
        for (var x = 0; x<100000;x++) {
            testJson();
        }
    }
    
    @Test()
    public void run1000000() {
        for (var x = 0; x<100000;x++) {
            testJson();
        }
    }
}
