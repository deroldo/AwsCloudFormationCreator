package br.com.deroldo.aws.cloudformation.replace;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonTypeTest {

    @Test
    public void just_to_coverage(){
        JsonType jsonType = JsonType.COMMA_DELIMITED_LIST;
        assertEquals(jsonType, JsonType.valueOf(jsonType.name()));
        assertEquals(jsonType, JsonType.get(jsonType.getTypeName()));
    }

}