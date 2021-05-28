package HAD.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ARCHITECTFormInfo {
  private String sampleNumberRange,experimentLocation,device1,device2,device3,
    reagentName,manufacturer,batchNo,effectiveDate,environmentTemperature,humidity;
}
