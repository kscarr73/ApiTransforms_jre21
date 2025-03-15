package com.icg.api.parser;

import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiObject;
import com.progbits.api.parser.Json2ObjectParser;
import java.io.StringReader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class Json2Tester {

    Json2ObjectParser _parser = new Json2ObjectParser(true);

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

    @BeforeClass
    public void runUp() {
        for (var x = 0; x < 100; x++) {
            runTest();
        }
    }
    
    @Test
    public void runTest() {
        try {
            ApiObject json = _parser.parseSingle(new StringReader(strJsonTest));
            
            assert json != null;
        } catch (ApiException | ApiClassNotFoundException apiEx) {
            // Nothing to report
        }
    }
    
    @Test
    public void run100() {
        for (var x = 0; x<100; x++) {
            runTest();
        }
    }
    
    @Test
    public void run1000() {
        for (var x = 0; x<1000; x++) {
            runTest();
        }
    }
    
    @Test
    public void run10000() {
        for (var x = 0; x<10000; x++) {
            runTest();
        }
    }
    
    @Test
    public void run100000() {
        for (var x = 0; x<100000; x++) {
            runTest();
        }
    }
    
    @Test
    public void run1000000() {
        for (var x = 0; x<1000000; x++) {
            runTest();
        }
    }
}
