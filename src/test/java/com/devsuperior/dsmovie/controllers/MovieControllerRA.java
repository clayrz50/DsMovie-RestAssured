package com.devsuperior.dsmovie.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dsmovie.tests.TokenUtil;

import io.restassured.http.ContentType;

public class MovieControllerRA {
	private String title;
	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken, invalidToken;
	private Long existingId, nonExistingId, dependentId;
	private Map<String, Object> newMovie;

	@BeforeEach
	public void setUp() throws JSONException {
		title = "Harry";
		clientUsername = "alex@gmail.com";
		clientPassword = "123456";

		adminUsername = "maria@gmail.com";
		adminPassword = "123456";

		existingId = 1L;
		nonExistingId = 100L;

		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		invalidToken = adminToken + "xpto";

		baseURI = "http://localhost:8080";
		newMovie = new HashMap<>();
		newMovie.put("title", "Test Movie");
		newMovie.put("score", 0.0F);
		newMovie.put("count", 0);
		newMovie.put("image", "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg");

	}

	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {
		given().get("/movies").then().statusCode(200).body("content.title",
				hasItems("Venom: Tempo de Carnificina", "Star Wars: Episódio I - A Ameaça Fantasma"));;
	}

	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {
		given().get("/movies?title={titleMovie}", title).then().statusCode(200).body("content.id[1]", is(20))
		.body("content.title[1]", equalTo("Harry Potter e a Pedra Filosofal")).body("content.score[1]", is(0.0F))
		.body("content.count[1]", is(0)).body("content.image[1]", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/lvOLivVeX3DVVcwfVkxKf0R22D8.jpg"));
	}

	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
				.accept(ContentType.JSON).when().get("/movies/{id}", existingId).then().statusCode(200)
				.body("id", is(1))
				.body("title", equalTo("The Witcher"))
				.body("score", is(3.0F))
				.body("count", is(3)).body("image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"));
	}

	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
				.accept(ContentType.JSON).when().get("/movies/{id}", nonExistingId).then().statusCode(404);
	}

	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {
		newMovie.put("title", "");
		JSONObject movieJSON = new JSONObject(newMovie);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
				.body(movieJSON).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/movies").then()
				.statusCode(422);
	}

	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
		JSONObject movieJSON = new JSONObject(newMovie);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + clientToken)
				.body(movieJSON).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/movies").then()
				.statusCode(403);
	}

	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
		JSONObject movieJSON = new JSONObject(newMovie);

		given().header("Content-type", "application/json").header("Authorization", "Bearer " + invalidToken)
				.body(movieJSON).contentType(ContentType.JSON).accept(ContentType.JSON).when().post("/movies").then()
				.statusCode(401);
	}
}
