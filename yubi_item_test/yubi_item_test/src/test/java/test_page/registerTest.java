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


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class registerTest extends commonDriver {
    public static ChromeDriver driver =  getConnection();
    @BeforeAll
    public static void open(){
        driver.get("http://124.223.222.249");
    }
    //判断此时的页面是否加载成功
    @Order(1)
    @Test
    public void goodPage(){
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div[2]/form/div[4]/a")).click();
    }
    /**
     * 冒烟测试 正常通过
     */
    @Order(2)
    @ParameterizedTest
    @CsvSource(value = {"wangwu10,12345678,12345678"})
    public void register(String userAccount,String password ,String checkPassword) throws InterruptedException {
        findUserAccount().sendKeys(userAccount);
        findPassword().sendKeys(password);
        findCheck().sendKeys(checkPassword);
        findButton().click();
        Thread.sleep(3000);
        String windowHandle = driver.getWindowHandle();
        Set<String> windowHandles = driver.getWindowHandles();
        for(String window : windowHandles){
            //在set中遍历到 和登录页面不相符的 标识 那么此时就是主页的标识 ，直接跳转
            if(window .equals(windowHandle)){
                driver.switchTo().window(window);
                break;
            }
        }
        Thread.sleep(3000);
        Assertions.assertEquals(driver.getCurrentUrl(),"http://124.223.222.249/user/login");
        //返回登录页面
        driver.findElement(By.cssSelector("#root > div > div > div > div.ant-pro-form-login-main > form > div:nth-child(5) > a")).click();
    }

    /**
     * 异常测试 为空时
     */

    @Order(3)
    @ParameterizedTest
    @CsvSource(value = {"'','',''","lisi,'12345678','123456789'","lisi,'','123456789'","'','12345678','123456789'","lisi,12345678,''"})
    public void failPwdNotEqualCheckPwd(String userAccount,String password,String checkPassword) throws InterruptedException {
        findUserAccount().sendKeys(userAccount);
        findPassword().sendKeys(password);
        findCheck().sendKeys(checkPassword);
        findButton().click();
        Assertions.assertEquals(driver.getCurrentUrl(),"http://124.223.222.249/user/register");
        Thread.sleep(3000);
        findUserAccount().sendKeys(Keys.CONTROL, "A");
        findUserAccount().sendKeys(Keys.CONTROL,"X");
        findPassword().sendKeys(Keys.CONTROL, "A");
        findPassword().sendKeys(Keys.BACK_SPACE);
        findCheck().sendKeys(Keys.CONTROL, "A");
        findCheck().sendKeys(Keys.BACK_SPACE);
    }
    @AfterAll
    public static void end(){
        driver.quit();
    }



}
