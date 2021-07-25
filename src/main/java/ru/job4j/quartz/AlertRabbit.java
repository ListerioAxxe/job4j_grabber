package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    private static Properties pr;
    private static int interval;

    private static Connection getConnection()
            throws IOException, SQLException, ClassNotFoundException {
        try (var in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            pr = new Properties();
            pr.load(in);
            Class.forName(pr.getProperty("hibernate.connection.driver_class"));
            String url = pr.getProperty("hibernate.connection.url");
            String username = pr.getProperty("hibernate.connection.username");
            String password = pr.getProperty("hibernate.connection.password");
            interval = Integer.parseInt(pr.getProperty("rabbit.interval"));
            return DriverManager.getConnection(url, username, password);
        }
    }

    private static void createTable(Connection cn) throws SQLException {
        String createTable = String.format("create table rabbit(%s);",
                "created_date timestamp");
            try (PreparedStatement ps = cn.prepareStatement(createTable)) {
                ps.execute();
            }
    }

    public static void main(String[] args) {
        try  (Connection cn = getConnection()) {
            createTable(cn);
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            String insertData = (String.format("insert into rabbit(%s) values ('%s');",
                    "created_date", Timestamp.valueOf(
                            LocalDateTime.now().withNano(0))));
            JobDataMap data = new JobDataMap();
            data.put("connection", cn);
            data.put("insertData", insertData);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail()
                    .getJobDataMap().get("connection");
            try (Statement a = connection.createStatement()) {
                String insertData = (String) context.getJobDetail()
                        .getJobDataMap().get("insertData");
                a.execute(insertData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}