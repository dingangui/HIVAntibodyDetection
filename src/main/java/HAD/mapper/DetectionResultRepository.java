package HAD.mapper;

import HAD.entity.DetectionResult;

import java.util.Date;
import java.util.List;

public interface DetectionResultRepository {
  void save(DetectionResult detectionResult);
  List<DetectionResult> findByAcceptanceNo(String AcceptanceNo);
  void update(DetectionResult detectionResult);

}
