package cn.luoyanze.mocktest.parser;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/11 12:02 AM
 */


public class FileTest {

    @Test
    public void files() {
        File users = new File("/Users/luoyanze");
        for (String s : users.list()) {
            System.out.println(s);
        }
    }

    @Test
    public void walkfiles() throws IOException {
        Path path = Paths.get("/Users/luoyanze/Documents/Projects/MockGen");
        Stream<Path> walk = Files.walk(path, FileVisitOption.FOLLOW_LINKS);
        walk.forEach(it -> System.out.println(it));
        //Files.walkFileTree(path, )
    }
}
