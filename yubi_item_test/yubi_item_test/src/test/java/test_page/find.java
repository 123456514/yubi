package test_page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class find extends commonDriver{
    public static ChromeDriver driver =  getConnection();
    public static WebElement findUserAccount(){
        return driver.findElement(By.cssSelector("#userAccount"));
    }
    public static WebElement findPassword(){
        return driver.findElement(By.cssSelector("#userPassword"));
    }
    public static WebElement findCheck(){
        return driver.findElement(By.cssSelector("#checkPassword"));
    }
    public static WebElement findButton(){
        return driver.findElement(By.cssSelector("#root > div > div > div > div.ant-pro-form-login-main > form > button > span"));
    }
}
