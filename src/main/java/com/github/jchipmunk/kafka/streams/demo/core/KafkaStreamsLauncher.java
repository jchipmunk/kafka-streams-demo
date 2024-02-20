package com.github.jchipmunk.kafka.streams.demo.core;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.jchipmunk.kafka.streams.demo.core.ApplicationState.FAILED;
import static com.github.jchipmunk.kafka.streams.demo.core.ApplicationState.INITIALIZED;
import static com.github.jchipmunk.kafka.streams.demo.core.ApplicationState.STARTED;
import static com.github.jchipmunk.kafka.streams.demo.core.ApplicationState.STOPPED;
import static org.apache.kafka.common.utils.Utils.loadProps;

public class KafkaStreamsLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsLauncher.class);

    @Nonnull
    private final AtomicReference<ApplicationState> state;
    @Nonnull
    private final Topology topology;
    @Nonnull
    private final Properties props;

    public KafkaStreamsLauncher(@Nonnull TopologyBuilder topologyBuilder,
                                @Nullable String pathToPropsFile) throws IOException {
        this(topologyBuilder, loadProps(pathToPropsFile));
    }

    public KafkaStreamsLauncher(@Nonnull TopologyBuilder topologyBuilder, @Nonnull Properties props) {
        state = new AtomicReference<>(INITIALIZED);
        this.topology = topologyBuilder.buildTopology(props);
        LOGGER.info("Topology description:\n{}", topology.describe());
        this.props = props;
    }

    public void run() {
        try (KafkaStreams streams = new KafkaStreams(topology, props)) {
            CountDownLatch latch = new CountDownLatch(1);

            streams.setUncaughtExceptionHandler(exception -> {
                LOGGER.error("Uncaught exception handler triggered:", exception);
                state.set(FAILED);
                latch.countDown();
                return null;
            });
            LOGGER.info("Uncaught exception handler added.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutdown hook triggered.");
                state.set(STOPPED);
                latch.countDown();
            }, "kafka-streams-shutdown-hook"));
            LOGGER.info("Shutdown hook added.");

            streams.start();
            state.compareAndSet(INITIALIZED, STARTED);
            latch.await();
            state.compareAndSet(STARTED, STOPPED);
        } catch (Throwable e) {
            LOGGER.error("Application encountered a fatal error:", e);
            state.set(FAILED);
        }
        if (state.get() == FAILED) {
            System.exit(1);
        }
    }

    public void run2() {
        KafkaStreams streams = new KafkaStreams(topology, props);
        CountDownLatch latch = new CountDownLatch(1);

        streams.setUncaughtExceptionHandler(exception -> {
            LOGGER.error("Uncaught exception handler triggered:", exception);
            state.set(FAILED);
            // KafkaStreams#close() call inside the handler can cause a deadlock, so perform the call later
            latch.countDown();
            return null;
        });
        LOGGER.info("Uncaught exception handler added.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown hook triggered.");
            streams.close();
            state.set(STOPPED);
            latch.countDown();
        }, "kafka-streams-shutdown-hook"));
        LOGGER.info("Shutdown hook added.");

        try {
            streams.start();
            state.compareAndSet(INITIALIZED, STARTED);
            latch.await();
            state.compareAndSet(STARTED, STOPPED);
        } catch (Throwable e) {
            LOGGER.error("Application encountered a fatal error:", e);
            state.set(FAILED);
            streams.close();
            System.exit(1);
        }

        // Stop the application if an uncaught exception is handled
        if (state.get() == FAILED) {
            streams.close();
            System.exit(1);
        }
    }
}
