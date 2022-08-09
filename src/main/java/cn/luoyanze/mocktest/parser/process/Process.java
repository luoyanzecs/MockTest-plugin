package cn.luoyanze.mocktest.parser.process;

import cn.luoyanze.mocktest.parser.model.JavaFileV3;
import cn.luoyanze.mocktest.parser.model.TestSourceMap;
import cn.luoyanze.mocktest.parser.visit.SourceVisitorAdapter;
import cn.luoyanze.mocktest.parser.visit.TestPrepareVisitAdapter;
import cn.luoyanze.mocktest.parser.visit.TestVisitorAdapter;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

/**
 * @Author luoyanze[luoyanzeze@icloud.com]
 * @Date 2022/8/6 11:37 AM
 */


public class Process {

    private TestVisitorAdapter testVisitorAdapter = new TestVisitorAdapter();
    private SourceVisitorAdapter sourceVisitorAdapter = new SourceVisitorAdapter();
    private TestPrepareVisitAdapter testPrepareVisitAdapter = new TestPrepareVisitAdapter();

    public TestSourceMap getMappingFromTestFile(String str) {
        TestSourceMap testSourceMap = new TestSourceMap();
        CompilationUnit parse1 = StaticJavaParser.parse(str);
        testPrepareVisitAdapter.visit(parse1, testSourceMap);
        return testSourceMap;
    }

    public String replaceLoggerToSlf4j(String str) {
        return str
                .replaceAll("import[^;\\n]*?LoggerFactory[;|\\s|\\n]", "import org.slf4j.LoggerFactory")
                .replaceAll("import[^;\\n]*?Logger[;|\\s|\\n]", "import org.slf4j.Logger");
    }

    public String generateTestString(String str, JavaFileV3 file) {
        CompilationUnit parse = StaticJavaParser.parse(str);
        sourceVisitorAdapter.visit(parse, file);
        return parse.toString();
    }


    public String refineTestFile(String str, JavaFileV3 source) {
        CompilationUnit testParse = StaticJavaParser.parse(str);
        testVisitorAdapter.visit(testParse, source);
        return testParse.toString();
    }
}
