package br.com.deroldo.aws.cloudformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    private static final String[] TO_REPLACE = {"Conditions"};

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            String path = Main.class.getClassLoader().getResource("teste.yml").getPath();
            Map<String, Map> userMap = mapper.readValue(new File(path), Map.class);

            userMap.keySet().forEach(resourceName -> {
                String templateName = Main.class.getClassLoader().getResource(resourceName + ".yml").getPath();
                try {
                    Map<String, Map> templateMap = mapper.readValue(new File(templateName), Map.class);

                    Arrays.asList(TO_REPLACE).forEach(toReplace -> {
                        Map<String, Object> userParamMap = userMap.get(resourceName);
                        userParamMap.keySet().forEach(resourceParamName -> {
                            replaceMap(resourceParamName, userParamMap.get(resourceParamName), templateMap.get(toReplace), new ArrayList<>(), templateMap.get(toReplace));
                        });

                        Map<String, Object> templateDefaultParams = templateMap.get("Parameters");
                        templateDefaultParams.keySet().forEach(templateParamName -> {
                            Map<String, Map> param = (Map<String, Map>) templateDefaultParams.get(templateParamName);
                            if (Objects.nonNull(param.get("Default"))){
                                replaceMap(templateParamName, param.get("Default"), templateMap.get(toReplace), new ArrayList<>(), templateMap.get(toReplace));
                            }
                        });
                    });

                    templateMap.remove("Parameters");

                    System.out.println(mapper.writeValueAsString(templateMap));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void replaceMap(String resourceParamName, Object value, Map template, List<String> breadcrumb, Map root) {
        template.keySet().forEach(resourceFieldName -> {
            if (template.get(resourceFieldName) instanceof Map) {
                breadcrumb.add((String) resourceFieldName);
                replaceMap(resourceParamName, value, (Map) template.get(resourceFieldName), breadcrumb, root);
                breadcrumb.remove(breadcrumb.size() - 1);
            } else if (template.get(resourceFieldName) instanceof List){
                itera(resourceParamName, value, (List) template.get(resourceFieldName), breadcrumb, root, resourceFieldName);
            } else {
                replace(resourceFieldName, template, resourceParamName, value, breadcrumb, root);
            }
        });
    }

    private static void itera(String resourceParamName, Object value, List list, List<String> breadcrumb, Map root, Object resourceFieldName) {
        for (int i = list.size(); i > 0; i--){
            if (list.get(i - 1) instanceof Map){
                replaceMap(resourceParamName, value, (Map) list.get(i - 1), new ArrayList<>(), (Map) list.get(i - 1));
            } else if (list.get(i - 1) instanceof List){
                itera(resourceParamName, value, (List) list.get(i - 1), breadcrumb, root, resourceFieldName);
            }
        }
    }

    private static void replace(Object resourceFieldName, Map template, String resourceParamName, Object value, List<String> breadcrumb, Map root) {
        if (resourceFieldName.equals("Ref") && template.get(resourceFieldName).equals(resourceParamName)){
            Map map = root;
            if (breadcrumb.isEmpty()){
                map.put("kkkkkkkk", value);
            } else {
                List<String> strings = breadcrumb.subList(0, breadcrumb.size() - 1);
                for (String key : strings) {
                    map = (Map) map.get(key);
                }
                map.put(breadcrumb.stream().reduce((s1, s2) -> s2).get(), value);
            }
        }
    }

}
