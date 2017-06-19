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
import com.plagui.modules.StreamFormats.ChainData;
import multichain.command.ChainCommand;
import multichain.command.MultichainException;
import multichain.command.StreamCommand;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
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
     */
    public UtilService() {
        File randomNumbersFiles = new File(Constants.RANDOM_NUMBERS_FILE);
        try {
            if(!randomNumbersFiles.exists())
                writeRandomNumbersToFile();
            populateRandomNumbers();
        } catch (IOException e) {
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
     * Convert all characters to lowercase. Break the text into sentences.
     * @param uncleanText the text that needs to be cleaned
     * @return {List<String>} containing separate sentences extracted from pdf file.
     */
    public List<String> cleanText(String uncleanText) {
        log.info("Cleaning text.");
        List<String> stringArrToList = new ArrayList<>();

        uncleanText = uncleanText.replaceAll("-\n","");
        uncleanText = uncleanText.replaceAll("[\n]"," ");
        uncleanText = StringUtils.normalizeSpace(uncleanText);
        uncleanText = uncleanText.toLowerCase();
        try {
            InputStream modelIn = new FileInputStream(Constants.EN_SENTENCE_BIN_LOC);
            SentenceModel model = new SentenceModel(modelIn);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
            Collections.addAll(stringArrToList, sentenceDetector.sentDetect(uncleanText));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringArrToList;
    }

    /**
     * Create word shingles of specified length from a sentence.
     * @param shingleLength the fixed length of all shingles
     * @param extractedSentences the list of sentences from which shingles need to be extracted
     * @return {List<String>} list containing word shingles from the sentence
     */
    public List<String> createShingles(int shingleLength, List<String> extractedSentences) {
        log.info("Creating shingles of length: {}", shingleLength);
        List<String> shinglesFromSentences = new ArrayList<>();
        for(String sentence : extractedSentences) {
            int firstIndex = 0;
            int lastIndex = sentence.indexOf(" ");
            for(int i = 1; i < shingleLength; i++) {
                lastIndex = sentence.indexOf(" ", lastIndex + 1);
            }
            while(lastIndex < sentence.length() && lastIndex > 0) {
                shinglesFromSentences.add(sentence.substring(firstIndex, lastIndex));
                firstIndex = sentence.indexOf(" ", firstIndex + 1);
                lastIndex = sentence.indexOf(" ", lastIndex + 1);
            }
        }
        return shinglesFromSentences;
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
     * @param streamName the name of the stream to publish to
     * @param keyAsDocHash the sha256 hash of the whole document
     * @param hexData the data to be submitted in hex string format
     * @return {String} Transaction id
     */
    public String submitToPlagchain(String streamName, String keyAsDocHash, String hexData) {
        log.info("Submitting data to plagchain");
        String response = null;
        ChainCommand.initializeChain(Constants.CHAIN_NAME);
        try {
            response = StreamCommand.publishStream(streamName, keyAsDocHash, hexData);
            System.out.println(response);
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
    public String formatDataToHex(List<Integer> minHashList, List<String> imageHashList, String contactInfo) {
        log.info("Formatting data to Hex string");
        ChainData chainData = new ChainData();
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
     * Read the random number file and populate the randomNumbers int array.
     * @throws IOException if error while fetching or reading file
     */
    public void populateRandomNumbers() throws IOException {
        log.info("Populating random numbers from file");
        BufferedReader bufferedReader;
        FileReader fileReader;
        String number;
        int i = 0;
        fileReader = new FileReader(Constants.RANDOM_NUMBERS_FILE);
        bufferedReader = new BufferedReader(fileReader);
        while((number = bufferedReader.readLine()) != null) {
            randomNumbers[i] = Integer.parseInt(number);
            i++;
        }
        bufferedReader.close();
        fileReader.close();
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

   /* *//**
     * Serializes java object to byte array
     * @param obj any java object
     * @return byte[] of the java object
     * @throws IOException if stream not available
     *//*
    public byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    *//**
     * Deserializes byte array to java object
     * @param data any byte array
     * @return Object java object
     * @throws IOException if stream not available
     *//*
    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }*/
}
