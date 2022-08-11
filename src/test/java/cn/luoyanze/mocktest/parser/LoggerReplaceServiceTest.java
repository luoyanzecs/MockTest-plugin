package cn.luoyanze.mocktest.parser;

import cn.luoyanze.mocktest.service.LoggerReplaceService;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/11 1:03 AM
 */


public class LoggerReplaceServiceTest {

    @Test
    public void testPath() {
        Path path = Paths.get("/Users/luoyanze/Documents/Projects/document-manager/document-manager-common/src/main/java/cn/luoyanze/documentmanager/common/util/SpringUtils.java");
        boolean java = path.toString().endsWith("java");
        System.out.println(java);
    }

    @Test
    public void replaceSingele() throws IOException {
        Stream<Path> walk = Files.walk(Paths.get("/Users/luoyanze/Documents/Projects/document-manager/document-manager-common"), FileVisitOption.FOLLOW_LINKS);
        Set<Path> collect = walk.collect(Collectors.toSet());
        Set<Path> collect1 = collect.stream().filter(it -> !Files.isDirectory(it, LinkOption.NOFOLLOW_LINKS)).collect(Collectors.toSet());
        Set<Path> collect2 = collect1.stream().filter(it -> it.toString().endsWith("java") || it.toString().endsWith("kt")).collect(Collectors.toSet());
        collect2.forEach(this::loggerhandler);
    }

    private void loggerhandler(Path path) {
        try {
            String before = Files.readString(path);
            String after = before.replaceAll("import[^;\\n]*?\\.LoggerFactory(?=[;|\\s|\\n])", "import org.slf4j.LoggerFactory")
                    .replaceAll("import[^;\\n]*?\\.Logger(?=[;|\\s|\\n])", "import org.slf4j.Logger");

            if (before.equals(after)) {
                return;
            }
            Files.writeString(path, after, StandardOpenOption.WRITE);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
