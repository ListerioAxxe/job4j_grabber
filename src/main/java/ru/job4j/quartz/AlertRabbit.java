package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.*;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static Connection cn;
    private static Properties pr;
    private static int interval;

    private static void init() throws SQLException, ClassNotFoundException {
        Class.forName(pr.getProperty("hibernate.connection.driver_class"));
        String url = pr.getProperty("hibernate.connection.url");
        String username = pr.getProperty("hibernate.connection.username");
        String password = pr.getProperty("hibernate.connection.password");
        cn = DriverManager.getConnection(url, username, password);
        interval = Integer.parseInt(pr.getProperty("rabbit.interval"));
    }

    private static void prRead() {
        try (var in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            pr = new Properties();
            pr.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(10)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
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
            Connection connection = (Connection) context
                    .getJobDetail().getJobDataMap().get("connection");
            try (Statement a = connection.createStatement()) {
                String insertData = (String) context
                        .getJobDetail().getJobDataMap().get("insertData");
                a.execute(insertData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}