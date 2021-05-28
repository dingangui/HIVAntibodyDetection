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
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/review")
public class ReviewHandler {

  @Resource
  private SampleInfoRepository sampleInfoRepository;
  @Resource
  private DetectionResultRepository detectionResultRepository;

  @GetMapping("/{acceptanceNo}")
//  展示样品基本信息和样品的历史检测结果
  public String review(@PathVariable String acceptanceNo, Model model) {
    SampleInfo sampleInfo = sampleInfoRepository.findByAcceptanceNo(acceptanceNo);
    List<DetectionResult> detectionResults = detectionResultRepository.findByAcceptanceNo(acceptanceNo);
    model.addAttribute("sampleInfo", sampleInfo);
    model.addAttribute("detectionResults", detectionResults);
    return "review";
  }

  //  提交审核结果
  @PostMapping("/submit")
  public String reviewSubmit(String conclusion, String acceptanceNo, HttpSession session) {
    //    判断是初筛审核还是复检审核
    String operation = sampleInfoRepository.findByAcceptanceNo(acceptanceNo).getOperation();

    //    如果该样品的下一步操作是初筛审核，说明本轮是“初筛审核”
    //    判断筛查结论是阳性还是阴性
    if (operation.equals("初筛审核") && conclusion.equals("HIV感染待确定")) {
      sampleInfoRepository.updateStatusAndOperation("初筛阳性，待复检", "复检1",
        "/detectionInput/", acceptanceNo);

      // 初筛阳性，需要进一步复检
      sampleInfoRepository.updateConclusion(conclusion, acceptanceNo);
      return "redirect:/samplesToBeTested";

    } else {
      sampleInfoRepository.updateStatusAndOperation("筛查结束，待导出", "导出报表",
        "/formExport/", acceptanceNo);

      // 设置检测者姓名
      sampleInfoRepository.updateReviewer(
        (String) session.getAttribute("username"),acceptanceNo);

      // 设置筛查结论
      sampleInfoRepository.updateConclusion(conclusion, acceptanceNo);

      // 筛查结束，可以直接导出报表

      return "redirect:/inspectionForm";

    }

// 无论如何，都是审核过了
  }
}
