package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@ApplicationScoped
@Path("/data")
@RegisterRestClient(configKey = "data-producer-api")
public interface DataProducerService {

    @GET
    @Produces("application/json")
    SensorMeasurement getSensorMeasurement();
}
