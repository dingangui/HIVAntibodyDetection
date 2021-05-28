package HAD.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SampleInfo {
  private int id;
  private String acceptanceNo;
  private String inspectionUnit;
  @DateTimeFormat(pattern="yyyy-MM-dd")
  private String inspectionDate;
  private String sampleType;
  private String patientType;
  private String patientName;
  private String sex;
  private String age;
  private String profession;
  private String country;
  private String nation;
  private String marriage;
  private String educationalLevel;
  private String IDNumber;
  private String phone;
  private String presentAddress;
  private String residenceAddress;
  private String status;
  private String operation;
  private String href;
  private String conclusion;
  private String inspector;
  private String reviewer;
}
