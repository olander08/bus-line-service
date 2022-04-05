package se.aiguilleit.busline.domain;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusLineRepository {

    List<BusLine> findAll();
    List<BusLine> saveAll(List<BusLine> busLines);

}
