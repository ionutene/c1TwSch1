package org.prv.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static int queryInterval = 1800000;
    private static int delayQueryInterval = 60000;
    private static String pattern = "HH:mm:ss";
    private static String searchItems = "out.txt";

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] ar) throws TwitterException {
        initProperties();
        List<Status> history = new ArrayList<>();
        Twitter twitter = TwitterFactory.getSingleton();

        while (true) try {
            List<Status> allStatuses = twitter.getHomeTimeline();
            for (String searchedString : getSearchStringFromFile()) {
                List<Status> finalHistory = history;
                List<Status> results = allStatuses.stream()
                        .filter(e -> e.getText().toLowerCase().contains(searchedString))
                        .filter(e -> e.getCreatedAt().getTime() > Instant.now().toEpochMilli() - queryInterval)
                        .filter(e -> !finalHistory.contains(e))
                        .collect(Collectors.toList());

                history = display(history, results);

                forgetOldResults(history);
            }
            Thread.sleep(delayQueryInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void forgetOldResults(List<Status> history) {
        for(Status status:history){
            if(status.getCreatedAt().getTime() + queryInterval < Instant.now().toEpochMilli()){
                history.remove(status);
            }
        }
    }

    private static List<Status> display(List<Status> history, List<Status> statuses2) {
        for (Status status : statuses2) {
            print(status);
            history.add(status);
        }
        return history;
    }

    private static void initProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = Main.class.getClassLoader().getResourceAsStream("config.properties");

            prop.load(input);

            if (prop.getProperty("queryInterval") != null) {
                queryInterval = Integer.valueOf(prop.getProperty("queryInterval"));
                LOGGER.info("loaded queryInterval: " + queryInterval);
            }

            if (prop.getProperty("delayQueryInterval") != null) {
                delayQueryInterval = Integer.valueOf(prop.getProperty("delayQueryInterval"));
                LOGGER.info("loaded delayQueryInterval: " + delayQueryInterval);
            }

            if (prop.getProperty("pattern") != null) {
                pattern = prop.getProperty("pattern");
                LOGGER.info("loaded pattern: " + pattern);
            }

            if (prop.getProperty("searchItems") != null) {
                searchItems = prop.getProperty("searchItems");
                LOGGER.info("loaded searchItems: " + searchItems);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<String> getSearchStringFromFile() {
        List<String> searchList = new ArrayList<>();
        try {
            File file = new File(searchItems);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            LOGGER.info("Found in file: " + file.getAbsolutePath());
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append(" | ");
                searchList.add(line);
            }
            fileReader.close();
            LOGGER.info("Contents of file: ");
            LOGGER.info(stringBuffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchList;
    }

    private static void print(Status status) {
        LOGGER.info(
                new SimpleDateFormat(pattern).format(Calendar.getInstance().getTime()) + "\n\t\t\t" +
                        new Date(status.getCreatedAt().getTime()) + " "
                        + status.getText() + "\n\t\t\t"
                       // + status.getQuotedStatusPermalink() + "\n\t\t\t "
                        + status.getUser().getName() + "\n\t\t\t "
                        + "https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId() + "\n\t\t\t "
                        + "\n__________________________________________________");
    }

}