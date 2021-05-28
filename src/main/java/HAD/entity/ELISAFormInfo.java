package HAD.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ELISAFormInfo {
  private String sampleNumberRange,experimentLocation,device1,device2,device3,
    reagentName,manufacturer,batchNo,effectiveDate,temperature,period1,period2,period3,
    environmentTemperature,humidity;
}
