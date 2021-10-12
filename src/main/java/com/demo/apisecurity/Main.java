package com.demo.apisecurity;

import com.demo.apisecurity.controller.SpaceController;
import org.dalesbred.Database;
import org.dalesbred.result.EmptyResultException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;

public class Main {

    private static final String DB_CONN = "jdbc:h2:mem:natter";

    public static void main(String[] args) throws URISyntaxException, IOException {
        var datasource = JdbcConnectionPool.create(DB_CONN, "natter", "password");
        var database = Database.forDataSource(datasource);
        createTables(database);
        datasource = JdbcConnectionPool.create(DB_CONN, "natter_api_user", "password");
        database = Database.forDataSource(datasource);

        var spaceController = new SpaceController(database);
        post("/spaces", spaceController::createSpace);

        after((request, response) -> response.type("application/json"));
        afterAfter((request, response) -> response.header("Server", ""));

        internalServerError(new JSONObject().put("error", "Internal server error").toString());
        notFound(new JSONObject().put("error", "Not found").toString());

        exception(IllegalArgumentException.class, Main::badRequest);
        exception(JSONException.class, Main::badRequest);
        exception(EmptyResultException.class,
            (ex, request, response) -> response.status(404));
    }

    private static void createTables(Database database) throws URISyntaxException, IOException {
        var path = Paths.get(Main.class.getResource("/schema.sql").toURI());
        database.update(Files.readString(path));
    }

    private static void badRequest(Exception ex, Request request, Response response) {
        response.status(400);
        response.body("{\"error\": \"" + ex.getMessage() + "\"}");
    }
}
