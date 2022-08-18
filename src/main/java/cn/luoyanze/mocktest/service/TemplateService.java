package cn.luoyanze.mocktest.service;

import cn.luoyanze.mocktest.parser.model.SimpleJavaSource;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/8 6:51 PM
 */


public class TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);

    static final String template;
    static {
        InputStream resourceAsStream = TemplateService.class.getResourceAsStream("/template/JavaTemplate.ftl");
        String ctx = "";
        try {
            if (resourceAsStream != null) {
                ctx = new String(resourceAsStream.readAllBytes());
                resourceAsStream.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        template = ctx;

    }

    private static Map<String, Object> getDataMap(SimpleJavaSource source, String testClassname) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("package_name", source.getPackageName());
        data.put("class_name", source.getClassname());
        data.put("test_class_name", testClassname);

        Set<String> prepareForTest = source.UTIL.getPrepareForTest();
        data.put("prepare_for_tests", prepareForTest);

        Set<String> suppresses =
                source.getImportMaps().entrySet().stream()
                        .filter(it -> prepareForTest.contains(it.getKey()))
                        .map(it -> it.getValue() + "." + it.getKey())
                        .collect(Collectors.toSet());
        data.put("suppresses", suppresses);

        data.put("mocks", source.UTIL.getMockFields());
        data.put("mock_statics", source.UTIL.getMockStatics());

        data.put("useSlf4j", source.UTIL.isUseSlf4j());
        data.put("has_logger", source.UTIL.hasLogger());
        data.put("constructor_params", source.UTIL.getConstructorParamsForTest());
        data.put("fields_not_in_constructor", source.UTIL.getFieldsNotInConstructor());

        data.put("imports", source.UTIL.getImports());

        return data;
    }

    public static String generateTemplate(SimpleJavaSource source, String testClassname) throws IOException, TemplateException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();

        stringTemplateLoader.putTemplate("JavaTemplate", template);
        configuration.setTemplateLoader(stringTemplateLoader);

        Map<String, Object> data = getDataMap(source, testClassname);

        Template template = configuration.getTemplate("JavaTemplate");
        StringWriter out = new StringWriter();
        template.process(data, out);

        return out.toString();
    }
}
