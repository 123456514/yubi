package test_page;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.util.Set;

import static test_page.find.*;


//测试类中的方法安装顺序执行
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//登录页面测试类
public class loginTest  extends commonDriver {
    //创建和浏览器的连接
    private static ChromeDriver driver =  getConnection();
    @BeforeAll
    public static void open(){
        //打开浏览器，并且设置为最大
        driver.get("http://124.223.222.249");
        driver.manage().window().maximize();
    }

    @Test
    @Order(1)
    //校验登录页面元素是否全部出现
    public  void goodPage(){
        //在规定的时间范围内，轮询等待元素出现之后就立即结束，如果在规定的时间内元素仍然没有出现，则会抛出一个noSuchElement异常
        //由于首次打开登录页面，用时较长，此时使用隐式等待，等待页面中一有元素出现，此时就停止等待，一定程度上的节省了时间
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        //用户名
        String username = findUserAccount().getAttribute("placeholder");
        //密码
        String password = findPassword().getAttribute("placeholder");
        // 标题
        String title = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div[1]/div[1]/span[2]")).getText();
        String titleTag = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div[1]/div[2]/a")).getText();
        String loginType = driver.findElement(By.xpath("//*[@id=\"rc-tabs-0-tab-account\"]")).getText();
        String registerLink = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div[2]/form/div[4]/a")).getText();
        String submit = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div[2]/form/button/span")).getText();
        //校验
        Assertions.assertEquals(title,"智能 BI");
        Assertions.assertEquals(titleTag,"致力于“取代”初级数据分析师");
        Assertions.assertEquals(loginType,"账户密码登录");
        Assertions.assertEquals(username,"请输入用户名");
        Assertions.assertEquals(password,"请输入密码");
        Assertions.assertEquals(registerLink,"注册");
        Assertions.assertEquals(submit,"登 录");
    }
    @Order(2)
    @ParameterizedTest  //参数化
    @CsvSource(value = {"zhoujin,12345678"})
    public void loginIsTrue(String  username,String password) throws InterruptedException {
        //得到登录页面的标识
        Thread.sleep(2000);
        String windowHandle = driver.getWindowHandle();

        findUserAccount().sendKeys(username);
        findPassword().sendKeys(password);
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div[2]/form/button/span")).click();
        //得到当前系统页面中所有的标识
        Set<String> windowHandles = driver.getWindowHandles();
        for(String window : windowHandles){
            //在set中遍历到 和登录页面不相符的 标识 那么此时就是主页的标识 ，直接跳转
            if(window .equals(windowHandle)){
                driver.switchTo().window(window);
                break;
            }
        }
        Thread.sleep(3000);
        Assertions.assertEquals(driver.getCurrentUrl(),"http://124.223.222.249/add_chart");
        Thread.sleep(3000);
        //返回登录页面
        driver.navigate().back();
    }


    /**
     * 异常测试   用户名为空
     */
    @Order(3)
    @ParameterizedTest
    @CsvSource(value = {"'',''",
            "123456789,''",
            "'',123456789",
            "12345678910,123456789"
        }
    )
    public void ErrorLoginUserIsNull(String username,String password) throws InterruptedException {
        Thread.sleep(3000);
        driver.findElement(By.xpath("//*[@id=\"userAccount\"]")).sendKeys(username);
        driver.findElement(By.xpath("//*[@id=\"userPassword\"]")).sendKeys(password);
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div[2]/form/button/span")).click();
        Assertions.assertEquals(driver.getCurrentUrl(),"http://124.223.222.249/user/login");
        findUserAccount().sendKeys(Keys.CONTROL, "A");
        findUserAccount().sendKeys(Keys.CONTROL,"X");
        findPassword().sendKeys(Keys.CONTROL, "A");
        findPassword().sendKeys(Keys.BACK_SPACE);
    }

    @AfterAll
    public static void end(){
        driver.quit();
    }


}

