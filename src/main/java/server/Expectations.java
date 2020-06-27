package server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Employee;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mockserver.client.MockServerClient;

import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static server.Data.*;

public class Expectations extends RunServer {

    static ObjectMapper mapper = new ObjectMapper();
    static MockServerClient mockServerClient = new MockServerClient(conf.address(), conf.port());

    public static void ping() {
        mockServerClient
                .when(
                        request().withMethod("GET").withPath("/ping"))
                .respond(
                        response().withStatusCode(200));
    }

    public static void getEmployees() throws JsonProcessingException {
        mockServerClient
                .when(
                        request().withMethod("GET").withPath("/employees"))
                .respond(
                        response().withStatusCode(200).withBody(mapper.writeValueAsString(getComp())));
    }

    public static void addEmployee() {
        mockServerClient
                .when(
                        request().withMethod("PUT").withPath("/employees/add"))
                .respond(
                        httpRequest -> {
                            // Deserialize from String in request body to POJO
                            Employee newEmpl = mapper.readValue((String) httpRequest.getBody().getValue(), Employee.class);
                            // Checks that Name is not empty, Title is not numeric
                            if (!newEmpl.getName().isEmpty() && newEmpl.getName().trim().length() > 0 &&
                                    !NumberUtils.isCreatable(newEmpl.getTitle())) {
                                addEmpl(newEmpl);
                                return response().withStatusCode(201).withBody(mapper.writeValueAsString(getComp()));
                            } else {
                                return response().withStatusCode(400).withBody(mapper.writeValueAsString(getComp()));
                            }
                        }
                );
    }

    public static void delEmployee() {
        mockServerClient
                .when(
                        request().withMethod("POST").withPath("/employees/delete").withHeaders(header("delete.*")))
                .respond(
                        httpRequest -> {
                            // Filter out the 'delete header' and get value
                            String value = httpRequest.getHeaderList().stream()
                                    .filter(x -> x.getName().toString().contains("delete"))
                                    .findFirst().get().getValues().get(0).toString();

                            // Check if number or text
                            if(NumberUtils.isCreatable(value)) {
                                delEmpl(NumberUtils.toInt(value));
                                return response().withBody("Employee deleted");
                            } else if(!value.isEmpty()) {
                                if(delEmplName(value))
                                    return response().withBody("Employee deleted");
                                else
                                    return response().withBody("Employee not found");
                            } else {
                                return response().withBody("Employee not found");
                            }
                        }
                );
    }

    public static void random() {
        mockServerClient
                .when(
                        request().withMethod("GET").withPath("/random"))
                .respond(
                        request -> {
                            if(RandomUtils.nextBoolean())
                                return response().withStatusCode(200);
                            else
                                return response().withStatusCode(500);
                        });
    }
}
