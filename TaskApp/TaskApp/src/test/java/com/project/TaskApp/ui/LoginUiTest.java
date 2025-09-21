package com.project.TaskApp.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginUiTest {

    static WebDriver driver;
    static WebDriverWait wait;

    @BeforeAll
    static void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1280, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    private void goToLogin() {
        driver.get("https://the-internet.herokuapp.com/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
    }

    private String readFlash() {
        WebElement flash = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("flash"))
        );
        // normalize because the banner contains "×" and line breaks
        return flash.getText().replace("×", "").trim();
    }

    @Test @Order(1)
    void login_success_showsSecureArea() {
        goToLogin();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys("tomsmith");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("SuperSecretPassword!");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String flash = readFlash().toLowerCase();
        assertTrue(flash.contains("you logged into a secure area"));
        assertTrue(driver.getCurrentUrl().contains("/secure"));

        // 🔑 reset state for the next test
        // there’s a Logout button on the secure page
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.button.secondary.radius"))).click();
        // ensure we’re back on the login page
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    @Test @Order(2)
    void login_fail_showsError() {
        goToLogin();
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys("tomsmith");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("wrong");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String flash = readFlash().toLowerCase();
        assertTrue(flash.contains("your password is invalid"));
    }
}
