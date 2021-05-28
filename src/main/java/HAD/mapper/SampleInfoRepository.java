package HAD.mapper;

import HAD.entity.SampleInfo;

import java.util.List;

public interface SampleInfoRepository {
  int getLastInsertID();

  void updateAcceptanceNo(String acceptanceNo, int id);

  void save(SampleInfo sampleInfo);

  List<SampleInfo> findPendingSamples();

  List<SampleInfo> findSamplesToBeTested();

  List<SampleInfo> findUnreviewedSamples();

  List<SampleInfo> findModifiableSamples();

  List<SampleInfo> findExportableSamples();

  List<SampleInfo> findPrimaryScreeningPassed();

  List<SampleInfo> findConfirmationNeeded();

  void updateStatusAndOperation(String status, String operation, String href, String acceptanceNo);

  void updateConclusion(String conclusion, String acceptanceNo);

  void updateInspector(String inspector,String acceptanceNo);

  void updateReviewer(String reviewer,String acceptanceNo);

  SampleInfo findByAcceptanceNo(String acceptanceNo);

  void update(SampleInfo sampleInfo);

}
