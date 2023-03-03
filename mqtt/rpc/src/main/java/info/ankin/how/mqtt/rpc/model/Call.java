package info.ankin.how.mqtt.rpc.model;

import java.util.function.Function;

public interface Call<I, O> extends Function<I, O> {
}
