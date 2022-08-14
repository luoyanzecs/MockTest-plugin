package cn.luoyanze.mocktest.parser;

import cn.luoyanze.mocktest.parser.model.java.ClassMap;
import org.junit.Test;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/11 12:02 AM
 */


public class FileTest {

    @Test
    public void files() throws IOException {
        String s = Files.readString(Paths.get("NearbyRestaurantRepositoryImpl.java"));
        String reg = "import[^;\n]*?\\.LoggerFactory(?=[;|\\s|\n])";
        Pattern compile = Pattern.compile(reg);
        Matcher matcher = compile.matcher(s);
        while (matcher.find()) {
            System.out.println(matcher.group());
            System.out.println("------------");
        }
    }

    @Test
    public void test_replace() {
        String s = "${name}${name}";
        System.out.println(s.replace("${name}", "123"));
    }

    @Test
    public void path_test() {
        Path path = Paths.get("/Users/sss/abc.txt");
        boolean sss = path.toString().matches("sss");
        System.out.println(sss);
        System.out.println("+++++++++++++");
        System.out.println(path.compareTo(Paths.get("sss")));
        System.out.println("--------------");
        System.out.println(path.startsWith("sss"));
        System.out.println(path.endsWith("sss"));
        System.out.println(path.resolveSibling("sss"));
        //Path abc = path.resolve(Paths.get("/abc"));
        //path.re
    }

    @Test
    public void test_file_path() {
        File file = new File("src/main/resources/template/JavaTemplate.ftl");
        System.out.println(file.toPath());
        System.out.println(file.getAbsoluteFile().toPath());
        //Path abc = path.resolve(Paths.get("/abc"));
        //path.re
    }

    @Test
    public void walkfiles() throws IOException {
        Path path = Paths.get("/Users/luoyanze/Documents/Projects/MockGen");
        Stream<Path> walk = Files.walk(path, FileVisitOption.FOLLOW_LINKS);
        walk.forEach(it -> System.out.println(it));
        //Files.walkFileTree(path, )
    }

    @Test
    public void test() throws IllegalAccessException, NoSuchFieldException, InstantiationException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        ClassMap test = (ClassMap) unsafe.allocateInstance(ClassMap.class);
        System.out.println(test);


        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator instantiator = objenesis.getInstantiatorOf(ClassMap.class);
        ClassMap date = (ClassMap) instantiator.newInstance();
        String classname = date.getClassname();
        String packageName = date.getPackageName();
    }
}
