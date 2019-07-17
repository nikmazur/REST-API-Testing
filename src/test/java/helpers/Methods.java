package helpers;

import com.github.javafaker.Faker;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import springrest.Application;
import springrest.Employee;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;

//Listener for attaching request & response logs to Allure
@Listeners(LogListener.class)
public class Methods {

    public static Faker faker;

    @BeforeSuite
    @Step("Launch the Spring REST server")
    public void launchServer() throws IOException {
        Application.main(new String[]{""});
        faker = new Faker();

        Properties prop = new Properties();
        Reader propReader = Files.newBufferedReader(Paths.get("src/main/resources/application.properties"));
        prop.load(propReader);
        RestAssured.baseURI = "http://" + prop.getProperty("address") + ":" + prop.getProperty("server.port");
    }

    @AfterSuite
    @Step("Generate new Employees for the next run")
    public void generateEmployees() throws IOException {

        for(int i = 1; i < 4; ++i) {
            Employee empl = genNewEmpl();
            Properties emplProp = new Properties();

            emplProp.setProperty("ID", String.valueOf(empl.getId()));
            emplProp.setProperty("Name", empl.getName());
            emplProp.setProperty("Title", empl.getTitle());
            emplProp.setProperty("Age", String.valueOf(empl.getAge()));

            File file = new File("randomEmployees/Empl" + i + ".properties");
            FileWriter writer = new FileWriter(file);
            emplProp.store(writer,"Employee Data");
            writer.close();
        }
    }

    @Step("Generate new Employee with random data")
    public Employee genNewEmpl() {
        Employee empl = new Employee
                (RandomUtils.nextInt(1000, 10000), faker.name().fullName(), faker.company().profession(), RandomUtils.nextInt(18, 80));
        Allure.addAttachment("New Employee Data", empl.toString());
        return empl;
    }

    public static RequestSpecification mainRequest() {
        return given().baseUri(RestAssured.baseURI).contentType(ContentType.JSON).accept(ContentType.JSON);
    }

    @Step("Get server status")
    public static int getStatus(String path) {
        return mainRequest().basePath(path).get().getStatusCode();
    }

    @Step("Retrieve all employees, return as List")
    public static List<Employee> getEmployees() {
        return Arrays.asList(mainRequest().basePath("/employees").get().then().assertThat().statusCode(200).extract().as(Employee[].class));
    }

    @Step("Add new employee, return new list of employees")
    public static List<Employee> addEmployee(Employee empl) {
        return Arrays.asList(mainRequest().basePath("/employees/add").body(empl).post()
                .then().assertThat().statusCode(200).extract().as(Employee[].class));
    }

    @Step("Request to delete an employee (by Name or Index)")
    public static String delEmployee(String type, Object id) {
        return mainRequest().basePath("/employees/delete").queryParam(type, id).post()
                .then().assertThat().statusCode(200).extract().asString();
    }

    //Data provider used in the 'checkEmployee' test
    @DataProvider
    public Object[][] getEmpl()
    {
        Object[][] data = new Object[3][2];

        for(int i = 1; i < 4; ++i) {
            Properties prop = new Properties();
            try {
                Reader propReader = Files.newBufferedReader(Paths.get("randomEmployees/Empl" + i + ".properties"));
                prop.load(propReader);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            data[i-1][0] = Integer.parseInt(prop.getProperty("ID"));
            data[i-1][1] = prop.getProperty("Name");
        }

        return data;
    }

}
