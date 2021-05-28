package HAD.controller;

import HAD.entity.DetectionResult;
import HAD.entity.SampleInfo;
import HAD.mapper.DetectionResultRepository;
import HAD.mapper.SampleInfoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("/modify")
public class ModifyHandler {
  @Resource
  private SampleInfoRepository sampleInfoRepository;
  @Resource
  private DetectionResultRepository detectionResultRepository;


  @GetMapping("/{acceptanceNo}")
  public String modify(@PathVariable String acceptanceNo, Model model) {
    //    根据样品受理编号，查询该样品已检测信息
    List<DetectionResult> detectionResults =
      detectionResultRepository.findByAcceptanceNo(acceptanceNo);

    //    向前端展示样品基本信息
    model.addAttribute("sampleInfo",
      sampleInfoRepository.findByAcceptanceNo(acceptanceNo));

    //    向前端展示样品检测结果
    model.addAttribute("detectionResults", detectionResults);

    return "modify";
  }

//修改样品信息
  @PostMapping("/sampleInfoSubmit")
  public String modifySampleInfo(SampleInfo sampleInfo){
    sampleInfoRepository.update(sampleInfo);
    return "redirect:/modify/"+sampleInfo.getAcceptanceNo();
  }

//  修改某一轮检测结果
  @PostMapping("/detectionResultSubmit")
  public String modifyDetectionResult(DetectionResult detectionResult){
    detectionResultRepository.update(detectionResult);
    return "redirect:/modify/"+detectionResult.getAcceptanceNo();
  }

//  修改审核结果
  @PostMapping("/conclusionSubmit")
  public String modifyConclusion(String conclusion,String acceptanceNo){
    sampleInfoRepository.updateConclusion(conclusion,acceptanceNo);
    return "redirect:/modify/"+acceptanceNo;
  }
}
