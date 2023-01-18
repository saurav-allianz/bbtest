package a.vehicle.service;

import a.api.model.DataOptionList;
import a.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

  private final VehicleRepository vehicleRepository;


  @Override
  public DataOptionList vehicleBrands() {
    return vehicleRepository.vehicleBrands();
  }

}
