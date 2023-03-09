package net.akaritakai.stream.scheduling;

import net.akaritakai.stream.InitDB;
import net.akaritakai.stream.Main;
import net.akaritakai.stream.scheduling.jobs.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public final class SetupScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(SetupScheduler.class);

    private SetupScheduler() {
    }

    public static SchedulerFactory createFactory() throws Exception {
        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Properties schedulerProperties = new Properties();
        schedulerProperties.setProperty("org.quartz.threadPool.class", org.quartz.simpl.SimpleThreadPool.class.getName());
        schedulerProperties.setProperty("org.quartz.threadPool.threadCount", "4");
        schedulerProperties.setProperty("org.quartz.jobStore.class", org.quartz.impl.jdbcjobstore.JobStoreTX.class.getName());
        schedulerProperties.setProperty("org.quartz.jobStore.driverDelegateClass", SqlLiteJDBCDelegate.class.getName());
        schedulerProperties.setProperty("org.quartz.jobStore.dataSource", "myDS");
        schedulerProperties.setProperty("org.quartz.jobStore.useProperties", "true");
        schedulerProperties.setProperty("org.quartz.dataSource.myDS.driver", JDBC.class.getName());
        schedulerProperties.setProperty("org.quartz.dataSource.myDS.URL", "jdbc:sqlite:scheduler.db");

        try (Connection connection = DriverManager.getConnection(schedulerProperties.getProperty("org.quartz.dataSource.myDS.URL"));
             ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM QRTZ_LOCKS")) {
            LOG.info("Database already initialized");
        } catch (SQLException ex) {
            LOG.warn("Attempting to reinitialize the database");
            InitDB.main(new String[0]);
        }

        schedulerFactory.initialize(schedulerProperties);

        return schedulerFactory;
    }

    public static void setup(Scheduler scheduler) throws SchedulerException {
        if (!scheduler.checkExists(JobKey.jobKey("ChatEnable"))) {
            scheduler.addJob(JobBuilder.newJob(ChatEnableJob.class).withIdentity("ChatEnable").storeDurably().build(), false);
        }

        if (!scheduler.checkExists(JobKey.jobKey("ChatDisable"))) {
            scheduler.addJob(JobBuilder.newJob(ChatDisableJob.class).withIdentity("ChatDisable").storeDurably().build(), false);
        }

        if (!scheduler.checkExists(JobKey.jobKey("ChatSend"))) {
            scheduler.addJob(JobBuilder.newJob(ChatSendJob.class).withIdentity("ChatSend").storeDurably().build(), false);
        }

        if (!scheduler.checkExists(JobKey.jobKey("StreamPlay"))) {
            scheduler.addJob(JobBuilder.newJob(StreamPlayJob.class).withIdentity("StreamPlay").storeDurably().build(), false);
        }

        if (!scheduler.checkExists(JobKey.jobKey("StreamStop"))) {
            scheduler.addJob(JobBuilder.newJob(StreamStopJob.class).withIdentity("StreamStop").storeDurably().build(), false);
        }
    }
}
