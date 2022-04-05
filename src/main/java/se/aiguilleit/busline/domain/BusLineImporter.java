package se.aiguilleit.busline.domain;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BusLineImporter {

    List<BusLine> getBusLines();

}
