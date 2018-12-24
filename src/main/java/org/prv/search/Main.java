package org.prv.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static String pattern = "HH:mm:ss";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] ar) throws TwitterException {
        List<Status> history = new ArrayList<>();
        Twitter twitter = TwitterFactory.getSingleton();

        while (true) {
            try {
                List<Status> statuses = twitter.getHomeTimeline();
                for (String searchString1 : getSearchStringFromFile()) {
                    List<Status> statuses2 = statuses.stream()
                            .filter(e -> e.getText().toLowerCase().contains(searchString1))
                            .filter(e -> e.getCreatedAt().getTime() > Instant.now().toEpochMilli() - 1800000)
                            .filter(e -> !history.contains(e))
                            .collect(Collectors.toList());
                    for (Status status : statuses2) {
                        print(status);
                        history.add(status);
                    }
                }
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = getClass().getClassLoader().getResourceAsStream("config.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            System.out.println(prop.getProperty("database"));
            System.out.println(prop.getProperty("dbuser"));
            System.out.println(prop.getProperty("dbpassword"));

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
            File file = new File("out.txt");
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
                        + status.getQuotedStatusPermalink() + "\n\t\t\t "
                          + status.getUser().getName()
                        + "\n__________________________________________________");
    }

    private static void print(List<Status> statuses) {
        for (Status searchStatus : statuses) {
            LOGGER.info(
                    "\n-------------------------getName------\n"
                            + searchStatus.getUser().getName()
                            + "\n-------------------------getText------\n"
                            + searchStatus.getText()
                            + "\n----------------------------getLang-----\n"
                            + searchStatus.getLang()
                            + "\n-------------------------getSource------\n"
                            + searchStatus.getSource()
                            + "\n-------------------------get time------\n"
                            + Instant.now().getEpochSecond()
                            + "\n-------------------------getQuotedStatusPermalink------\n"
                            + searchStatus.getQuotedStatusPermalink()

            );
        }
    }

    private static void printLite(List<Status> statuses) {
        for (Status status : statuses) {
            LOGGER.info(
                    simpleDateFormat.format(new Date(status.getCreatedAt().getTime())) + " "
                            + status.getText() + "\n\t\t"
                            + status.getQuotedStatusPermalink() + "\n\t\t "
                            + status.getUser().getName() + "\n\t\t");

        }
    }

    private static List<Status> filterStatus(List<Status> statuses, final String filterBy) {
        return statuses.stream().filter(e -> e.getText().toLowerCase().contains(filterBy.toLowerCase())).collect(Collectors.toList());
    }

    private static void fileWrite() {
        try {
            File fout = new File("out.txt");

            FileOutputStream fos = new FileOutputStream(fout);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            for (int i = 0; i < 10; i++) {
                bw.write("something");
                bw.newLine();
            }

            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Status> searchSimular(Twitter twitter, String tag) throws TwitterException {
        List<Status> result = new ArrayList<>();
        QueryResult qRes = twitter.search(new Query(tag).lang("EN").count(100).count(100).resultType(Query.ResultType.recent));
        do {
            result.addAll(qRes.getTweets());
            qRes = twitter.search(qRes.nextQuery());
        } while (result.size() < 200 && qRes.hasNext());

        return result;
    }





}