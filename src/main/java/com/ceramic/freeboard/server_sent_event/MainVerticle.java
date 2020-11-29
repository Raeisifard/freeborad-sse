package com.ceramic.freeboard.server_sent_event;

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

public class MainVerticle extends AbstractVerticle {
  private final static int PORT = 9001;
  private final static String retry = "5000";
  private final static Logger LOG = LoggerFactory.getLogger(com.ceramic.freeboard.MainVerticle.class.getName());
  private final static String EB_ADDRESS = "freeboard.sse";

  private HttpServer server;
  private final StaticHandler staticFiles = StaticHandler.create();
  //private final SSEHandler sse = SSEHandler.create();
  private final EventBusSSEBridge eventBusSSEBridge = EventBusSSEBridge.create();
  private Long timerId;
  private final String magicToken = "theOneThatRulesThemAll";
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
    //router.get("/").handler(context -> context.reroute("index.html"));
    router.get("/dev").handler(context -> context.reroute("/index-dev.html"));
    router.get("/test").handler(context -> context.reroute("/sse/index.html"));
    //router.get("/sse.html").handler(context -> context.reroute("/sse/index.html"));
    router.get("/sse").handler(eventBusSSEBridge);
    //router.get("/dev").handler(rc -> rc.reroute("/freeboard/index-dev.html"));
    router.route("/*").handler(StaticHandler.create());
    eventBusSSEBridge.mapping(request -> EB_ADDRESS);
    vertx.createHttpServer().requestHandler(router).listen(PORT, http -> {
      if (http.succeeded()) {
        vertx.setPeriodic(5000, this::fetchISSPosition);
        startPromise.complete();
        System.out.println("HTTP server started on port " + PORT);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
  private void fetchISSPosition(Long timerId) {
    this.timerId = timerId;
    DeliveryOptions dO = new DeliveryOptions();
    var uuid = UUID.randomUUID().toString();
    dO.addHeader("event", "statement").addHeader("retry", retry).addHeader("id", uuid);
    //var date = new Date().toString();
    //System.out.println(date + ":" + uuid);
    var msgTest = new JsonObject().put("data", new Date().toString()).put("id", uuid);
    vertx.eventBus().publish(EB_ADDRESS, msgTest, dO);
  }
  @Override
  public void stop(Promise<Void> stopPromise) {
    if (timerId != null) {
      vertx.cancelTimer(timerId);
    }
    if (server != null) {
      server.close();
    }
    stopPromise.complete();
  }
}
