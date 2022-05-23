package locations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LocationsControllerWebClientIT {

    @Autowired
    WebTestClient webClient;

    @Autowired
    LocationsService service;

    @BeforeEach
    void setUp() {
        service.deleteAllLocations();
        webClient
                .post()
                .uri("/api/locations")
                .bodyValue(new CreateLocationCommand("Budapest", 47.497912, 19.040235))
                .exchange();
        webClient
                .post()
                .uri("/api/locations")
                .bodyValue(new CreateLocationCommand("Róma", 41.90383, 12.50557))
                .exchange();
        webClient
                .post()
                .uri("/api/locations")
                .bodyValue(new CreateLocationCommand("Athén", 37.97954, 23.72638))
                .exchange();
    }

    @Test
    void testGetLocations() {
        webClient
                .get()
                .uri("/api/locations")
                .exchange()
                .expectBodyList(LocationDto.class)
                .hasSize(3)
                .contains(new LocationDto(1L, "Budapest", 47.497912, 19.040235))
                .contains(new LocationDto(2L, "Róma", 41.90383, 12.50557))
                .contains(new LocationDto(3L, "Athén", 37.97954, 23.72638));
    }

    @Test
    void testFindLocationById() {
        webClient
                .get()
                .uri("/api/locations/{id}", 2)
                .exchange()
                .expectBody(LocationDto.class)
                .value(location -> assertEquals("Róma", location.getName()));
    }

    @Test
    void testGetLocationsByName() {
        webClient
                .post()
                .uri("/api/locations")
                .bodyValue(new CreateLocationCommand("Bécs", 48.2083, 16.3731))
                .exchange()
                .expectStatus().isCreated();
        webClient
                .get()
                .uri(builder -> builder.path("/api/locations").queryParam("prefix", "B").build())
                .exchange()
                .expectBodyList(LocationDto.class)
                .hasSize(2)
                .contains(new LocationDto(1L, "Budapest", 47.497912, 19.040235))
                .contains(new LocationDto(4L, "Bécs", 48.2083, 16.3731));
    }

    @Test
    void testUpdateLocation() {
        webClient
                .put()
                .uri("/api/locations/{id}", 2)
                .bodyValue(new UpdateLocationCommand("Unknown", 2.2, 3.3))
                .exchange()
                .expectStatus().isOk();
        webClient
                .get()
                .uri("/api/locations/{id}", 2)
                .exchange()
                .expectBody(LocationDto.class)
                .value(location -> assertEquals("Unknown", location.getName()))
                .value(location -> assertEquals(2.2, location.getLatitude()))
                .value(location -> assertEquals(3.3, location.getLongitude()));
    }

    @Test
    void testDeleteLocation() {
        webClient
                .delete()
                .uri("/api/locations/{id}", 2)
                .exchange()
                .expectStatus().isNoContent();
        webClient
                .get()
                .uri("/api/locations/{id}", 2)
                .exchange()
                .expectStatus().isNotFound();
        webClient
                .get()
                .uri("/api/locations")
                .exchange()
                .expectBodyList(LocationDto.class)
                .hasSize(2)
                .contains(new LocationDto(1L, "Budapest", 47.497912, 19.040235))
                .contains(new LocationDto(3L, "Athén", 37.97954, 23.72638));
    }

    // Validáció
    @Test
    void testCreateLocationWithNotValidNameLatitudeAndLongitude() {
        webClient
                .post()
                .uri("/api/locations")
                .bodyValue(new CreateLocationCommand("", -500, 500))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testUpdateLocationWithNotValidNameLatitudeAndLongitude() {
        webClient
                .put()
                .uri("/api/locations/{id}", 2)
                .bodyValue(new UpdateLocationCommand("", -500, 500))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
