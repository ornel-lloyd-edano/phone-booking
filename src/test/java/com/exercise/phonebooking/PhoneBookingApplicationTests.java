package com.exercise.phonebooking;

import com.exercise.phonebooking.controller.BookPhone;
import com.exercise.phonebooking.controller.PhoneDetails;
import com.exercise.phonebooking.controller.ReturnPhone;
import com.exercise.phonebooking.entity.Booking;
import com.exercise.phonebooking.entity.UserAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PhoneBookingApplicationTests {

	@Value(value="${local.server.port}")
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	LocalDateTime now = LocalDateTime.of(2023, 5, 28, 0, 0);
	Instant instant = now.atZone(ZoneId.of("UTC")).toInstant();;

	@Test
	@Order(1)
	public void getPhoneShouldReturnBookablePhone() {
		PhoneDetails expectedResponse = new PhoneDetails(
			1, "Samsung Galaxy S9", "GSM / CDMA / HSPA / EVDO / LTE",
			"GSM 850 / 900 / 1800 / 1900 - SIM 1 & SIM 2 (dual-SIM model only)",
			"HSDPA 850 / 900 / 1700(AWS) / 1900 / 2100 - Global, USA",
			"1, 2, 3, 4, 5, 7, 8, 12, 13, 17, 18, 19, 20, 25, 26, 28, 32, 38, 39, 40, 41, 66 - Global",
	true,
			"Not yet booked",
			"Not yet booked",
			new ArrayList<>()
		);

		assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/phone-booking/1",
				PhoneDetails.class)).isEqualTo(expectedResponse) ;
	}

	@Test
	@Order(2)
	public void bookPhoneShouldReturnUnavailableBookablePhone() throws Exception {
		PhoneDetails expectedResponse = new PhoneDetails(
			1, "Samsung Galaxy S9",
			null,
			null,
			null,
			null,
			false,
			"Lloyd",
			instant.toString(),
			new ArrayList<>(){{
				Booking booking = new Booking();
				booking.setId(1);
				booking.setPhoneUser("Lloyd");
				booking.setTimestamp(instant.toEpochMilli());
				booking.setAction(UserAction.BORROWED);
				add(booking);
			}}
		);

		BookPhone request = new BookPhone(1, "Lloyd", now);
		PhoneDetails response = this.restTemplate.postForObject("http://localhost:" + port + "/phone-booking", request, PhoneDetails.class);
		ObjectMapper mapper = new ObjectMapper();

		assertThat(mapper.writeValueAsString(response)).isEqualTo(mapper.writeValueAsString(response)) ;
	}

	@Test
	@Order(3)
	public void getPhoneShouldReturnBookablePhoneWithBookings() throws Exception {

		PhoneDetails expectedResponse = new PhoneDetails(
			1, "Samsung Galaxy S9", "GSM / CDMA / HSPA / EVDO / LTE",
			"GSM 850 / 900 / 1800 / 1900 - SIM 1 & SIM 2 (dual-SIM model only)",
			"HSDPA 850 / 900 / 1700(AWS) / 1900 / 2100 - Global, USA",
			"1, 2, 3, 4, 5, 7, 8, 12, 13, 17, 18, 19, 20, 25, 26, 28, 32, 38, 39, 40, 41, 66 - Global",
			false,
			"Lloyd",
			instant.toString(),
			new ArrayList<>(){{
				Booking booking = new Booking();
				booking.setId(1);
				booking.setPhoneUser("Lloyd");
				booking.setTimestamp(instant.toEpochMilli());
				booking.setAction(UserAction.BORROWED);
				add(booking);
			}}
		);

		PhoneDetails response = this.restTemplate.getForObject("http://localhost:" + port + "/phone-booking/1", PhoneDetails.class);
		ObjectMapper mapper = new ObjectMapper();
		assertThat(mapper.writeValueAsString(response)).isEqualTo(mapper.writeValueAsString(expectedResponse));
	}

	@Test
	@Order(4)
	public void returnPhoneShouldReturnAvailablePhoneWithUpdatedBookings() throws Exception {
		PhoneDetails expectedResponse = new PhoneDetails(
			1, "Samsung Galaxy S9", null,
			null,
			null,
			null,
			true,
			"Lloyd",
			instant.toString(),
			new ArrayList<>(){{
				Booking booking1 = new Booking();
				booking1.setId(1);
				booking1.setPhoneUser("Lloyd");
				booking1.setTimestamp(instant.toEpochMilli());
				booking1.setAction(UserAction.BORROWED);
				add(booking1);

				Booking booking2 = new Booking();
				booking2.setId(2);
				booking2.setPhoneUser("Lloyd");
				booking2.setTimestamp(instant.toEpochMilli());
				booking2.setAction(UserAction.RETURNED);
				add(booking2);
			}}
		);

		HttpEntity<ReturnPhone> request = new HttpEntity<>(new ReturnPhone(1, now), null);
		PhoneDetails response = this.restTemplate.exchange("http://localhost:" + port + "/phone-booking",
				HttpMethod.PUT, request, PhoneDetails.class).getBody();
		ObjectMapper mapper = new ObjectMapper();
		assertThat(mapper.writeValueAsString(response)).isEqualTo(mapper.writeValueAsString(expectedResponse));
	}

	@Test
	@Order(5)
	public void getPhoneShouldReturnBookablePhoneWithReturnedBookings() throws Exception {

		PhoneDetails expectedResponse = new PhoneDetails(
				1, "Samsung Galaxy S9", "GSM / CDMA / HSPA / EVDO / LTE",
				"GSM 850 / 900 / 1800 / 1900 - SIM 1 & SIM 2 (dual-SIM model only)",
				"HSDPA 850 / 900 / 1700(AWS) / 1900 / 2100 - Global, USA",
				"1, 2, 3, 4, 5, 7, 8, 12, 13, 17, 18, 19, 20, 25, 26, 28, 32, 38, 39, 40, 41, 66 - Global",
				true,
				"Lloyd",
				instant.toString(),
				new ArrayList<>(){{
					Booking booking1 = new Booking();
					booking1.setId(1);
					booking1.setPhoneUser("Lloyd");
					booking1.setTimestamp(instant.toEpochMilli());
					booking1.setAction(UserAction.BORROWED);
					add(booking1);

					Booking booking2 = new Booking();
					booking2.setId(2);
					booking2.setPhoneUser("Lloyd");
					booking2.setTimestamp(instant.toEpochMilli());
					booking2.setAction(UserAction.RETURNED);
					add(booking2);
				}}
		);

		PhoneDetails response = this.restTemplate.getForObject("http://localhost:" + port + "/phone-booking/1", PhoneDetails.class);
		ObjectMapper mapper = new ObjectMapper();
		assertThat(mapper.writeValueAsString(response)).isEqualTo(mapper.writeValueAsString(expectedResponse));
	}

}
