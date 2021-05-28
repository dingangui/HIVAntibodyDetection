package HAD.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class DetectionResult {
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private String
    acceptanceNo,
    detectionMethod,
    detectionDate,
    manufacture,
    batchNo,
    validDate,
    criticalValue,
    detectionValue,
    detectionResult,
    detectionName;
}
