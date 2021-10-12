package com.demo.apisecurity.controller;

import org.dalesbred.Database;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.regex.Pattern;

public class SpaceController {

    private static final int MAX_SPACE_NAME_LENGTH = 255;
    private static final Pattern OWNER_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9]{1,29}");
    private final Database database;

    public SpaceController(Database database) {
        this.database = database;
    }

    public JSONObject createSpace(Request request, Response response) {
        var json = new JSONObject(request.body());
        var spaceName = json.getString("name");
        if (spaceName.length() > MAX_SPACE_NAME_LENGTH) {
            throw new IllegalArgumentException("space name too long");
        }
        var owner = json.getString("owner");
        if (!OWNER_PATTERN.matcher(owner).matches()) {
            throw new IllegalArgumentException("invalid username: " + owner);
        }

        return database.withTransaction(tx -> {
            var spaceId = database.findUniqueLong("select next value for space_id_seq;");
            database.updateUnique(
                "insert into spaces(space_id, name, owner) values(?, ?, ?);",
                spaceId, spaceName, owner
            );
            var locationUri = "/spaces/" + spaceId;
            response.status(201);
            response.header("Location", locationUri);

            return new JSONObject()
                .put("name", spaceName)
                .put("uri", locationUri);
        });
    }
}
