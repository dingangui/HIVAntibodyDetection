package HAD.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PARTFormInfo {
  private String testItem,sampleName, patientType,manufacturer,
    batchNo,effectiveDate,temperatureAndHumidity,reactionTime;
}
