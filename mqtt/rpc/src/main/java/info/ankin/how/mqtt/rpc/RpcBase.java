// package info.ankin.how.mqtt.rpc;
//
// import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
// import lombok.Data;
// import lombok.RequiredArgsConstructor;
// import lombok.experimental.Accessors;
// import org.springframework.beans.factory.annotation.Autowired;
//
// @RequiredArgsConstructor
// public abstract class RpcBase {
//     @Autowired
//     protected RpcServer.Props props;
//     @Autowired
//     protected Mqtt5RxClient client;
//
//     /**
//      * implies that messages have:
//      * * location (datacenter)
//      * * application name (multiple apps)
//      * * rpc function name (multiple functions per app)
//      * * instance (client identifier)
//      */
//     @Accessors(chain = true)
//     @Data
//     protected static class Topic {
//         String datacenter;
//         String application;
//         String function;
//         String instance;
//
//         @Override
//         public String toString() {
//             return datacenter + "/" + application + "/" + function + "/" + instance;
//         }
//     }
// }
