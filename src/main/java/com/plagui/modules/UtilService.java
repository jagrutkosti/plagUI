package com.plagui.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.plagui.config.Constants;
import com.plagui.modules.streamformats.ChainData;
import com.plagui.modules.uploaddocs.PDServersDTO;
import multichain.command.MultichainException;
import multichain.command.StreamCommand;
import multichain.object.StreamItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Jagrut on 19-06-2017.
 * Utility class that has all generic methods that can be used by any part of the project.
 */
@Service
public class UtilService {
    private final Logger log = LoggerFactory.getLogger(UtilService.class);
    private final int[] randomNumbers = new int[Constants.NUMBER_OF_RANDOM_NUMBERS];

    /**
     * !!!DO NOT MODIFY random-number.txt FILE EVER, after deployment!!!
     *
     * Initialize random numbers from file. The random numbers used should always be the same for all documents for
     * MinHash algorithm to work. So, if you delete the random-number.txt file, you need to re-index all documents.
     *
     * Read the random number file and populate the randomNumbers int array.
     * @throws IOException if error while fetching or reading file
     */
    @PostConstruct
    public void populateRandomNumbers() {
        log.info("Populating random numbers from file");
        List<String> randomNumAsString;
        try {
            randomNumAsString = IOUtils.readLines(this.getClass().getResourceAsStream(Constants.RANDOM_NUMBERS_FILE), "UTF-8");
            int i = 0;
            for(String number : randomNumAsString) {
                randomNumbers[i] = Integer.parseInt(number);
                i++;
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts text from Multipart file using iText library
     * Works only for pdf files. Check for file extension where calling this method.
     * @param pdfFile the multipart file from the user
     * @return {String} Extracted text from the PDF file
     */
    public String parsePdf(MultipartFile pdfFile) {
        log.info("Extracting text from pdf: {}", pdfFile.getOriginalFilename());
        StringJoiner extractedText = new StringJoiner(" ");
        try {
            PdfReader reader = new PdfReader(pdfFile.getInputStream());
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            TextExtractionStrategy strategy;
            for(int i = 1; i <= reader.getNumberOfPages(); i++) {

                strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                extractedText.add(strategy.getResultantText());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractedText.toString();
    }

    /**
     * Scans the entire PDF file and gets the images as byte array. The extracted images are not extracted in order.
     * Works only for pdf files. Check for file extension where calling this method.
     * @param pdfFile the Multipart pdf file from which to extract image
     * @return List of byte array objects. Each item in list is the byte representation of single image from file
     */
    public List<byte[]> extractImageFromPdfFile(MultipartFile pdfFile) {
        log.info("Extracting image from file: {}", pdfFile.getOriginalFilename());
        List<byte[]> byteArrayImages = new ArrayList<>();
        try {
            PdfReader pdfReader = new PdfReader(pdfFile.getInputStream());
            for(int i = 0; i < pdfReader.getXrefSize(); i++) {
                PdfObject pdfObject = pdfReader.getPdfObject(i);
                if(pdfObject == null || !pdfObject.isStream())
                    continue;
                PRStream prStream = (PRStream) pdfObject;
                PdfObject pdfType = prStream.get(PdfName.SUBTYPE);
                if(pdfType != null && pdfType.toString().equals(PdfName.IMAGE.toString())) {
                    byteArrayImages.add(PdfReader.getStreamBytesRaw(prStream));
                }
            }
            pdfReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayImages;
    }

    /**
     * Removes the hyphens added by the iText library. Removes any new line character.
     * Convert all characters to lowercase. Removes all punctuations.
     * @param uncleanText the text that needs to be cleaned
     * @return {List<String>} containing single sentence as whole text.
     */
    public String cleanText(String uncleanText) {
        log.info("Cleaning text.");
        //For text extracted from pdf has unnecessary line breaks and hyphen(when line break occurs in words)
        uncleanText = uncleanText.replaceAll("-\n","");
        //For removing all line breaks
        uncleanText = uncleanText.replaceAll("[\n]"," ");
        //For removing all punctuations
        uncleanText = uncleanText.replaceAll("\\p{P}", "");
        //Convert everything to lowercase
        uncleanText = uncleanText.toLowerCase();
        return uncleanText;
    }

    /**
     * Remove all white spaces as identified by Java
     * @param stringWithWhiteSpaces string containing white spaces
     * @return String without any white space
     */
    public String removeAllWhiteSpaces(String stringWithWhiteSpaces) {
        return StringUtils.deleteWhitespace(stringWithWhiteSpaces);
    }

    /**
     * Create character shingles of specified length from a sentence.
     * @param shingleLength the fixed length of all shingles
     * @param cleanedText the sentence from which shingles need to be extracted
     * @return {List<String>} list containing word shingles from the sentence
     */
    public Set<String> createShingles(int shingleLength, String cleanedText) {
        log.info("Creating shingles of length: {}", shingleLength);
        Set<String> shinglesFromSentences = new HashSet<>();
        for(int i = 0; i < cleanedText.length() - shingleLength + 1; i++) {
            shinglesFromSentences.add(cleanedText.substring(i, i + shingleLength));
        }
        return shinglesFromSentences;
    }

    /**
     * Create a list of word shingles from a single string.
     * @param shingleLength the length of each shingle
     * @param cleanedText the text from which shingle is to be created
     * @return List<String> containing all shingles
     */
    public List<String> createWordShingles(int shingleLength, String cleanedText) {
        log.info("Creating word shingles of length: {}", shingleLength);
        List<String> shinglesFromString = new ArrayList<>();
        int firstIndex = -1;
        int lastIndex = StringUtils.ordinalIndexOf(cleanedText," ", shingleLength);
        while(lastIndex < cleanedText.length() && lastIndex > 0) {
            shinglesFromString.add(cleanedText.substring(firstIndex + 1, lastIndex));
            firstIndex = cleanedText.indexOf(" ", firstIndex + 1);
            lastIndex = cleanedText.indexOf(" ", lastIndex + 1);
        }
        return shinglesFromString;
    }

    /**
     * Generates MinHash signature from the given document's shingles. Initial hash value is of Java's hashcode.
     * After that, that hashValue is XORed with random numbers fetched from the files.
     * @param docShingles all shingles created from the document/text
     * @return {int[]} array of MinHash values for the document/text
     */
    public int[] generateMinHashSignature(List<String> docShingles) {
        log.info("Generating MinHash from shingles");
        int[] minHash = new int[Constants.NUMBER_OF_RANDOM_NUMBERS + 1];

        int[] hashcodes = new int[docShingles.size()];
        for(int i = 0; i < docShingles.size(); i++) {
            hashcodes[i] = docShingles.get(i).hashCode();
        }
        minHash[0] = Collections.min(Arrays.asList(ArrayUtils.toObject(hashcodes)));

        for(int  i = 1; i <= Constants.NUMBER_OF_RANDOM_NUMBERS; i++) {
            int[] tempAllHashValues = new int[docShingles.size()];
            for(int j = 0; j < docShingles.size(); j++) {
                tempAllHashValues[j] = hashcodes[j] ^ randomNumbers[i - 1];
            }
            minHash[i] = Collections.min(Arrays.asList(ArrayUtils.toObject(tempAllHashValues)));
        }
        return minHash;
    }

    /**
     * Creates list of SHA256 hash from list of objects. The objects are first serialized into byte array and then the
     * hash is generated. Used as a generic method for generating SHA256 hash
     * @param listOfObjects list of java objects to generate hash for.
     * @return List of SHA256 hash as string
     */
    public List<String> generateSHA256HashFromObjects(List<byte[]> listOfObjects) {
        log.info("Generating SHA-256 hash.");
        List<String> sha256Hashes = new ArrayList<>();

        for(byte[] object : listOfObjects) {
            StringBuilder seedHash = new StringBuilder();
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                messageDigest.update(object);
                byte[] messageDigestBytes = messageDigest.digest();
                for(byte singleByte : messageDigestBytes) {
                    String hex = Integer.toHexString(0xFF & singleByte);
                    if(hex.length() == 1)
                        seedHash.append("0");
                    seedHash.append(hex);
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            sha256Hashes.add(seedHash.toString());
        }
        return sha256Hashes;
    }

    /**
     * Initializes the chain and submits the transaction into plagchain with document sha256 hash as key and  hexdata as
     * value (derived from ChainData)
     * @param walletAddress the wallet address of the logged in user
     * @param streamName the name of the stream to publish to
     * @param keyAsDocHash the sha256 hash of the whole document
     * @param hexData the data to be submitted in hex string format
     * @return {String} Transaction id
     */
    public String submitToPlagchainFrom(String walletAddress, String streamName, String keyAsDocHash, String hexData) {
        log.info("Submitting data to plagchain");
        String response = null;
        try {
            List<StreamItem> alreadyExistingKeys = StreamCommand.listStreamKeyItems(streamName, keyAsDocHash);
            if(alreadyExistingKeys != null && alreadyExistingKeys.size() > 0)
                return "Document already exists in " + streamName;
            response = StreamCommand.publishFromStream(walletAddress, streamName, keyAsDocHash, hexData);
            log.info("Transaction response: {}", response);
        } catch (MultichainException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Submits the transaction into plagchain as key pair in the mentioned stream.
     * @param streamName the name of the stream to publish to
     * @param key the key
     * @param hexData the data to be submitted in hex string format
     * @return {String} Transaction id
     */
    public String submitToPlagchain(String streamName, String key, String hexData) {
        log.info("Submitting data to plagchain");
        String response = null;
        try {
            response = StreamCommand.publishStream(streamName, key, hexData);
            log.info("Transaction response: {}", response);
        } catch (MultichainException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Transforms the hashes and contact information to JSON format as required by the plagchain and then to
     * hex string.
     * @param minHashList the list of integers representing the minhash of a document
     * @param imageHashList the list of sha256 hash of all images associated with this document
     * @param contactInfo the contact info as string
     * @return String data transformed into Hex String representation
     */
    public String formatDataToHex(String fileName, List<Integer> minHashList, List<String> imageHashList, String contactInfo) {
        log.info("Formatting data to Hex string");
        ChainData chainData = new ChainData();
        if(fileName != null && fileName.length() > 0)
            chainData.setFileName(fileName);
        if(minHashList != null && minHashList.size() > 0)
            chainData.setTextMinHash(minHashList);
        if(imageHashList != null && imageHashList.size() > 0)
            chainData.setImageHash(imageHashList);
        if(contactInfo != null && contactInfo.length() > 0)
            chainData.setContactInfo(contactInfo);

        Gson gson = new GsonBuilder().create();
        return DatatypeConverter.printHexBinary(gson.toJson(chainData, ChainData.class).getBytes());
    }

    /**
     * Generate random int numbers and store in a file.
     * @throws IOException if error while fetching or reading file
     */
    public void writeRandomNumbersToFile() throws IOException {
        log.info("Generating random number and writing to file");
        BufferedWriter bufferedWriter;
        FileWriter fileWriter;
        StringJoiner randomNumFile = new StringJoiner("\n");
        Random randomNumGenerator = new Random();
        for(int i = 0; i < Constants.NUMBER_OF_RANDOM_NUMBERS; i++) {
            randomNumFile.add(((Integer) randomNumGenerator.nextInt()).toString());
        }

        fileWriter = new FileWriter(Constants.RANDOM_NUMBERS_FILE);
        bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(randomNumFile.toString());

        bufferedWriter.close();
        fileWriter.close();
    }

    /**
     * Transforms the data from Hex-String to ChainData object
     * @param dataInHex the data in the form of hexadecimal string
     * @return {ChainData} object containing relevant information
     */
    public ChainData transformDataFromHexToObject(String dataInHex) {
        String dataInString = new String(DatatypeConverter.parseHexBinary(dataInHex));
        Gson gson = new Gson();
        return gson.fromJson(dataInString,ChainData.class);
    }

    /**
     * Data in Blockchain is always stored in the JSON format.
     * This is a generic method to transform the hex coded data into JSON object.
     * @param dataInHex the data in hexadecimal string
     * @return {JSONObject} with data from stream item
     */
    public JSONObject transformDataFromHextoJSON(String dataInHex) {
        String dataInString = new String(DatatypeConverter.parseHexBinary(dataInHex));
        try {
            return new JSONObject(dataInString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check the user response for Google recaptcha if it is correct or not.
     * @param gRecaptchaResponse user response as a string
     * @return boolean true, if the solved captcha was correct, false otherwise
     */
    public boolean checkGoogleRecaptcha(String gRecaptchaResponse) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(Constants.GOOGLE_RECAPTCHA_URL);
        List<NameValuePair> paramList = new ArrayList<>();
        paramList.add(new BasicNameValuePair("secret", Constants.GOOGLE_RECAPTCHA_SECRET));
        paramList.add(new BasicNameValuePair("response", gRecaptchaResponse));

        try {
            postRequest.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null)
                result.append(line);
            JSONObject responseString = new JSONObject(result.toString());
            return responseString.getBoolean("success");
        } catch (HttpHostConnectException e) {
            return false;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * To extract text from file based on file extension
     * @param multipartFile the multipartfile received from user
     * @return
     */
    public String getTextFromDoc(MultipartFile multipartFile) {
        if(multipartFile.getOriginalFilename().endsWith(".pdf"))
            return parsePdf(multipartFile);
        else
            try {
                return new String(multipartFile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
    }

    /**
     * Get all available servers list stored on the blockchain stream
     * @return List of PDServersDTO object
     */
    public List<PDServersDTO> getAllPDServers() {
        log.info("UtilService#getAllPDServers()");
        List<PDServersDTO> pdServersList = new ArrayList<>();
        try {
            List<StreamItem> allPDServersAsStream = StreamCommand.listStreamItems(Constants.PD_SERVERS_STREAM);
            for(StreamItem item : allPDServersAsStream) {
                PDServersDTO pdServer = new PDServersDTO();
                pdServer.setPdServerName(item.getKey());

                JSONObject itemData = transformDataFromHextoJSON(item.getData());
                if(itemData != null) {
                    pdServer.setPingUrl(itemData.getString("ping"));
                    pdServer.setSubmitDocUrl(itemData.getString("submitDoc"));
                    pdServer.setCheckSimUrl(itemData.getString("checkSim"));
                    pdServersList.add(pdServer);
                }
            }
        } catch (MultichainException | JSONException e) {
            e.printStackTrace();
        }
        return  pdServersList;
    }

    /**
     * Makes HTTP call to mentioned url with specified payloads
     * @param paramsList list of parameters
     * @param url url to which the post request is to be made
     * @return {GenericResponse} contains response from server
     */
    public GenericResponse postRequestPDServer(GenericPostRequest paramsList, String url, String publisherWalletAddress) {
        log.info("Making HTTP call to Plagdetection module");
        GenericResponse response = new GenericResponse();

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(url);

        try {
            File payload;
            if(paramsList.getTextualContent() != null && paramsList.getTextualContent().length() > 0) {
                payload = new File(paramsList.getFileName());
                FileUtils.writeStringToFile(payload, paramsList.getTextualContent(), Charset.defaultCharset());
            } else {
                payload = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + paramsList.getMultipartFile().getOriginalFilename());
                paramsList.getMultipartFile().transferTo(payload);
            }

            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            entity.addPart("file", new FileBody(payload));

            if(paramsList.getContactInfo() != null && paramsList.getContactInfo().length() > 0)
                entity.addPart("contactInfo", new StringBody(paramsList.getContactInfo()));

            entity.addPart("publisherWalletAddress", new StringBody(publisherWalletAddress));
            postRequest.setEntity(entity);
            HttpResponse responseFromServer = httpClient.execute(postRequest);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseFromServer.getEntity().getContent()));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null)
                result.append(line);
            response.setResponseText(result.toString());
        } catch (HttpHostConnectException e) {
            response.setError("Plag detection server not available");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}
