package HAD.controller;

import HAD.entity.SampleInfo;
import HAD.mapper.SampleInfoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import java.util.Calendar;

@Controller
public class InfoSubmitHandler {

  @Resource
  private SampleInfoRepository sampleInfoRepository;
//提交样品信息
  @PostMapping("/infoSubmit")
  public String infoSubmit(SampleInfo sampleInfo){
    // 保存基本信息
    sampleInfoRepository.save(sampleInfo);
    // 获取受理编号
    String acceptanceNo = 'A'+String.valueOf(Calendar.getInstance().get(Calendar.YEAR))+ '-' + sampleInfoRepository.getLastInsertID();
    // 更新受理编号
    sampleInfoRepository.updateAcceptanceNo(acceptanceNo,sampleInfoRepository.getLastInsertID());
    return "redirect:/pendingSamples";
  }
}
