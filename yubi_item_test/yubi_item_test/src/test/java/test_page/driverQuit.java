package test_page;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import test_page.commonDriver;

public class driverQuit extends commonDriver {
    private static EdgeDriver driver = new EdgeDriver();
    @Test
    public void quit(){
        driver.quit();
    }
}
