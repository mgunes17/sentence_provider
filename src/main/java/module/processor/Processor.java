package module.processor;

import content.ContentHandler;
import model.Sentence;
import model.Source;
import module.crawler.WebPage;
import module.processor.preprocess.PreprocessHandler;
import module.processor.preprocess.PreprocessedSentence;
import module.processor.validation.ValidationHandler;
import module.saver.DBSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static application.Admin.crawler;

/**
 * Created by mustafa on 23.04.2017.
 */
public class Processor {
    private final static Logger logger = LoggerFactory.getLogger(Processor.class);
    private DBSaver dbSaver;
    private Queue<Source> dataToSaveQueue;
    private ContentHandler contentHandler;
    private ValidationHandler validationHandler;

    public Processor(DBSaver dbSaver) {
        this.dbSaver = dbSaver;
        dataToSaveQueue = new LinkedList<Source>();
        contentHandler = new ContentHandler();
        validationHandler = new ValidationHandler();
    }

    public void process(Queue<WebPage> webPageQueue) {
        WebPage webPage;
        logger.trace("Processor running..");

        while((webPage = webPageQueue.poll()) != null) {
            Source source = buildSource(webPage.getUrl(), webPage.getContent());
            logger.trace("Processor " + source.getSourceName());

            if(isSourceWorthy(source)) { //doküman kayda değer mi ona bakılacak
                dataToSaveQueue.offer(source);
                logger.info("Processor kayda değer bulundu " + source.getSourceName());
            }
        }

        System.out.println("Veri işleme tamamlandı.");
        System.out.println("Veriler kaydediliyor.");
        dbSaver.save(dataToSaveQueue);
    }

    private boolean isSourceWorthy(Source source){
        int sentenceCount = source.getSentenceSet().size();

        return sentenceCount > 5;
    }

    private Source buildSource(String sourceName, String content){
        Source source = new Source(sourceName);
        List<String> sentences = contentHandler.getSentencesFromParagraph(content);

        for(String sentence: sentences) {
            if(validationHandler.validate(sentence)){
                PreprocessHandler preprocessHandler = new PreprocessHandler();
                PreprocessedSentence preprocessedSentence = preprocessHandler.process(sentence);

                Sentence sentenceObject = buildSentenceObject(preprocessedSentence);
                sentenceObject.setSourceName(sourceName);
                source.getSentenceSet().add(sentenceObject);
                source.updateWordCountMap(preprocessedSentence.getWordCountMap());
            }
        }

        return source;
    }

    private Sentence buildSentenceObject(PreprocessedSentence preprocessedSentence){
        Sentence newSentence = new Sentence(preprocessedSentence.getOriginalSentence());
        newSentence.setStemmedWordsList(preprocessedSentence.getStemList());
        newSentence.setTokenList(preprocessedSentence.getTokenList());
        // tags ata
        // questions ata

        return newSentence;
    }

    private boolean isSaveable(String content) {
        return false;
    }

    public Queue<Source> getDataToSaveQueue() {
        return dataToSaveQueue;
    }

    public void setDataToSaveQueue(Queue<Source> dataToSaveQueue) {
        this.dataToSaveQueue = dataToSaveQueue;
    }
}
