package com.example.jenkinstest;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.*;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

@RestController
@SpringBootApplication
public class JenkinsTestApplication {

    private JenkinsServer jenkinsServer;

    private static final String GIT_URL = "https://github.com/candyleer/jenkins-test.git";

    public static void main(String[] args) {
        SpringApplication.run(JenkinsTestApplication.class, args);
    }


    @PostConstruct
    public void init() throws URISyntaxException {
        jenkinsServer = new JenkinsServer(new URI("http://localhost:8080"), "admin", "admin");
    }

    @RequestMapping("hello")
    public Object hello() {
        return Collections.singletonMap("hello", "world");
    }

    @RequestMapping("create")
    public String jenkins() throws Exception {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.xml");
        assert resourceAsStream != null;
        String s = IOUtils.toString(resourceAsStream);
        String replacedText = s.replaceAll("GIT_URL", GIT_URL);
        jenkinsServer.createJob("auto_test_job", replacedText, true);
        return "success";
    }

    @RequestMapping("build")
    public String build(String jobName) throws Exception {
        JobWithDetails job = jenkinsServer.getJob(jobName);
        QueueReference queueReference = job.build(true);
        String queueItemUrlPart = queueReference.getQueueItemUrlPart();
        System.out.println(queueItemUrlPart);
        QueueItem queueItem = null;
        do {
            queueItem = jenkinsServer.getQueueItem(queueReference);
        } while (queueItem == null || queueItem.getExecutable() == null);
        Build build = jenkinsServer.getBuild(queueItem);
        BuildResult result = build.details().getResult();
        for (int i = 0; i < 10; i++) {
            System.out.println(result);
            Thread.sleep(500);
        }
        System.out.println(build.details().getConsoleOutputText());
        return "success";
    }

}

