package br.com.deroldo.aws.cloudformation.userdata;

import com.amazonaws.services.cloudformation.model.Capability;

import java.util.List;

public class YmlData {

    private String awsYml;
    private List<Capability> capabilities;

    public YmlData(String awsYml, List<Capability> capabilities) {
        this.awsYml = awsYml;
        this.capabilities = capabilities;
    }

    public String getAwsYml() {
        return awsYml;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }
}
