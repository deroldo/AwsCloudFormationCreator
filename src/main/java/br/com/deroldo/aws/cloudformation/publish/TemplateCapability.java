package br.com.deroldo.aws.cloudformation.publish;

import com.amazonaws.services.cloudformation.model.Capability;

import java.util.Arrays;

import static java.util.Objects.nonNull;

public enum TemplateCapability {

    CLUSTER_ECS("ClusterEcs", Capability.CAPABILITY_NAMED_IAM),
    DEFAULT(null, null);

    private String templateName;
    private Capability capability;

    TemplateCapability(String templateName, Capability capability) {
        this.templateName = templateName;
        this.capability = capability;
    }

    public static TemplateCapability get(String templateName){
        return Arrays.stream(TemplateCapability.values())
                .filter(tc -> nonNull(tc.getTemplateName()) && tc.getTemplateName().equals(templateName))
                .findFirst()
                .orElse(TemplateCapability.DEFAULT);
    }

    public Capability getCapability() {
        return capability;
    }

    public String getTemplateName() {
        return templateName;
    }
}
