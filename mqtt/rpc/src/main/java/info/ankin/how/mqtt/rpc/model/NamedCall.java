package info.ankin.how.mqtt.rpc.model;

public interface NamedCall<I, O> extends Call<I, O> {
    String name();
}
