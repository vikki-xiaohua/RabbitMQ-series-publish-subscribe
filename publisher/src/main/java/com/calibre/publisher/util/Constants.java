package com.calibre.publisher.util;

public class Constants {
    public static final String TOPIC_EXCHANGE_FX_RATE_API = "topic-exchange-fx-rate";
    public static final String TOPIC_QUEUE_FX_RATE_API = "topic-queue-fx-rate";
    public static final String TOPIC_FX_RATE_API_ROUTING_KEY = "topic.fx.rate.routing.key.#";

    public static final String TOPIC_BATCH_QUEUE_FX_RATE_API = "topic-batch-queue-fx-rate";
    public static final String TOPIC_BATCH_FX_RATE_API_ROUTING_KEY = "topic.batch.fx.rate.routing.key.#";

    public static final String HAZELCAST_FX_RATE_CACHE_KEY_PREFIX = "fx-currency-pair";

    public static final int REST_TEMPLATE_CONNECT_TIMEOUT = 3000;
    public static final int REST_TEMPLATE_READ_TIMEOUT = 3000;
    public static final String GET_REALTIME_DATA_FOR_CURRENCY_PAIR = "/real-time/{currency_pair}?api_token={api_token}&fmt={fmt}";
    public static final String FX_RATE_API_QUERY_PARAMETER_CURRENCY_PAIR = "currency_pair";
    public static final String FX_RATE_API_QUERY_PARAMETER_API_TOKEN = "api_token";
    public static final String FX_RATE_API_QUERY_PARAMETER_FORMAT = "fmt";
    public static final String FX_RATE_API_CURRENCY_PAIR_SUFFIX = ".FOREX";
    public static final String FOREX_ENDPOINT_CURRENCY_PAIR_LIST = "forex.endpoint.currency-pair-list";

    public static final String APPLICATION_ERROR_SUBJECT = "Publisher Application Error!";
    public static final String APPLICATION_ERROR_BODY = "<p>Publisher Application Error Detail:<br>";

    public static final int SCHEDULED_DATA_REQUEST_FIXED_RATE_MILLISECONDS = 10 * 60 * 1000;
    public static final int SCHEDULED_DATA_REQUEST_RATE_INITIAL_DELAY_MILLISECONDS = 3000;


    public static final String ERROR_EXCHANGE = "error-exchange";
    public static final String ERROR_QUEUE = "error-queue";
    public static final String ERROR_ROUTING_KEY = "error-routing-key";

    public static final String ERROR_BATCH_QUEUE = "error-batch-queue";
    public static final String ERROR_BATCH_ROUTING_KEY = "error-batch-routing-key";

    public static final String TOPIC_BATCH_FX_RATE_ROUTING_KEY_1 = "topic.batch.fx.rate.routing.key.1";
    public static final String TOPIC_FX_RATE_ROUTING_KEY_1 = "topic.fx.rate.routing.key.1";

}
