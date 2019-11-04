package dev.fervento.assertbydebug.sample.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    double ACCURACY = 1e-6;

    Car c1;
    Car c2;
    Person father;
    Person child;

    @BeforeEach
    public void before() {
        c1 = new Car("EZ91JAVA").put("Vendor", "Miat").put("Value", 301.334f);
        c2 = new Car("X001010").put("Value", 3.1416);

        father = new Person(UUID.fromString("3bb546e9-a95a-4e83-aa43-5088d522294b"), 10L, "Giulio", "Rossi", 35, Instant.EPOCH, Person.Gender.MALE, null)
                        .addCar(c1)
                        .addCar(c2);

        child = new Person(UUID.fromString("7de8adff-2219-4361-8373-dfaa411ed9f7"), 14L, "Paola", "Rossi", 35, Instant.ofEpochMilli(101), Person.Gender.FEMALE, father)
                .addCar(c1);
    }

    private String getOutcome() {
        try {
            return (String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException|IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testJUnit() {
        System.out.println("Set breakpoint, copy as JUNIT assertion child");
        String expectedValue = "assertEquals(\"7de8adff-2219-4361-8373-dfaa411ed9f7\", child.getUuid().toString());\n" +
                "assertEquals(14, child.getInsuranceId());\n" +
                "assertEquals(\"Paola\", child.getName());\n" +
                "assertEquals(\"Rossi\", child.getSurname());\n" +
                "assertEquals(35, child.getAge());\n" +
                "assertEquals(\"1970-01-01T00:00:00.101Z\", child.getLastAccess().toString());\n" +
                "assertEquals(dev.fervento.assertbydebug.sample.entity.Person.Gender.FEMALE, child.getGender());\n" +
                "\n" +
                "Object[] collection = child.getCars().toArray();\n" +
                "assertEquals(1, collection.length);\n" +
                "dev.fervento.assertbydebug.sample.entity.Car item = ((dev.fervento.assertbydebug.sample.entity.Car)collection[0]);\n" +
                "assertEquals(\"EZ91JAVA\", item.getPlateNumber());\n" +
                "assertEquals(301.334015, ((java.lang.Float)((java.util.HashMap)item.getAdditionalData()).get(\"Value\")), ACCURACY);\n" +
                "assertEquals(\"Miat\", ((java.lang.String)((java.util.HashMap)item.getAdditionalData()).get(\"Vendor\")));\n" +
                "\n" +
                "assertEquals(\"3bb546e9-a95a-4e83-aa43-5088d522294b\", child.getFather().getUuid().toString());\n" +
                "assertEquals(10, child.getFather().getInsuranceId());\n" +
                "assertEquals(\"Giulio\", child.getFather().getName());\n" +
                "assertSame(child.getSurname(), child.getFather().getSurname());\n" +
                "assertEquals(35, child.getFather().getAge());\n" +
                "assertEquals(\"1970-01-01T00:00:00Z\", child.getFather().getLastAccess().toString());\n" +
                "assertEquals(dev.fervento.assertbydebug.sample.entity.Person.Gender.MALE, child.getFather().getGender());\n" +
                "\n" +
                "Object[] collection_2 = child.getFather().getCars().toArray();\n" +
                "assertEquals(2, collection_2.length);\n" +
                "assertSame(item, ((dev.fervento.assertbydebug.sample.entity.Car)child.getFather().getCars().toArray()[0]));\n" +
                "dev.fervento.assertbydebug.sample.entity.Car item_2 = ((dev.fervento.assertbydebug.sample.entity.Car)collection_2[1]);\n" +
                "assertEquals(\"X001010\", item_2.getPlateNumber());\n" +
                "assertEquals(3.141600, ((java.lang.Double)((java.util.HashMap)item_2.getAdditionalData()).get(\"Value\")), ACCURACY);\n" +
                "\n" +
                "assertNull(child.getFather().getFather());\n";

        assertEquals(expectedValue, getOutcome());

        assertEquals("7de8adff-2219-4361-8373-dfaa411ed9f7", child.getUuid().toString());
        assertEquals(14, child.getInsuranceId());
        assertEquals("Paola", child.getName());
        assertEquals("Rossi", child.getSurname());
        assertEquals(35, child.getAge());
        assertEquals("1970-01-01T00:00:00.101Z", child.getLastAccess().toString());
        assertEquals(dev.fervento.assertbydebug.sample.entity.Person.Gender.FEMALE, child.getGender());

        Object[] collection = child.getCars().toArray();
        assertEquals(1, collection.length);
        dev.fervento.assertbydebug.sample.entity.Car item = ((dev.fervento.assertbydebug.sample.entity.Car)collection[0]);
        assertEquals("EZ91JAVA", item.getPlateNumber());
        assertEquals(301.334015, ((java.lang.Float)((java.util.HashMap)item.getAdditionalData()).get("Value")), ACCURACY);
        assertEquals("Miat", ((java.lang.String)((java.util.HashMap)item.getAdditionalData()).get("Vendor")));

        assertEquals("3bb546e9-a95a-4e83-aa43-5088d522294b", child.getFather().getUuid().toString());
        assertEquals(10, child.getFather().getInsuranceId());
        assertEquals("Giulio", child.getFather().getName());
        assertSame(child.getSurname(), child.getFather().getSurname());
        assertEquals(35, child.getFather().getAge());
        assertEquals("1970-01-01T00:00:00Z", child.getFather().getLastAccess().toString());
        assertEquals(dev.fervento.assertbydebug.sample.entity.Person.Gender.MALE, child.getFather().getGender());

        Object[] collection_2 = child.getFather().getCars().toArray();
        assertEquals(2, collection_2.length);
        assertSame(item, ((dev.fervento.assertbydebug.sample.entity.Car)child.getFather().getCars().toArray()[0]));
        dev.fervento.assertbydebug.sample.entity.Car item_2 = ((dev.fervento.assertbydebug.sample.entity.Car)collection_2[1]);
        assertEquals("X001010", item_2.getPlateNumber());
        assertEquals(3.141600, ((java.lang.Double)((java.util.HashMap)item_2.getAdditionalData()).get("Value")), ACCURACY);

        assertNull(child.getFather().getFather());

        System.out.println(expectedValue);
    }

    @Test
    public void testJSON() {
        System.out.println("Set breakpoint, copy as JSON child");
        String expectedValue = "{\n" +
                "  \"child\" : {\n" +
                "    \"uuid\" : \"7de8adff-2219-4361-8373-dfaa411ed9f7\",\n" +
                "    \"insuranceId\" : 14,\n" +
                "    \"name\" : \"Paola\",\n" +
                "    \"surname\" : \"Rossi\",\n" +
                "    \"age\" : 35,\n" +
                "    \"lastAccess\" : \"1970-01-01T00:00:00.101Z\",\n" +
                "    \"gender\" : \"FEMALE\",\n" +
                "    \"cars\" : [ {\n" +
                "      \"plateNumber\" : \"EZ91JAVA\",\n" +
                "      \"additionalData\" : {\n" +
                "        \"Value\" : 301.3340148925781,\n" +
                "        \"Vendor\" : \"Miat\"\n" +
                "      }\n" +
                "    } ],\n" +
                "    \"father\" : {\n" +
                "      \"uuid\" : \"3bb546e9-a95a-4e83-aa43-5088d522294b\",\n" +
                "      \"insuranceId\" : 10,\n" +
                "      \"name\" : \"Giulio\",\n" +
                "      \"surname\" : \"Rossi\",\n" +
                "      \"age\" : 35,\n" +
                "      \"lastAccess\" : \"1970-01-01T00:00:00Z\",\n" +
                "      \"gender\" : \"MALE\",\n" +
                "      \"cars\" : [ {\n" +
                "        \"plateNumber\" : \"EZ91JAVA\",\n" +
                "        \"additionalData\" : {\n" +
                "          \"Value\" : 301.3340148925781,\n" +
                "          \"Vendor\" : \"Miat\"\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"plateNumber\" : \"X001010\",\n" +
                "        \"additionalData\" : {\n" +
                "          \"Value\" : 3.1416\n" +
                "        }\n" +
                "      } ],\n" +
                "      \"father\" : null\n" +
                "    }\n" +
                "  }\n" +
                "}";

        assertEquals(expectedValue, getOutcome());
        System.out.println(expectedValue);
    }

    @Test
    public void testJSOG() {
        System.out.println("Set breakpoint, copy as JSON child");
        String expectedValue = "{\n" +
                "  \"child\" : {\n" +
                "    \"@id\" : \"1735\",\n" +
                "    \"uuid\" : \"7de8adff-2219-4361-8373-dfaa411ed9f7\",\n" +
                "    \"insuranceId\" : 14,\n" +
                "    \"name\" : \"Paola\",\n" +
                "    \"surname\" : \"Rossi\",\n" +
                "    \"age\" : 35,\n" +
                "    \"lastAccess\" : \"1970-01-01T00:00:00.101Z\",\n" +
                "    \"gender\" : \"FEMALE\",\n" +
                "    \"cars\" : [ {\n" +
                "      \"@id\" : \"1732\",\n" +
                "      \"plateNumber\" : \"EZ91JAVA\",\n" +
                "      \"additionalData\" : {\n" +
                "        \"@id\" : \"1839\",\n" +
                "        \"Value\" : 301.3340148925781,\n" +
                "        \"Vendor\" : \"Miat\"\n" +
                "      }\n" +
                "    } ],\n" +
                "    \"father\" : {\n" +
                "      \"@id\" : \"1734\",\n" +
                "      \"uuid\" : \"3bb546e9-a95a-4e83-aa43-5088d522294b\",\n" +
                "      \"insuranceId\" : 10,\n" +
                "      \"name\" : \"Giulio\",\n" +
                "      \"surname\" : \"Rossi\",\n" +
                "      \"age\" : 35,\n" +
                "      \"lastAccess\" : \"1970-01-01T00:00:00Z\",\n" +
                "      \"gender\" : \"MALE\",\n" +
                "      \"cars\" : [ {\n" +
                "        \"@ref\" : \"1732\"\n" +
                "      }, {\n" +
                "        \"@id\" : \"1733\",\n" +
                "        \"plateNumber\" : \"X001010\",\n" +
                "        \"additionalData\" : {\n" +
                "          \"@id\" : \"1859\",\n" +
                "          \"Value\" : 3.1416\n" +
                "        }\n" +
                "      } ],\n" +
                "      \"father\" : null\n" +
                "    }\n" +
                "  }\n" +
                "}";

        assertEquals(expectedValue, getOutcome());
        System.out.println(expectedValue);
    }


}