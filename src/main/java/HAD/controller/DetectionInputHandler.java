package HAD.controller;

import HAD.entity.DetectionResult;
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
//处理检测结果输入的控制器
@RequestMapping("/detectionInput")
public class DetectionInputHandler {
  @Resource
  private SampleInfoRepository sampleInfoRepository;
  @Resource
  private DetectionResultRepository detectionResultRepository;

  //  跳转到某个样品的输入检测结果页面
  @GetMapping("/{acceptanceNo}")
  public String detectionInput(@PathVariable String acceptanceNo, Model model) {

//    根据样品受理编号，查询该样品已检测信息
    List<DetectionResult> detectionResults =
      detectionResultRepository.findByAcceptanceNo(acceptanceNo);

//    判断该样品已经有几次检测记录了
//    < 3 次，显示 提交检测结果 的div，同时设置好新一轮检测的名字
    if (detectionResults.size() < 3) {
      model.addAttribute("detectionNeeds", "true");
      if (detectionResults.size() == 0)
        model.addAttribute("detectionName", "初筛");
      if (detectionResults.size() == 1)
        model.addAttribute("detectionName", "复检1");
      if (detectionResults.size() == 2)
        model.addAttribute("detectionName", "复检2");
    } else {
      model.addAttribute("detectionNeeds", "false");
    }

    //    向前端展示样品基本信息
    model.addAttribute("sampleInfo",
      sampleInfoRepository.findByAcceptanceNo(acceptanceNo));

    //    向前端展示样品检测结果
    model.addAttribute("detectionResults", detectionResults);

    return "detectionInput";
  }

  //  提交检测结果
  @PostMapping("/detectionResultSubmit")
  public String detectionResultSubmit(DetectionResult detectionResult,HttpSession session) {

    //    获取本轮检测结果样品的受理编号
    String acceptanceNo = detectionResult.getAcceptanceNo();

    //    更新样品 状态、操作、链接、操作人姓名
    switch (detectionResult.getDetectionName()) {
      case "初筛": {
        sampleInfoRepository.updateStatusAndOperation
          ("初筛通过，待审核", "初筛审核",
            "/review/", acceptanceNo);
        sampleInfoRepository.updateInspector(
          (String) session.getAttribute("username"),acceptanceNo);
        break;
      }case "复检1":
        sampleInfoRepository.updateStatusAndOperation
          ("复检一次，再复检", "复检2",
            "/detectionInput/", acceptanceNo);
        break;
      case "复检2":{
        sampleInfoRepository.updateStatusAndOperation
          ("复检两次，待审核", "复检审核",
            "/review/", acceptanceNo);
        sampleInfoRepository.updateInspector(
          (String) session.getAttribute("username"),acceptanceNo);
        break;
      }
    }
    detectionResultRepository.save(detectionResult);
    return "redirect:/pendingSamples";
  }
}
