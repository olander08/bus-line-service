package se.aiguilleit.busline.ports.outgoing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import se.aiguilleit.busline.domain.BusLine;
import se.aiguilleit.busline.domain.BusLineRepository;

@Repository
public class BusLineRepositoryInMem implements BusLineRepository {

    private Map<Integer, BusLine> busLinesById = new HashMap<>();


    @Override
    public List<BusLine> findAll() {
        return busLinesById.values().stream()
                .toList();
    }


    @Override
    public List<BusLine> saveAll(List<BusLine> busLines) {
        busLines.forEach(b -> busLinesById.put(b.getLineNumber(), b));
        return busLinesById.values().stream().toList();
    }
}
