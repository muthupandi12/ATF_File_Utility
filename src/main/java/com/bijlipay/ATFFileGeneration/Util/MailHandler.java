package com.bijlipay.ATFFileGeneration.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.text.ParseException;
import java.util.Date;

@Component
public class MailHandler {

    @Autowired
    private JavaMailSender sender;

    @Value("${atf.file.updated.path}")
    private String atfFileUpdatedPath;

    @Value("${atf.file.report.path}")
    private String atfFileReportPath;
    public void sendATFRuleDataMail(String date) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("reports@bijlipay.co.in");
            helper.setTo(Constants.SENT_TO);
            helper.setSubject("ATF Rules Executed Report -" + date + "");
            helper.setText(String.format("Dear All," + "\n\n" + "Greetings from Bijlipay!!" + "\n\n" + "" +
                    " ATF Rules Executed Report - "+ date +" \n\n" + "Regards," + "\n" + "Team " +
                    "Bijlipay\n\n Note :" +
                    "This is an auto generated email. Please do not respond to this email id.\n For any queries You can call us @ 1800 4200 235 or" +
                    " write to us @ service@bijlipay.co.in."));
            FileSystemResource file1 = new FileSystemResource(atfFileUpdatedPath + "ATF_UPI_Txn_Count_Report-" + date + ".xlsx");

//            FileSystemResource file1 = new FileSystemResource(atfFileUpdatedPath + "ATF_Rules_Executed_Report-" + date + ".txt");
            helper.addAttachment(file1.getFilename(), file1);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendValidatedATFDataMail(String date) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("reports@bijlipay.co.in");
            helper.setTo(Constants.SENT_VALIDATED_ATF);
            helper.setSubject("Validated ATF Report -" + date + "");
            helper.setText(String.format("Dear All," + "\n\n" + "Greetings from Bijlipay!!" + "\n\n" + "" +
                    " Validated ATF Report - "+ date +" \n\n" + "Regards," + "\n" + "Team " +
                    "Bijlipay\n\n Note :" +
                    "This is an auto generated email. Please do not respond to this email id.\n For any queries You can call us @ 1800 4200 235 or" +
                    " write to us @ service@bijlipay.co.in."));

            FileSystemResource file1 = new FileSystemResource(atfFileUpdatedPath + "Validated_ATF_Report-" + date + ".txt");
            helper.addAttachment(file1.getFilename(), file1);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public void sendReversalMail(String date) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("reports@bijlipay.co.in");
            helper.setTo(Constants.SENT_TO);
            helper.setSubject("ATF ACK Reversal Report-" + date + "");
            helper.setText(String.format("Dear All," + "\n\n" + "Greetings from Bijlipay!!" + "\n\n" + "" +
                    " ATF ACK Reversal Report - "+ date +" \n\n" + "Regards," + "\n" + "Team " +
                    "Bijlipay\n\n Note :" +
                    "This is an auto generated email. Please do not respond to this email id.\n For any queries You can call us @ 1800 4200 235 or" +
                    " write to us @ service@bijlipay.co.in."));

            FileSystemResource file1 = new FileSystemResource(atfFileUpdatedPath + "ATF_ACK_Reversal_Report-" + date + ".txt");
            helper.addAttachment(file1.getFilename(), file1);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendTotalFileMail() {
        try {
            Date date = new Date();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("reports@bijlipay.co.in");
            helper.setTo(Constants.SENT_TOTAL_MAIL);
            helper.setSubject("Switch- ATF & Settlement & TxnList File Missing Data Report " + date);
            helper.setText(String.format("Dear All," + "\n\n" + "Greetings from Bijlipay!!" + "\n\n" + "" +
                    " ATF & Settlement & TxnList File Missing Data Report  - " + DateUtil.currentDate("yyyy-MM-dd") + "\n\n" + "Regards," + "\n" + "Team " +
                    "Bijlipay\n\n Note :" +
                    "This is an auto generated email. Please do not respond to this email id.\n For any queries You can call us @ 1800 4200 235 or" +
                    " write to us @ service@bijlipay.co.in."));

//            String allTxnMissingData =atfFileUpdatedPath + "All_Txn_File_Missing_Data-" + DateUtil.previousDate() + ".csv";
//            String settlementFileMissingData =atfFileUpdatedPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_Missing_Data_" + DateUtil.currentDate2() + ".csv";
//            String txnAndSettlementMissingData =atfFileUpdatedPath + "AllTxnAndSettlementMissingData_" + DateUtil.currentDate2() + ".csv";
//
//            String allTxnMissingDataFileName =String.format("All_Txn_File_Missing_Data-" + DateUtil.previousDate() + ".csv");
//            String settlementFileMissingDataFileName =String.format("Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_Missing_Data_" + DateUtil.currentDate2() + ".csv");
//            String txnAndSettlementMissingDataFileName =String.format("AllTxnAndSettlementMissingData_" + DateUtil.currentDate2() + ".csv");
//
//            helper.addAttachment(allTxnMissingDataFileName,new File(allTxnMissingData));
//            helper.addAttachment(settlementFileMissingDataFileName,new File(settlementFileMissingData));
//            helper.addAttachment(txnAndSettlementMissingDataFileName,new File(txnAndSettlementMissingData));
//


            FileSystemResource allTxnFile = new FileSystemResource(atfFileUpdatedPath + "All_Txn_File_Missing_Data-" + DateUtil.previousDate() + ".csv");
            FileSystemResource settlementFile = new FileSystemResource(atfFileUpdatedPath + "Response_BIGILIPAY_AXIS_H2H_SETTLEMENT_Missing_Data_" + DateUtil.currentDate2() + ".csv");
            FileSystemResource txnAndSettlementFile = new FileSystemResource(atfFileUpdatedPath + "AllTxnAndSettlementMissingData_" + DateUtil.currentDate2() + ".csv");
            helper.addAttachment(allTxnFile.getFilename(),allTxnFile);
            helper.addAttachment(settlementFile.getFilename(),settlementFile);
            helper.addAttachment(txnAndSettlementFile.getFilename(),txnAndSettlementFile);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public void sendPhonePeSettlementReports(String date) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("reports@bijlipay.co.in");
            helper.setTo(Constants.SENT_TO_PHONEPE_REPORTS);
            helper.setSubject("PhonePe Settlement Reports -" + date + "");
            helper.setText(String.format("Dear All," + "\n\n" + "Greetings from Bijlipay!!" + "\n\n" + "" +
                    " PhonePe Settlement Reports - - "+ date +" \n\n" + "Regards," + "\n" + "Team " +
                    "Bijlipay\n\n Note :" +
                    "This is an auto generated email. Please do not respond to this email id.\n For any queries You can call us @ 1800 4200 235 or" +
                    " write to us @ service@bijlipay.co.in."));

            FileSystemResource atfMissingDataFile = new FileSystemResource(atfFileUpdatedPath + "PhonePe_Missing_Response_IN_Settlement_File_" + date + ".csv");
            FileSystemResource settlementMissingDataFile = new FileSystemResource(atfFileUpdatedPath + "PhonePe_Missing_Response_IN_ATF_File_" + date + ".csv");
            FileSystemResource settlementDataFile = new FileSystemResource(atfFileUpdatedPath + "PhonePe_Final_Settlement_File_" + date + ".csv");
            FileSystemResource updatedAtfCountFile = new FileSystemResource(atfFileUpdatedPath + "ATF_Rules_Data_Count-" + date + ".txt");

            helper.addAttachment(atfMissingDataFile.getFilename(), atfMissingDataFile);
            helper.addAttachment(settlementMissingDataFile.getFilename(), settlementMissingDataFile);
            helper.addAttachment(settlementDataFile.getFilename(), settlementDataFile);
            helper.addAttachment(updatedAtfCountFile.getFilename(), updatedAtfCountFile);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendGeneratedATFToMail(String currentDateTime) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("reports@bijlipay.co.in");
            helper.setTo(Constants.SENT_ATF_MAIL);
            helper.setSubject("ATF Generation Report-" + currentDateTime + "");
            helper.setText(String.format("Dear All," + "\n\n" + "Greetings from Bijlipay!!" + "\n\n" + "" +
                    " ATF Generation Report - "+ currentDateTime +" \n\n" + "Regards," + "\n" + "Team " +
                    "Bijlipay\n\n Note :" +
                    "This is an auto generated email. Please do not respond to this email id.\n For any queries You can call us @ 1800 4200 235 or" +
                    " write to us @ service@bijlipay.co.in."));

            FileSystemResource file1 = new FileSystemResource(atfFileUpdatedPath + "All_Txn_File_"+currentDateTime+".csv");
            helper.addAttachment(file1.getFilename(), file1);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
