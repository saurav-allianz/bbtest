package a.vehicle.repository;

import a.api.model.DataOption;
import a.api.model.DataOptionList;
import org.springframework.stereotype.Component;

@Component
public class MockVehicleRepository implements VehicleRepository {

  @Override
  public DataOptionList vehicleBrands() {
    return new DataOptionList()
        .addValuesItem(
            new DataOption()
                .label("BMW")
                .value("BMW")
                .additionalText("")
        );
  }

}
