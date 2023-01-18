package a.vehicle.rest;

import a.api.VehicleSearchApi;
import a.api.model.DataOptionList;

import a.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VehicleController implements VehicleSearchApi {

  private final VehicleService vehicleService;


  @Override
  public ResponseEntity<DataOptionList> vehicleBrands() {
    DataOptionList brands = vehicleService.vehicleBrands();
    return ResponseEntity
        .ok()
        .body(brands);
  }
}
