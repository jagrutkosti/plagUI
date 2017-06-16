package com.plagui.modules.UploadDocs;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.plagui.config.Constants;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Created by Jagrut on 07-06-2017.
 * Service class to handle all logic and processing work related to the Plagchain
 */
@Service
public class PlagchainService {
    private final Logger log = LoggerFactory.getLogger(PlagchainService.class);
    private final int[] randomNumbers = new int[Constants.NUMBER_OF_RANDOM_NUMBERS];

    /**
     * !!!DO NOT MODIFY random-number.txt FILE EVER, after deployment!!!
     *
     * Initialize random numbers from file. The random numbers used should always be the same for all documents for
     * MinHash algorithm to work. So, if you delete the random-number.txt file, you need to re-index all documents.
     */
    public PlagchainService() {
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
     * Read the random number file and populate the randomNumbers int array.
     * @throws IOException
     */
    public void populateRandomNumbers() throws IOException {
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
     * @throws IOException
     */
    public void writeRandomNumbersToFile() throws IOException{
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
}
