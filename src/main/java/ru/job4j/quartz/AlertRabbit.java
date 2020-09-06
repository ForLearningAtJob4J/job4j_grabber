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
    private static final String PROPERTY_FILE_NAME = "rabbit.properties";
    private static final Logger LOG = LoggerFactory.getLogger(AlertRabbit.class);

    public static void main(String[] args) {
        LOG.info("Program started");
        try (Connection cn = getConnection()) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            prepareSheduler(scheduler, cn);
            scheduler.start();
            Thread.sleep(10_000);
            scheduler.shutdown();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        LOG.info("Program finished");
    }

    private static void prepareSheduler(Scheduler scheduler, Connection cn) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("connection", cn);
        JobDetail job = newJob(Rabbit.class).usingJobData(data).build();

        SimpleScheduleBuilder times = simpleSchedule().withIntervalInSeconds(getTimerInterval()).repeatForever();
        Trigger trigger = newTrigger().startNow().withSchedule(times).build();

        scheduler.scheduleJob(job, trigger);
    }

    public static Connection getConnection() {
        try (FileInputStream in = new FileInputStream(PROPERTY_FILE_NAME)) {
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

    private static int getTimerInterval() {
        try (FileInputStream in = new FileInputStream(PROPERTY_FILE_NAME)) {
            Properties config = new Properties();
            config.load(in);
            return Integer.parseInt(config.getProperty("rabbit.interval"));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return 5;
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