package com.robot.engineeringrobot.service;

import com.robot.common.service.ParallelExecutionService;
import com.robot.network.message.video.VideoNetworkMessageCodec;
import com.robot.network.message.video.VideoNetworkMessageParameters;
import com.robot.network.message.video.VideoNetworkMessagePayload;
import com.robot.network.message.video.VideoNetworkMessageType;
import com.robot.network.service.NetworkMessageConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.freedesktop.gstreamer.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.robot.network.message.video.VideoNetworkMessageType.START;
import static com.robot.network.message.video.VideoNetworkMessageType.STOP;

public class VideoService implements NetworkMessageConsumer<VideoNetworkMessagePayload> {
    private static final Logger logger = LogManager.getLogger(VideoService.class);

    private final String device;
    private final ParallelExecutionService parallelExecutionService;
    private boolean started = false;

    @Inject
    public VideoService(
            @Named("video.device") String device,
            ParallelExecutionService parallelExecutionService) {
        this.device = device;
        this.parallelExecutionService = parallelExecutionService;
    }

    @Override
    public synchronized void consumeMessage(VideoNetworkMessagePayload payload) {
        if (payload == null) {
            logger.error("Empty payload");
            return;
        }

        VideoNetworkMessageType messageType = payload.getMessageType();

        if (messageType == STOP) {
            if (!started) {
                logger.warn("Service not running");
                return;
            }

            stopStreaming();
            started = false;
            logger.info("Service stopped");
            return;
        }

        VideoNetworkMessageParameters parameters = payload.getParameters();
        if (parameters == null) {
            logger.error("Empty parameters");
            return;
        }

        if (started) {
            logger.info("Restarting service");
            stopStreaming();
            startStreaming(parameters);
        } else {
            started = true;
            startStreaming(parameters);
        }
        logger.info("Service started");
    }

    private synchronized void startStreaming(VideoNetworkMessageParameters parameters) {
        String pipelineConfig;
        switch (parameters.getCodec()) {
            case H264: {
                pipelineConfig = getH264Config(parameters);
                break;
            }
            case MPEG4: {
                pipelineConfig = getMpeg4Config(parameters);
                break;
            }
            default: {
                pipelineConfig = null;
            }
        }

        if (pipelineConfig == null) {
            logger.error("Unknown codec {}", parameters.getCodec());
            return;
        }

        logger.info("Pipeline: {}", pipelineConfig);

        parallelExecutionService.submitLongRunning(() -> {
            Gst.init();
            Bin bin = Bin.launch(pipelineConfig, true);

            Element udpsink = ElementFactory.make("udpsink", "udpsink");
            udpsink.set("sync", "false");
            udpsink.set("host", parameters.getReceiverAddress().getHostAddress());
            udpsink.set("port", String.valueOf(parameters.getReceiverPort()));

            Pipeline pipe = new Pipeline();
            pipe.addMany(bin, udpsink);
            Pipeline.linkMany(bin, udpsink);

            pipe.play();

            Gst.main();
        });
    }

    private synchronized void stopStreaming() {
        Gst.quit();
    }

    private String getMpeg4Config(VideoNetworkMessageParameters parameters) {
        return
                String.format("v4l2src device=\"%s\" ", device) +
                String.format("! video/x-raw,width=%s,height=%s ",
                        parameters.getHorizontalResolution(),
                        parameters.getVerticalResolution()) +
                "! queue ! videoconvert ! queue ! avenc_mpeg4 " +
                "! queue ! rtpmp4vpay config-interval=3 ! queue ";
    }

    private String getH264Config(VideoNetworkMessageParameters parameters) {
        return
                String.format("v4l2src device=\"%s\" ", device) +
                String.format("! video/x-raw,width=%s,height=%s ",
                        parameters.getHorizontalResolution(),
                        parameters.getVerticalResolution()) +
                "! queue ! videoconvert ! queue " +
                "! x264enc " +
                "speed-preset=ultrafast " +
                "tune=zerolatency " +
                "byte-stream=true " +
                "threads=4 " +
                "key-int-max=15 " +
                "intra-refresh=true " +
                "! queue ! h264parse " +
                "! queue ! rtph264pay pt=96 ! queue ";
    }
}