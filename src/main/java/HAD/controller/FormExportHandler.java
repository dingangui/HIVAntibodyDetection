package HAD.controller;

import HAD.entity.*;
import HAD.mapper.DetectionResultRepository;
import HAD.mapper.SampleInfoRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* 负责导出报表的控制器 */
@Controller
@RequestMapping("/formExport")
public class FormExportHandler {
  @Resource
  private SampleInfoRepository sampleInfoRepository;
  @Resource
  private DetectionResultRepository detectionResultRepository;

  //  跳转到送检单预览列表，显示哪些样品可以到处送检单
  @GetMapping("/{acceptanceNo}")
  public String inspectionForm(@PathVariable String acceptanceNo, Model model) {
    SampleInfo sampleInfo = sampleInfoRepository.findByAcceptanceNo(acceptanceNo);
    List<DetectionResult> detectionResults = detectionResultRepository.findByAcceptanceNo(acceptanceNo);
    if (detectionResults.size() == 2) {
      detectionResults.add(new DetectionResult());
    }
    if (detectionResults.size() == 1) {
      detectionResults.add(new DetectionResult());
      detectionResults.add(new DetectionResult());
    }
    model.addAttribute("sampleInfo", sampleInfo);
    model.addAttribute("detectionResults", detectionResults);
    return "forms/inspectionForm";
  }

  // 导出ELISA原始记录
  /*
   *   用的是XWPFDocument，版本是3.17，注意要统一版本
   *   XWPFDocument支持docx文件，而HWPFDocument不支持
   *   之所以用XWPFDocument是因为后者不能正确导出word中8列以上的表格
   *   XWPFDocument现在用着也有一些问题，
   *   1. 支撑代码太多，整个结构变得复杂了起来
   *   2. 部分${xxx}变量不能正确被替换掉，模板中输入变量的顺序有一定的限制，
   *      要按照字符顺序输入，不能先把{}一起按出来
   * */

  /*
  * update
  * 改用spire doc了XWPFDocument确实难用
  * */
  @PostMapping("exportELISAForm")
  public String exportELISAForm(ELISAFormInfo elisaFormInfo, HttpServletResponse response) throws IOException {

    Document document = new Document("src/main/resources/static/file/ELISAForm.docx");


    List<SampleInfo> sampleInfos = sampleInfoRepository.findPrimaryScreeningPassed();

    // 设置样品编号
    String sampleNumberRange = "";
    sampleNumberRange += sampleInfos.get(0).getAcceptanceNo() + "～";
    sampleNumberRange += sampleInfos.get(sampleInfos.size() - 1).getAcceptanceNo();

    //修改模板内容
    document.replace("${sampleNumberRange}", sampleNumberRange, false, true);
    document.replace("${experimentLocation}", elisaFormInfo.getExperimentLocation(), false, true);
    document.replace("${device1}", elisaFormInfo.getDevice1(), false, true);
    document.replace("${device2}", elisaFormInfo.getDevice2(), false, true);
    document.replace("${device3}", elisaFormInfo.getDevice3(), false, true);
    document.replace("${reagentName}", elisaFormInfo.getReagentName(), false, true);
    document.replace("${manufacturer}", elisaFormInfo.getManufacturer(), false, true);
    document.replace("${batchNo}", elisaFormInfo.getBatchNo(), false, true);
    document.replace("${effectiveDate}", elisaFormInfo.getEffectiveDate(), false, true);
    document.replace("${temperature}", elisaFormInfo.getTemperature(), false, true);
    document.replace("${period1}", elisaFormInfo.getPeriod1(), false, true);
    document.replace("${period2}", elisaFormInfo.getPeriod2(), false, true);
    document.replace("${period3}", elisaFormInfo.getPeriod3(), false, true);
    document.replace("${environmentTemperature}", elisaFormInfo.getEnvironmentTemperature(), false, true);
    document.replace("${humidity}", elisaFormInfo.getHumidity(), false, true);

    int i = 1;
    int m = sampleInfos.size();
    for (SampleInfo sampleInfo : sampleInfos) {
      document.replace("${sampleNumber" + i + "}", sampleInfo.getAcceptanceNo(), false, true);
      i++;
    }
    for (i = m + 1; i < m + 4; i++) {
      document.replace("${sampleNumber" + i + "}", "QC", false, true);

    }
    for (i = m + 4; i < 83; i++) {
      document.replace("${sampleNumber" + i + "}", "", false, true);
    }

    String date;
    Calendar now = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
    date = sdf.format(now.getTime());
    document.replace("${date}", date, false, true);


    document.saveToFile("src/main/resources/static/file/ELISATemp.docx", FileFormat.Docx_2013);
    //把doc输出到输出流中

    // 浏览器下载word
    String fileName = "HIV抗体检测原始记录（酶联免疫法） - " + date + ".docx";//被下载文件的名称
    fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
    File file = new File("src/main/resources/static/file/ELISATemp.docx");
    if (downloadFile(file, response, fileName)) {
      return "forms/ELISAForm";
    }
    return null;
  }

  // 针对导出送检表的支撑代码，第 i 次检测的检测结果录入
  public void detectionResultsInput(Range range, DetectionResult detectionResult, int i) {
    // 先分析检测方法
    String detectionMethod = detectionResult.getDetectionMethod();
    switch (detectionMethod) {
      case "ELISA":
        range.replaceText("${detectionMethod" + i + "1}", "√");
        break;
      case "PA":
        range.replaceText("${detectionMethod" + i + "2}", "√");
        break;
      case "化学发光":
        range.replaceText("${detectionMethod" + i + "3}", "√");
        break;
      case "RT":
        range.replaceText("${detectionMethod" + i + "4}", "√");
        break;
      default:
        range.replaceText("${detectionMethod" + i + "5}", detectionMethod);
        break;
    }
    range.replaceText("${detectionMethod" + i + "1}", "□");
    range.replaceText("${detectionMethod" + i + "2}", "□");
    range.replaceText("${detectionMethod" + i + "3}", "□");
    range.replaceText("${detectionMethod" + i + "4}", "□");
    range.replaceText("${detectionMethod" + i + "5}", "_____");

    // 分析检测日期
    String detectionDate = detectionResult.getDetectionDate();
    range.replaceText("${detectionDateY" + i + "}", detectionDate.substring(0, 4));
    range.replaceText("${detectionDateM" + i + "}", detectionDate.substring(5, 7));
    range.replaceText("${detectionDateD" + i + "}", detectionDate.substring(8, 10));

    //    分析试剂厂家
    range.replaceText("${manufacture" + i + "}", detectionResult.getManufacture());

//    批号
    range.replaceText("${batchNo" + i + "}", detectionResult.getBatchNo());
//  有效日期
    range.replaceText("${validDate" + i + "}", detectionResult.getValidDate());
//  临界值
    range.replaceText("${criticalValue" + i + "}", detectionResult.getCriticalValue());
//  样本检测值
    range.replaceText("${detectionValue" + i + "}", detectionResult.getDetectionValue());

    range.replaceText("${batchNo" + i + "}", detectionResult.getBatchNo());

//    检测结果
    String detectionResult_ = detectionResult.getDetectionResult();
    if (detectionResult_.equals("有反应")) {
      range.replaceText("${detectionResult" + i + "1}", "√");
      range.replaceText("${detectionResult" + i + "2}", "□");
    } else {
      range.replaceText("${detectionResult" + i + "1}", "□");
      range.replaceText("${detectionResult" + i + "2}", "√");
    }
  }

  //  针对导出送检表的支撑代码，第 i 次的检测结果填入空白信息
  public void detectionResultsClean(Range range, int i) {

//  检测方法填入空白信息
    range.replaceText("${detectionMethod" + i + "1}", "□");
    range.replaceText("${detectionMethod" + i + "2}", "□");
    range.replaceText("${detectionMethod" + i + "3}", "□");
    range.replaceText("${detectionMethod" + i + "4}", "□");
    range.replaceText("${detectionMethod" + i + "5}", "____");

//    检测结果填入空白信息
    range.replaceText("${detectionDateY" + i + "}", "   ");
    range.replaceText("${detectionDateM" + i + "}", "   ");
    range.replaceText("${detectionDateD" + i + "}", "   ");

    //    分析试剂厂家
    range.replaceText("${manufacture" + i + "}", "");

//    批号
    range.replaceText("${batchNo" + i + "}", "");
//  有效日期
    range.replaceText("${validDate" + i + "}", "");
//  临界值
    range.replaceText("${criticalValue" + i + "}", "");
//  样本检测值
    range.replaceText("${detectionValue" + i + "}", "");

//    检测结果
    range.replaceText("${detectionResult" + i + "1}", "□");
    range.replaceText("${detectionResult" + i + "2}", "□");

  }

  // 导出样品送检表
  /*
   * 1.0版本代码，直接操作doc
   * 用HWPDocument
   * */
  @PostMapping("/exportInspectionForm")
  public String exportInspectionForm(String acceptanceNo, HttpServletResponse response) throws IOException {

    SampleInfo sampleInfo = sampleInfoRepository.findByAcceptanceNo(acceptanceNo);
    List<DetectionResult> detectionResults = detectionResultRepository.findByAcceptanceNo(acceptanceNo);

    String templatePath = "src/main/resources/static/file/inspectionForm.doc";
    InputStream is = new FileInputStream(templatePath);
    OutputStream os;
    HWPFDocument doc = new HWPFDocument(is);
    Range range = doc.getRange();

    //把range范围内的${acceptanceNo}替换为当前的编号
    range.replaceText("${acceptanceNo}", sampleInfo.getAcceptanceNo());
    range.replaceText("${inspectionUnit}", sampleInfo.getInspectionUnit());
    range.replaceText("${inspectDateY}", sampleInfo.getInspectionDate().substring(0, 4));
    range.replaceText("${inspectDateM}", sampleInfo.getInspectionDate().substring(5, 7));
    range.replaceText("${inspectDateD}", sampleInfo.getInspectionDate().substring(8, 10));

    //    送检样品
    switch (sampleInfo.getSampleType()) {
      case "全血":
        range.replaceText("${sampleType1}", "√");
        break;
      case "血浆":
        range.replaceText("${sampleType2}", "√");
        break;
      case "血清":
        range.replaceText("${sampleType3}", "√");
        break;
      case "口腔黏膜渗出液":
        range.replaceText("${sampleType4}", "√");
        break;
      case "尿":
        range.replaceText("${sampleType5}", "√");
        break;
      default:
        range.replaceText("${sampleType6}", sampleInfo.getSampleType());
        break;
    }
    range.replaceText("${sampleType1}", "□");
    range.replaceText("${sampleType2}", "□");
    range.replaceText("${sampleType3}", "□");
    range.replaceText("${sampleType4}", "□");
    range.replaceText("${sampleType5}", "□");
    range.replaceText("${sampleType6}", "____");

    //    其他样品信息
    range.replaceText("${patientType}", sampleInfo.getPatientType());
    range.replaceText("${patientName}", sampleInfo.getPatientName());
    range.replaceText("${sex}", sampleInfo.getSex());
    range.replaceText("${age}", sampleInfo.getAge());
    range.replaceText("${profession}", sampleInfo.getProfession());
    range.replaceText("${country}", sampleInfo.getCountry());
    range.replaceText("${nation}", sampleInfo.getNation());
    range.replaceText("${marriage}", sampleInfo.getMarriage());
    range.replaceText("${educationalLevel}", sampleInfo.getEducationalLevel());
    range.replaceText("${IDNumber}", sampleInfo.getIDNumber());
    range.replaceText("${phone}", sampleInfo.getPhone());
    range.replaceText("${presentAddress}", sampleInfo.getPresentAddress());
    range.replaceText("${residenceAddress}", sampleInfo.getResidenceAddress());


    //  检测结果
    if (detectionResults.size() == 1) {
      detectionResultsInput(range, detectionResults.get(0), 1);
      detectionResultsClean(range, 2);
      detectionResultsClean(range, 3);
    }
    if (detectionResults.size() == 2) {
      detectionResultsInput(range, detectionResults.get(0), 1);
      detectionResultsInput(range, detectionResults.get(1), 2);
      detectionResultsClean(range, 3);

    }
    if (detectionResults.size() == 3) {
      detectionResultsInput(range, detectionResults.get(0), 1);
      detectionResultsInput(range, detectionResults.get(1), 2);
      detectionResultsInput(range, detectionResults.get(2), 3);
    }

//筛查结论
    if (sampleInfo.getConclusion().equals("HIV感染待确定")) {
      range.replaceText("${conclusion1}", "√");
      range.replaceText("${conclusion2}", "□");
    } else {
      range.replaceText("${conclusion1}", "□");
      range.replaceText("${conclusion2}", "√");
    }
//    检测者、审核者
    range.replaceText("${inspector}", sampleInfo.getInspector());
    range.replaceText("${reviewer}", sampleInfo.getReviewer());

    os = new FileOutputStream(new File("src/main/resources/static/file/inspectionTemp.doc"));
    //把doc输出到输出流中
    doc.write(os);
    os.close();
    is.close();

    // 浏览器下载word
    String fileName = acceptanceNo + ".doc";//被下载文件的名称
    File file = new File("src/main/resources/static/file/inspectionTemp.doc");
    if (downloadFile(file, response, fileName)) {
      sampleInfoRepository.updateStatusAndOperation("报表已导，再导出",
        "再次导出", "/formExport/", acceptanceNo);
      return "redirect:/inspectionForm";
    }
    return null;
  }

  // 导出送检表的二维码
  @PostMapping("/exportQRCode")
  public void exportQRCode(String acceptanceNo, HttpServletResponse response) {
    SampleInfo sampleInfo = sampleInfoRepository.findByAcceptanceNo(acceptanceNo);
    // 浏览器下载二维码
    generateQRCode(sampleInfo.toString());
    // 浏览器下载word
    String fileName = acceptanceNo + ".png";//被下载文件的名称
    File file = new File("src/main/resources/static/file/QRCodeTemp.png");
    downloadFile(file, response, fileName);
  }

  // 导出快速检测原始记录
  /*
   * 虽然是2.0，但是还是继续用了HWPFDocument
   * 因为XWPDocument有BUG啊，谁知道哪个替换的地方会不会又出错，稳点好
   *
   * */
  @PostMapping("/exportPARTForm")
  public String exportPARTForm(PARTFormInfo partFormInfo, HttpServletResponse response) throws IOException {

    // 找初筛通过的人
    List<SampleInfo> sampleInfos = sampleInfoRepository.findPrimaryScreeningPassed();

    // 一张表格只能打印14个样品信息，根据样品数量计算需要打印多少张
    int docxNumber = sampleInfos.size() / 14 + 1;
    int m = 0;
    for (int i = 0; i < docxNumber; i++) {
      String templatePath = "src/main/resources/static/file/PARTForm.doc";
      InputStream is = new FileInputStream(templatePath);
      OutputStream os;
      HWPFDocument doc = new HWPFDocument(is);
      Range range = doc.getRange();

      range.replaceText("${testItem}", partFormInfo.getTestItem());
      range.replaceText("${sampleName}", partFormInfo.getSampleName());
      range.replaceText("${patientType}", partFormInfo.getPatientType());
      range.replaceText("${manufacturer}", partFormInfo.getManufacturer());
      range.replaceText("${batchNo}", partFormInfo.getBatchNo());
      range.replaceText("${effectiveDate}", partFormInfo.getEffectiveDate());
      range.replaceText("${temperatureAndHumidity}", partFormInfo.getTemperatureAndHumidity());
      range.replaceText("${reactionTime}", partFormInfo.getReactionTime());

      for (int j = 1; j <= 14; j++) {
        if (m < sampleInfos.size()) {
          range.replaceText("${sampleNumber" + j + "}", sampleInfos.get(m).getAcceptanceNo());
          range.replaceText("${name" + j + "}", sampleInfos.get(m++).getPatientName());
        } else {
          range.replaceText("${sampleNumber" + j + "}", "");
          range.replaceText("${name" + j + "}", "");
        }
      }
      String date;
      Calendar now = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
      date = sdf.format(now.getTime());
      range.replaceText("${date}", date);

      os = new FileOutputStream(new File("src/main/resources/static/file/PARTTemp" + i + ".doc"));
      //把doc输出到输出流中
      doc.write(os);
      os.close();
      is.close();
    }

/*
    //获取第一个文档的路径
    String filePath1 = "src/main/resources/static/file/temp0.doc";
      //获取第二个文档的路径
      String filePath2 = "src/main/resources/static/file/temp1.doc";
      //加载第一个文档
      Document document = new Document(filePath1);

      //使用insertTextFromFile方法将第二个文档的内容插入到第一个文档
      document.insertTextFromFile(filePath2, FileFormat.Doc_Pre_97);
      //保存文档
      document.saveToFile("src/main/resources/static/file/temp0.doc", FileFormat.Docx_2013);
*/


    // 下面，合并导出的多张PART表
    if (docxNumber > 1) {
      //获取第一个文档的路径
      String filePath1 = "src/main/resources/static/file/PARTTemp0.doc";
      for (int i = 1; i < docxNumber; i++) {
        //获取第二个文档的路径
        String filePath2 = "src/main/resources/static/file/PARTTemp" + i + ".doc";
        //加载第一个文档
        Document document = new Document(filePath1);

        //使用insertTextFromFile方法将第二个文档的内容插入到第一个文档
        document.insertTextFromFile(filePath2, FileFormat.Doc_Pre_97);
        //保存文档
        document.saveToFile("src/main/resources/static/file/PARTTemp0.doc", FileFormat.Docx_2013);
      }
    }

    String date;
    Calendar now = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
    date = sdf.format(now.getTime());
    // 浏览器下载word
    String fileName = "HIV快速检测原始记录 - " + date + ".doc";//被下载文件的名称
    fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
    File file = new File("src/main/resources/static/file/PARTTemp0.doc");
    downloadFile(file, response, fileName);
    return "forms/PARTForm";

  }

  // 导出ARCHITECT原始记录
  /*
   * 这里打算用一下spire doc，据介绍代码很简单
   * */
  @PostMapping("/exportARCHITECTForm")
  public String exportARCHITECTForm(ARCHITECTFormInfo ARCHITECTFormInfo, HttpServletResponse response) {
    // 找需要做ARCHITECT测试的人
    List<SampleInfo> sampleInfos = sampleInfoRepository.findPrimaryScreeningPassed();
    //加载Word文档
    // 设置样品编号
    String sampleNumberRange = "";
    sampleNumberRange += sampleInfos.get(0).getAcceptanceNo() + "～";
    sampleNumberRange += sampleInfos.get(sampleInfos.size() - 1).getAcceptanceNo();


    Document document = new Document("src/main/resources/static/file/ARCHITECTForm.docx");

    //使用新文本替换文档中的指定文本
    document.replace("${sampleNumberRange}", sampleNumberRange, false, true);
    document.replace("${experimentLocation}", ARCHITECTFormInfo.getExperimentLocation(), false, true);
    document.replace("${device1}", ARCHITECTFormInfo.getDevice1(), false, true);
    document.replace("${device2}", ARCHITECTFormInfo.getDevice2(), false, true);
    document.replace("${device3}", ARCHITECTFormInfo.getDevice3(), false, true);
    document.replace("${reagentName}", ARCHITECTFormInfo.getReagentName(), false, true);
    document.replace("${manufacturer}", ARCHITECTFormInfo.getManufacturer(), false, true);
    document.replace("${batchNo}", ARCHITECTFormInfo.getBatchNo(), false, true);
    document.replace("${effectiveDate}", ARCHITECTFormInfo.getEffectiveDate(), false, true);
    document.replace("${environmentTemperature}", ARCHITECTFormInfo.getEnvironmentTemperature(), false, true);
    document.replace("${humidity}", ARCHITECTFormInfo.getHumidity(), false, true);

    int i = 1;
    int m = sampleInfos.size();
    for (SampleInfo sampleInfo : sampleInfos) {
      document.replace("${sampleNumber" + i + "}", sampleInfo.getAcceptanceNo(),false,true);
      i++;
    }
    for (i = m + 1; i < 93; i++) {
      document.replace("${sampleNumber" + i + "}", "",false,true);
    }


    String date;
    Calendar now = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
    date = sdf.format(now.getTime());

    document.replace("${detectionDate}", date, false, true);

    document.saveToFile("src/main/resources/static/file/ARCHITECTTemp.docx", FileFormat.Docx_2013);

    // 浏览器下载word
    String fileName = "ARCHITECT原始记录 - " + date + ".docx";//被下载文件的名称
    fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
    File file = new File("src/main/resources/static/file/ARCHITECTTemp.docx");
    downloadFile(file, response, fileName);
    return "forms/ARCHITECTForm";

  }


  //  浏览器下载文件
  public Boolean downloadFile(File file, HttpServletResponse response, String fileName) {
    if (file.exists()) {
      response.setContentType("application/force-download");// 设置强制下载不打开
      response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
      byte[] buffer = new byte[1024];
      FileInputStream fis = null;
      BufferedInputStream bis = null;
      try {
        fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);
        OutputStream outputStream = response.getOutputStream();
        int i = bis.read(buffer);
        while (i != -1) {
          outputStream.write(buffer, 0, i);
          i = bis.read(buffer);
        }
        return true;
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (bis != null) {
          try {
            bis.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (fis != null) {
          try {
            fis.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return false;
  }

  //  生成二维码
  public void generateQRCode(String msg) {
    String path = "src/main/resources/static/file/QRCodeTemp.png";
    try {
      File file = new File(path);
      OutputStream ous = new FileOutputStream(file);
      if (StringUtils.isEmpty(msg))
        return;
      String format = "png";
      MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
      Map<EncodeHintType, String> map = new HashMap<EncodeHintType, String>();
      //设置编码 EncodeHintType类中可以设置MAX_SIZE， ERROR_CORRECTION，CHARACTER_SET，DATA_MATRIX_SHAPE，AZTEC_LAYERS等参数
      map.put(EncodeHintType.CHARACTER_SET, "UTF-8");
      map.put(EncodeHintType.MARGIN, "2");
      //生成二维码
      BitMatrix bitMatrix = new MultiFormatWriter().encode(msg, BarcodeFormat.QR_CODE, 300, 300, map);
      MatrixToImageWriter.writeToStream(bitMatrix, format, ous);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // 以下
  // 以下是XWPDocument支撑代码

  /**
   * 替换段落里面的变量
   *
   * @param doc    要替换的文档
   * @param params 参数
   */
  private void replaceInPara(XWPFDocument doc, Map<String, Object> params) {
    Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
    XWPFParagraph para;
    while (iterator.hasNext()) {
      para = iterator.next();
      this.replaceInPara(para, params);
    }
  }

  /**
   * 替换段落里面的变量
   *
   * @param para   要替换的段落
   * @param params 参数
   */
  private void replaceInPara(XWPFParagraph para, Map<String, Object> params) {
    List<XWPFRun> runs;
    Matcher matcher;
    if (this.matcher(para.getParagraphText()).find()) {
      runs = para.getRuns();
      for (int i = 0; i < runs.size(); i++) {
        XWPFRun run = runs.get(i);
        String runText = run.toString();
        matcher = this.matcher(runText);
        if (matcher.find()) {
          while ((matcher = this.matcher(runText)).find()) {
            runText = matcher.replaceFirst(String.valueOf(params.get(matcher.group(1))));
          }
          //直接调用XWPFRun的setText()方法设置文本时，在底层会重新创建一个XWPFRun，把文本附加在当前文本后面，
          //所以我们不能直接设值，需要先删除当前run,然后再自己手动插入一个新的run。
          para.removeRun(i);
          para.insertNewRun(i).setText(runText);
        }
      }
    }
  }

  /**
   * 替换表格里面的变量
   *
   * @param doc    要替换的文档
   * @param params 参数
   */
  private void replaceInTable(XWPFDocument doc, Map<String, Object> params) {
    Iterator<XWPFTable> iterator = doc.getTablesIterator();
    XWPFTable table;
    List<XWPFTableRow> rows;
    List<XWPFTableCell> cells;
    List<XWPFParagraph> paras;
    while (iterator.hasNext()) {
      table = iterator.next();
      rows = table.getRows();
      for (XWPFTableRow row : rows) {
        cells = row.getTableCells();
        for (XWPFTableCell cell : cells) {
          paras = cell.getParagraphs();
          for (XWPFParagraph para : paras) {
            this.replaceInPara(para, params);
          }
        }
      }
    }
  }

  /**
   * 正则匹配字符串
   *
   * @param str
   * @return
   */
  private Matcher matcher(String str) {
    Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(str);
    return matcher;
  }

}

