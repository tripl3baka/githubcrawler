package com.githubcrawler.crawler.Controller;

import com.githubcrawler.crawler.Service.GithubConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {
    private final GithubConnector githubConnector;

    @Autowired
    public APIController(GithubConnector githubConnector) {
        this.githubConnector = githubConnector;
    }

    @GetMapping("/repositories/{name}")
    public String getRepositories(@PathVariable String name) {
        return githubConnector.getRepositories(name);
    }
}
