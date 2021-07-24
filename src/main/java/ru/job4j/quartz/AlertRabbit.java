package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static Connection cn;
    private static Properties pr;
    private static int interval;

    private static void init() {
        try (var in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            pr = new Properties();
            pr.load(in);
            Class.forName(pr.getProperty("hibernate.connection.driver_class"));
            String url = pr.getProperty("hibernate.connection.url");
            String username = pr.getProperty("hibernate.connection.username");
            String password = pr.getProperty("hibernate.connection.password");
            interval = Integer.parseInt(pr.getProperty("rabbit.interval"));
            cn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            init();
            createTable();
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

    private static void createTable() throws SQLException {
        String createTable = String.format("create table rabbit(%s);",
                "created_date timestamp");
        try (PreparedStatement ps = cn.prepareStatement(createTable)) {
            ps.execute();
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