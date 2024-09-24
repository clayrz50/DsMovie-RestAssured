package com.devsuperior.dsmovie.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dsmovie.tests.TokenUtil;

import io.restassured.http.ContentType;

public class ScoreControllerRA {
	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken, invalidToken;
	private Long existingId, nonExistingId, dependentId;
	private Map<String, Object> scoreInstance;
	@BeforeEach
	public void setUp() throws Exception  {
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
		scoreInstance=new HashMap<>();
		scoreInstance.put("movieId", nonExistingId);
		scoreInstance.put("score", 5.0F);
	}
	
	@Test
	public void saveScoreShouldReturnNotFoundWhenMovieIdDoesNotExist() throws Exception {
		JSONObject scoreJSON = new JSONObject(scoreInstance);
		
		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
				.body(scoreJSON).contentType(ContentType.JSON).accept(ContentType.JSON).when().put("/scores").then()
				.statusCode(404);
	}
	
	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenMissingMovieId() throws Exception {
		scoreInstance.put("movieId", null);
		JSONObject scoreJSON = new JSONObject(scoreInstance);
		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
		.body(scoreJSON).contentType(ContentType.JSON).accept(ContentType.JSON).when().put("/scores").then()
		.statusCode(422);
	}
	
	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenScoreIsLessThanZero() throws Exception {
		scoreInstance.put("score", -1.0F);
		JSONObject scoreJSON = new JSONObject(scoreInstance);
		given().header("Content-type", "application/json").header("Authorization", "Bearer " + adminToken)
		.body(scoreJSON).contentType(ContentType.JSON).accept(ContentType.JSON).when().put("/scores").then()
		.statusCode(422);
		
	}
}
