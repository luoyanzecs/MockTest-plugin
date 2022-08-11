package cn.luoyanze.mocktest.service;

import cn.luoyanze.mocktest.parser.model.JavaFileV3;
import cn.luoyanze.mocktest.parser.model.java.ClassMap;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/8 6:51 PM
 */


public class TemplateService {

    public static String generateTemplate(JavaFileV3 file) throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();

        stringTemplateLoader.putTemplate("JavaTemplate", new String(TemplateService.class.getResourceAsStream("/template/JavaTemplate.ftl").readAllBytes()));
        configuration.setTemplateLoader(stringTemplateLoader);

        Map<String, Object> data = getDataMap(file);

        Template template = configuration.getTemplate("JavaTemplate");
        StringWriter out = new StringWriter();
        template.process(data, out);

        return out.toString();
    }


    private static Map<String, Object> getDataMap(JavaFileV3 file) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("package_name", file.getPackageName());

        List<Object> mocks = file.getFields().
                stream().map(it -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("classname", it.getClassname());
                    map.put("name", it.getName());
                    return map;
                }).collect(Collectors.toList());

        data.put("mocks", mocks);

        List<String> imports = file.getUseImports().stream().map(ClassMap::toString).sorted().collect(Collectors.toList());
        imports.add(file.getPackageName() + "." + file.getName());

        data.put("imports", imports);
        data.put("useSlf4j", file.isUseSlf4j());
        data.put("prepareForTests", file.getUseImports().stream().map(ClassMap::getClassname).sorted().collect(Collectors.toList()));
        data.put("suppresses", file.getUseImports().stream().map(ClassMap::toString).sorted().collect(Collectors.toList()));
        data.put("test_class_name", file.getTestFilenameForGenerate());
        data.put("class_name", file.getName());
        data.put("mockStatics", file.getCtripStaticMocks());

        return data;
    }
}
