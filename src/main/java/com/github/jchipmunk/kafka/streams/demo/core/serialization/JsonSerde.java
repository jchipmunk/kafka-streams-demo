package com.github.jchipmunk.kafka.streams.demo.core.serialization;

import com.fasterxml.jackson.databind.JsonNode;

public final class JsonSerde extends AbstractSerde<JsonNode> {

    public JsonSerde() {
        super(new JsonSerializer(), new JsonDeserializer());
    }
}
