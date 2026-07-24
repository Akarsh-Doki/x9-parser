package com.fcrm.fraud.x9parser.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fcrm.fraud.x9parser.config.X9Config.FieldDef;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

// Checks the settings loaded from application.properties instead of being hardcoded
@SpringBootTest
class X9ConfigTest {
    @Autowired
    private X9Config config;

    @Test
    void loadsTheFifteenCheckFields() {
        List<FieldDef> fields= config.getCheckFields();
        assertEquals(15, fields.size());
        assertEquals("Item Amount", config.getCheckFields().get(6).getName());
        assertEquals(48, config.getCheckFields().get(6).getStart());
    }

    @Test
    void loadsTheBigFormatMapping() {
        assertEquals("ECE Institution Item Sequence Number", config.getBigFormatMapping().get("ISN"));
        assertEquals("Item Amount", config.getBigFormatMapping().get("Amount"));
    }
}