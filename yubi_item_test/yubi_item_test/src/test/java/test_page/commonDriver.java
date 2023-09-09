package test_page;

import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

public class commonDriver {
    public static ChromeDriver chromeDriver;
    public static ChromeDriver getConnection(){
        if(chromeDriver == null){
            chromeDriver = new ChromeDriver();
        }
        chromeDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(40));
        return chromeDriver;
    }
}

