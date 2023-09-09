package test_page;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
//使用测试套件，可以批量的执行测试脚本
@Suite
@SelectClasses({registerTest.class,loginTest.class})
public class autoSuite {

}