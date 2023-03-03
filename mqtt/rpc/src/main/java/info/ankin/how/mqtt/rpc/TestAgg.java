package info.ankin.how.mqtt.rpc;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestAgg {
    public TestAgg(List<TestInst> insts) {
        System.out.println(insts);
    }
}
