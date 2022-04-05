package se.aiguilleit.busline.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import se.aiguilleit.busline.domain.BusLine;
import se.aiguilleit.busline.domain.BusLineRepository;
import se.aiguilleit.busline.ports.incoming.trafiklab.TrafiklabBusLineImporter;

import java.util.List;

@Service
public class BusLineService {

    private static final Logger LOG = LoggerFactory.getLogger(BusLineService.class);

    private ApplicationContext context;
    private TrafiklabBusLineImporter busLineImporter;
    private BusLineRepository busLineRepository;
    private Environment environment;


    public BusLineService(
            ApplicationContext context,
            TrafiklabBusLineImporter busLineImporter,
            BusLineRepository busLineRepository,
            Environment environment
    ) {
        this.context = context;
        this.busLineImporter = busLineImporter;
        this.busLineRepository = busLineRepository;
        this.environment = environment;
    }


    @EventListener(ApplicationReadyEvent.class)
    private void onReady() {
        // hack to prevent import before wiremock server stub configuration during testing
        if (!environment.acceptsProfiles(Profiles.of("test"))) {
            try {
                importBusLines();
            } catch (Exception e) {
                LOG.error("Unable to import bus lines on application ready, exiting", e);

                SpringApplication.exit(context, () -> 0);
            }
        }
    }


    public void importBusLines() {
        List<BusLine> busLines = busLineImporter.getBusLines();
        busLineRepository.saveAll(busLines);
    }


    public List<BusLine> getBusLines() {
        return busLineRepository.findAll();
    }
}
