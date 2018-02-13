package br.com.deroldo.aws.cloudformation.publish;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TemplateCapabilityTest {

    @Test
    public void just_to_coverage(){
        TemplateCapability templateCapability = TemplateCapability.CLUSTER_ECS;
        assertEquals(templateCapability, TemplateCapability.valueOf(templateCapability.name()));
        assertEquals(templateCapability, TemplateCapability.get(templateCapability.getTemplateName()));
    }

}