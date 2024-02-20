package com.github.jchipmunk.kafka.streams.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jchipmunk.kafka.streams.demo.core.TopologyBuilder;
import com.github.jchipmunk.kafka.streams.demo.core.serialization.JsonSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import javax.annotation.Nonnull;
import java.util.Properties;

public class RoutingTopologyBuilder implements TopologyBuilder {

    @Nonnull
    @Override
    public Topology buildTopology(@Nonnull Properties props) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, JsonNode> inputStream =
                builder.stream("input", Consumed.with(Serdes.String(), new JsonSerde()))
                        .filter((key, value) -> value.isObject());

        inputStream.mapValues(RoutingTopologyBuilder::transform)
                .to("output-1", Produced.with(Serdes.String(), new JsonSerde()));
        inputStream.mapValues(value -> value)
                .to("output-2", Produced.with(Serdes.String(), new JsonSerde()));
        inputStream.mapValues(value -> value)
                .to("output-3", Produced.with(Serdes.String(), new JsonSerde()));

        inputStream.to("output-json", Produced.with(Serdes.String(), new JsonSerde()));
        return builder.build();
    }

    @Nonnull
    private static JsonNode transform(@Nonnull JsonNode value) {
        ObjectNode objectNode = value.deepCopy();
        return objectNode;
    }
}
