package com.github.jchipmunk.kafka.streams.demo;

import com.github.jchipmunk.kafka.streams.demo.core.KafkaStreamsLauncher;
import com.github.jchipmunk.kafka.streams.demo.core.TopologyBuilder;

import java.io.IOException;

public class KafkaStreamsDemoApp {

    public static void main(String[] args) throws IOException {
        TopologyBuilder topologyBuilder = new RoutingTopologyBuilder();
        String pathToPropsFile = args[0];
        KafkaStreamsLauncher streamsLauncher = new KafkaStreamsLauncher(topologyBuilder, pathToPropsFile);
        streamsLauncher.run();
    }
}
