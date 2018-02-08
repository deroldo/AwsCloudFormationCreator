package br.com.deroldo.aws.cloudformation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static final String[] TO_REPLACE = {"Conditions"};

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {

            final ObjectReader r = mapper.readerFor(Map.class);

            String path = Main.class.getClassLoader().getResource("teste.yml").getPath();
            Map<String, Map> userMap = r.readValue(new File(path));

            userMap.keySet().forEach(resourceName -> {
                String templateName = Main.class.getClassLoader().getResource(resourceName + ".yml").getPath();
                try {

                    String[] templateYml = {Files.readAllLines(Paths.get(templateName)).stream()
                            .reduce((s1, s2) -> s1.concat("\n").concat(s2)).get()};

                    Map<String, Object> toReplace = new HashMap<>();

                    Map<String, Object> userParamMap = userMap.get(resourceName);
                    userParamMap.keySet().forEach(resourceParamName -> {
                        if (userParamMap.get(resourceParamName) instanceof List){
                            final String listValues = ((List<Map>) userParamMap.get(resourceParamName)).stream()
                                    .map(l -> "- " + l.keySet().iterator().next().toString() + ": " + l.values().iterator()
                                            .next().toString())
                                    .reduce((s1, s2) -> s1.concat("\n").concat(s2))
                                    .get();

                            final String key = UUID.randomUUID().toString();
                            toReplace.put(key, listValues);

                            templateYml[0] = templateYml[0].replace("Ref: " + resourceParamName, key);
                        } else {
                            templateYml[0] = templateYml[0].replace("Ref: " + resourceParamName, userParamMap.get(resourceParamName).toString());
                        }
                    });

                    Map<String, Map> templateMap = r.readValue(new File(templateName));
                    Map<String, Object> templateDefaultParams = templateMap.get("Parameters");
                    templateDefaultParams.keySet().forEach(templateParamName -> {
                        Map<String, Object> param = (Map<String, Object>) templateDefaultParams.get(templateParamName);
                        if (Objects.nonNull(param.get("Default"))){
                            templateYml[0] = templateYml[0].replace("Ref: " + templateParamName, param.get("Default").toString());
                        }
                    });

                    toReplace.forEach((k, v) -> {
                        final int index = templateYml[0].indexOf(k);
                        final int enterIndex = templateYml[0].substring(0, index).lastIndexOf("\n");
                        final int diff = templateYml[0].substring(enterIndex, index).replace(" ", "").length();
                        final int qdtEspaco = index - enterIndex - diff;

                        String espacos = "";
                        for (int i = 0; i < qdtEspaco; i++){
                            espacos += " ";
                        }

                        templateYml[0] = templateYml[0].replace(k, v.toString().replace("\n", "\n" + espacos));
                    });
                    templateMap = r.readValue(templateYml[0]);
//                    templateMap = mapper.readValue(templateYml[0], Map.class);

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
