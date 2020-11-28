package com.ceramic.freeboard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sse.EventBusSSEBridge;

import java.util.Date;
import java.util.UUID;

public class SseGeneratorVerticle extends AbstractVerticle {
  private final static Logger LOG = LoggerFactory.getLogger(MainVerticle.class.getName());
  private final static String EB_ADDRESS = "freeboard.sse";
  private final static String retry = "5000";
  private Long timerId;
  private int periodic = 5000;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.setPeriodic(periodic, this::fetchISSPosition);
    startPromise.complete();
    System.out.println("SSE Generator started with " + periodic + "ms periodic.");
  }

  private void fetchISSPosition(Long timerId) {
    this.timerId = timerId;
    DeliveryOptions dO = new DeliveryOptions();
    var uuid = UUID.randomUUID().toString();
    dO.addHeader("event", "statement").addHeader("retry", retry).addHeader("id", uuid);
    var msgTest = new JsonObject().put("data", new Date().toString()).put("id", uuid);
    vertx.eventBus().publish(EB_ADDRESS, msgTest, dO);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    if (timerId != null) {
      vertx.cancelTimer(timerId);
    }
    stopPromise.complete();
  }
}
