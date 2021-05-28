package HAD.controller;


//处理首页样品列表的Controller

import HAD.entity.SampleInfo;
import HAD.mapper.DetectionResultRepository;
import HAD.mapper.SampleInfoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@Controller
public class SampleListHandle {

  @Resource
  private SampleInfoRepository sampleInfoRepository;
  @Resource
  private DetectionResultRepository detectionResultRepository;

  //  首页默认显示
  //  所有待处理的样品列表
  @RequestMapping("/pendingSamples")
  public String pendingSamples(Model model) {
    List<SampleInfo> sampleInfos = sampleInfoRepository.findPendingSamples();
    model.addAttribute("list",sampleInfos);
    return "index";
  }

  //点击“录入样品信息”，跳转到输入信息页面
  @RequestMapping("/infoInput")
  public String infoInputSamples() {
    return "infoInput";
  }

//  点击“录入检测结果”，显示需要检测的样品
  @RequestMapping("/samplesToBeTested")
  public String samplesToBeTested(Model model) {
    List<SampleInfo> sampleInfos = sampleInfoRepository.findSamplesToBeTested();
    model.addAttribute("list",sampleInfos);
    return "index";
  }

  //  点击“审核检测结果”，显示未审核的样品
  @RequestMapping("/unreviewedSamples")
  public String unreviewedSamples(Model model) {
    List<SampleInfo> sampleInfos = sampleInfoRepository.findUnreviewedSamples();
    model.addAttribute("list",sampleInfos);
    return "index";
  }

  //  点击“修改样品信息”，显示可修改信息的样品
  @RequestMapping("/modifiableSamples")
  public String modifiableSamples(Model model) {
    List<SampleInfo> sampleInfos = sampleInfoRepository.findModifiableSamples();
    for(SampleInfo sampleInfo:sampleInfos){
      sampleInfo.setOperation("修改数据");
      sampleInfo.setHref("/modify/");
    }
    model.addAttribute("list",sampleInfos);
    return "index";
  }
  //  点击“导出HIV抗体筛查检测报告（送检单）”，显示可导出报表的样品列表
  @RequestMapping("/inspectionForm")
  public String inspectionForm(Model model) {
    List<SampleInfo> sampleInfos = sampleInfoRepository.findExportableSamples();
    model.addAttribute("list",sampleInfos);
    return "index";
  }

  // 跳转到ELISA检测原始记录预览
  @RequestMapping("/ELISAForm")
  public String ELISAForm(Model model){
    // 找初筛通过的人
    List<SampleInfo> sampleInfos = sampleInfoRepository.findPrimaryScreeningPassed();

    // 设置当前日期
    String date;
    Calendar now = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
    date = sdf.format(now.getTime());

    if(sampleInfos.size()>0) {
      // 设置样品编号
      String sampleNumberRange = "";
      sampleNumberRange += sampleInfos.get(0).getAcceptanceNo() + "～";
      sampleNumberRange += sampleInfos.get(sampleInfos.size() - 1).getAcceptanceNo();
      model.addAttribute("sampleNumberRange", sampleNumberRange);
    }
    else{
      model.addAttribute("sampleNumberRange", "今日无样品需要测验");
      // 设置当前日期
    }
    model.addAttribute("date", date);
    return "forms/ELISAForm";
  }

  // 跳转到快速检测原始记录
  @RequestMapping("/PARTForm")
  public String PARTForm(Model model){
    return "forms/PARTForm";
  }

  // 跳转到ARCHITECT原始记录
  @SuppressWarnings("DuplicatedCode")
  @RequestMapping("/ARCHITECTForm")
  public String ARCHITECTForm(Model model){
    // 找初筛通过的人
    List<SampleInfo> sampleInfos = sampleInfoRepository.findPrimaryScreeningPassed();

    // 设置当前日期
    String date;
    Calendar now = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
    date = sdf.format(now.getTime());

    if(sampleInfos.size()>0) {
      // 设置样品编号
      String sampleNumberRange = "";
      sampleNumberRange += sampleInfos.get(0).getAcceptanceNo() + "～";
      sampleNumberRange += sampleInfos.get(sampleInfos.size() - 1).getAcceptanceNo();
      model.addAttribute("sampleNumberRange", sampleNumberRange);
    }
    else{
      model.addAttribute("sampleNumberRange", "今日无样品需要测验");
      // 设置当前日期
    }
    model.addAttribute("date", date);
    return "forms/ARCHITECTForm";
  }
}
