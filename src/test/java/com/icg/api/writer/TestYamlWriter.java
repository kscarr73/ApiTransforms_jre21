package com.icg.api.writer;

import com.progbits.api.model.ApiObject;
import com.progbits.api.parser.YamlObjectParser;
import com.progbits.api.writer.YamlObjectWriter;
import java.io.StringReader;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestYamlWriter {

    YamlObjectWriter objectWriter = new YamlObjectWriter(true);
    YamlObjectParser objectParser = new YamlObjectParser(true);

    @BeforeTest
    public void runUp() throws Exception {
        for (int x = 0; x < 100; x++) {
            testWriteSingle();
        }
    }
    
    @Test
    public void testWriteSingle() throws Exception {

        ApiObject objTest = new ApiObject();

        objTest.setString("startField", "Testing's Fate");
        objTest.setInteger("intTest", 13);
        objTest.setDouble("doubleTest", 12.312);
        objTest.createList("listTest");
        objTest.getListAdd("listTest").setString("field1", "value1").setString("field2", "value2");

        objTest.createList("listTest2");
        objTest.getListAdd("listTest2").setString("field1", "value1").setString("field2", "value2");

        objTest.createObject("objectTest");
        objTest.getObject("objectTest").setString("objectString", "This");

        objTest.createStringArray("myStringArray");
        objTest.getStringArray("myStringArray").add("This' My' Other'");
        objTest.getStringArray("myStringArray").add("That");
        objTest.getStringArray("myStringArray").add("Other");

        objTest.createIntegerArray("myIntegerArray");
        objTest.getIntegerArray("myIntegerArray").add(42);
        objTest.getIntegerArray("myIntegerArray").add(13);
        objTest.getIntegerArray("myIntegerArray").add(7);

        String strTest = objectWriter.writeSingle(objTest);

        assert strTest != null;

        ApiObject objParse = objectParser.parseSingle(new StringReader(strTest));

        assert objParse != null;
    }

    @Test
    public void run100() throws Exception {
        for (int x = 0; x < 100; x++) {
            testWriteSingle();
        }
    }

    @Test
    public void run1000() throws Exception {
        for (int x = 0; x < 1000; x++) {
            testWriteSingle();
        }
    }

    @Test
    public void run10000() throws Exception {
        for (int x = 0; x < 10000; x++) {
            testWriteSingle();
        }
    }

    @Test
    public void run100000() throws Exception {
        for (int x = 0; x < 100000; x++) {
            testWriteSingle();
        }
    }
}
