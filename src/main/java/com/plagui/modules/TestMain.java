package com.plagui.modules;

import com.plagui.config.Constants;
import com.plagui.modules.uploaddocs.PlagchainUploadService;
import multichain.command.ChainCommand;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jagrut on 22-06-2017.
 */
public class TestMain {
    private static UtilService utilService = new UtilService();
    private static PlagchainUploadService plagchainUploadService = new PlagchainUploadService(utilService);

    public static void main(String[] args) {
        ChainCommand.initializeChain(Constants.CHAIN_NAME);
        File currentFile = new File("F:\\College stuffs\\Masters\\Project\\Papers\\PMC\\pan-plagiarism-corpus-2011\\external-detection-corpus\\source-document\\part1\\source-document00001.txt");
        //Collection<File> allFiles = FileUtils.listFiles(directory , FileFilterUtils.suffixFileFilter(".txt"), TrueFileFilter.TRUE);

        //System.out.println(allFiles.size());
        //for (File currentFile : allFiles) {
            if(currentFile.isFile()) {
                try {
                    String fileName = currentFile.getName();
                    String contentOfFile = FileUtils.readFileToString(currentFile, "UTF-8");
                    String success = plagchainUploadService.processAndTimestampText(fileName,"1JtjJSPG7exG5XRsqNLHR1HqePUFeSA5GL5E3A", contentOfFile,null, false);
                    System.out.println(fileName + success);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        //}
    }
}
