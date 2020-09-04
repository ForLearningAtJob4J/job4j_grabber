package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

/**
 *  This is just another strange class
 */
public class AlertRabbit {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRabbit.class);

    public static Connection getConnection() {
//        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
        try (FileInputStream in = new FileInputStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("jdbc.driver"));
            return DriverManager.getConnection(
                    config.getProperty("jdbc.url"),
                    config.getProperty("jdbc.username"),
                    config.getProperty("jdbc.password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) {
        int timeInterval = 5;
        LOG.info("Program started");

//        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
        try (FileInputStream in = new FileInputStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(in);
            timeInterval = Integer.parseInt(config.getProperty("rabbit.interval"));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        try (Connection cn = getConnection()) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            JobDataMap data = new JobDataMap();
            data.put("connection", cn);
            JobDetail job = newJob(Rabbit.class).usingJobData(data).build();

            SimpleScheduleBuilder times = simpleSchedule().withIntervalInSeconds(timeInterval).repeatForever();
            Trigger trigger = newTrigger().startNow().withSchedule(times).build();

            scheduler.scheduleJob(job, trigger);

            scheduler.start();
            Thread.sleep(10_000);
            scheduler.shutdown();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        LOG.info("Program finished");
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit adds row to the base ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement st =
                         cn.prepareStatement("INSERT INTO rabbit (created) values (?)")) {
                st.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                st.executeUpdate();
            } catch (SQLException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }
}