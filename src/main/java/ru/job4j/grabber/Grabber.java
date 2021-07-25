package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.html.SqlRuParse;
import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private static SqlRuDateTimeParser dateParser;
    private final Properties cfg = new Properties();

    public Store store() {
        return new PsqlStore(cfg);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg() throws IOException {
        try (InputStream in = new FileInputStream(new File("app.properties"))) {
            cfg.load(in);
        }
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

        public static class GrabJob implements Job {
            @Override
            public void execute(JobExecutionContext context) throws JobExecutionException {
                JobDataMap map = context.getJobDetail().getJobDataMap();
                Store store = (Store) map.get("store");
                Parse parse = (Parse) map.get("parse");
                List<Post> posts;
                try {
                    posts = parse.list("https://www.sql.ru/forum/job-offers");
                } catch (IOException e) {
                    throw new JobExecutionException("Ошибка загрузки данных с сайта", e);
                }
                for (Post post : posts) {
                    store.save(post);
                }
            }
        }

    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new SqlRuParse(dateParser), store, scheduler);
    }
}