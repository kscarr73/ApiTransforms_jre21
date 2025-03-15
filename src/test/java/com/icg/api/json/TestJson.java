package com.icg.api.json;

import com.progbits.api.exception.ApiException;
import com.progbits.api.json.UTF8JsonParser;
import static com.progbits.api.json.UTF8JsonParser.Event.START_ARRAY;
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
                            "MyStrArray": [ "This", "That", "Other" ],
                            "AnotherObject": {
                                "TestVar1": "OtherVal",
                                "TestVar2": "ThisVal"
                            }
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
                        System.out.println("Key Name: " + keyName);
                    }
                    
                    case START_ARRAY -> {
                        System.out.println("In Array");
                    }
                    
                    case START_OBJECT -> {
                        System.out.println("In Array");
                    }

                    case END_ARRAY -> {
                        System.out.println("End Array");
                    }
                    
                    case END_OBJECT -> {
                        System.out.println("End Object");
                    }
                    
                    case VALUE_STRING -> {
                        System.out.println(keyName + ":" + json.getString());
                    }

                    case VALUE_TRUE -> {
                        System.out.println(keyName + ": true");
                    }

                    case VALUE_NUMBER_INT -> {
                        System.out.println(keyName + ": " + json.getInt());
                    }

                    case VALUE_NUMBER_FLOAT -> {
                        System.out.println(keyName + ": " + json.getDouble());
                    }

                    default -> {
                        // Nothing to do
                    }
                }
            }
        } catch (ApiException ex) {

        }
    }
}
