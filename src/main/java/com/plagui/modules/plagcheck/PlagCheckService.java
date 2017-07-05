package com.plagui.modules.plagcheck;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plagui.config.Constants;
import com.plagui.modules.UtilService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jagrut on 20-06-2017.
 * Service method to process logic for plagiarism check
 */
@Service
public class PlagCheckService {
    private final Logger log = LoggerFactory.getLogger(PlagCheckService.class);
    private UtilService utilService;

    public PlagCheckService(UtilService utilService) {
        this.utilService = utilService;
    }

    /**
     * Breaks down the PDF file into plain text, cleans the text and generates minhash. Calls callPlagDetectionModule()
     * to get results from plagdetection module.
     * @param plagCheckDoc pdf file to check for plagiarism
     * @param checkUnpublishedWork if true, check unpublished work stream
     * @return {PlagCheckResultDTO} containing the result in JSON string. Will contain all matched along with
     *                              Jaccard similarity
     */
    public PlagCheckResultDTO plagCheckForDoc(MultipartFile plagCheckDoc, boolean checkUnpublishedWork) {
        log.info("Processing the pdf file for plagiarism check");
        //Calculate sha256 hash for document
        List<String> sha256DocHash = new ArrayList<>();
        try {
            sha256DocHash = utilService.generateSHA256HashFromObjects(Arrays.asList(plagCheckDoc.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Parse pdf file for text and generate min hash
        String textFromPdf = utilService.parsePdf(plagCheckDoc);
        String cleanedText = utilService.cleanText(textFromPdf);
        cleanedText = utilService.removeAllWhiteSpaces(cleanedText);
        List<String> allShingles = new ArrayList<>();
        allShingles.addAll(utilService.createShingles(Constants.SHINGLE_LENGTH, cleanedText));
        int[] minHashFromShingles = utilService.generateMinHashSignature(allShingles);

        //Parse pdf file for images and generate sha256 hash
        List<byte[]> imagesFromPdfAsByte = utilService.extractImageFromPdfFile(plagCheckDoc);
        List<String> sha256HashOfImages = utilService.generateSHA256HashFromObjects(imagesFromPdfAsByte);
        PlagCheckResultDTO response = callPlagDetectionModule(sha256DocHash.get(0), Arrays.asList(ArrayUtils.toObject(minHashFromShingles)), sha256HashOfImages, checkUnpublishedWork);
        response.setPlagCheckDocFileName(plagCheckDoc.getOriginalFilename());
        return response;
    }

    /**
     * Makes HTTP call to Plag detection module to fetch similar documents existing in the plagchain
     * @param docHash the sha-256 document hash
     * @param minHashList the min hash generated from the test file
     * @param imageHashList the list of sha-256 hash of the images
     * @param checkUnpublishedWork if true, check unpublished work stream
     * @return {PlagCheckResultDTO} containing the result in JSON string. Will contain all matched along with
     *                              Jaccard similarity
     */
    public PlagCheckResultDTO callPlagDetectionModule(String docHash, List<Integer> minHashList, List<String> imageHashList, boolean checkUnpublishedWork) {
        log.info("Making HTTP call to Plagdetection module");
        PlagCheckResultDTO responseFromPlagdetection = new PlagCheckResultDTO();
        Gson gson = new GsonBuilder().create();
        String minHashListAsString = gson.toJson(minHashList);
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(Constants.PLAGDETECTION_URL);


        List<NameValuePair> paramList = new ArrayList<>();
        paramList.add(new BasicNameValuePair("docHash", docHash));
        paramList.add(new BasicNameValuePair("textHashList", minHashListAsString.substring(1, minHashListAsString.length() - 1)));
        paramList.add(new BasicNameValuePair("imageHashList", gson.toJson(imageHashList)));
        paramList.add(new BasicNameValuePair("checkUnpublishedWorkStream", gson.toJson(checkUnpublishedWork)));

        try {
            postRequest.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
            HttpResponse response = httpClient.execute(postRequest);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null)
                result.append(line);
            responseFromPlagdetection.setResultJsonString(result.toString());
        } catch (HttpHostConnectException e) {
            responseFromPlagdetection.setError("Plag detection server not available");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseFromPlagdetection;
    }
}
