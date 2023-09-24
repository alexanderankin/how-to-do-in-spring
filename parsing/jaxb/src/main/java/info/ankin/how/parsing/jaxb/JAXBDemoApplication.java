package info.ankin.how.parsing.jaxb;

import info.ankin.how.parsing.jaxb.generated.ShipOrder;
import lombok.SneakyThrows;
import org.xmlunit.builder.Input;

import java.nio.file.Files;
import java.nio.file.Path;
import jakarta.xml.bind.JAXBContext;

public class JAXBDemoApplication {
    @SneakyThrows
    public static void main(String[] args) {
        String s = Files.readString(Path.of(JAXBDemoApplication.class.getResource("/jaxb-demo/documents/ship-order.xml").toURI()));

        JAXBContext jaxbContext = JAXBContext.newInstance(ShipOrder.class);
        ShipOrder shipOrder = (ShipOrder) jaxbContext.createUnmarshaller().unmarshal(Input.fromString(s).build());

        System.out.println(shipOrder);
    }
}
