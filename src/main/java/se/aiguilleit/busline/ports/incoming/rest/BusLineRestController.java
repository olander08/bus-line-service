package se.aiguilleit.busline.ports.incoming.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.aiguilleit.busline.application.BusLineService;
import se.aiguilleit.busline.domain.BusLine;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class BusLineRestController {

    private BusLineService busLineService;


    public BusLineRestController(BusLineService busLineService) {
	    this.busLineService = busLineService;
    }


    @GetMapping("/bus-lines")
    public BusLinesDTO getBusLines() {
        return toBusLinesDTO(busLineService.getBusLines());
    }


    private BusLinesDTO toBusLinesDTO(List<BusLine> busLines) {
        List<BusLineDTO> busLineDTOs = busLines.stream()
                .map(this::toBusLineDTO)
                .collect(Collectors.toList());

        return new BusLinesDTO(busLineDTOs);
    }


    private BusLineDTO toBusLineDTO(BusLine busLine) {
        List<StopDTO> stops = busLine.getStops().stream()
                .map(s -> new StopDTO(s.getStopNumber(), s.getStopAreaNumber(), s.getName()))
                .collect(Collectors.toList());

		return new BusLineDTO(busLine.getLineNumber(), stops);
    }
}

