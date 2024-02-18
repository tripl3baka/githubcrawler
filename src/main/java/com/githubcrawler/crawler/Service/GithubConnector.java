package com.githubcrawler.crawler.Service;

import com.githubcrawler.crawler.DTO.Branch;
import com.githubcrawler.crawler.DTO.Commit;
import com.githubcrawler.crawler.DTO.Repository;
import com.githubcrawler.crawler.DTO.ResponseRecords.BranchInfo;
import com.githubcrawler.crawler.DTO.ResponseRecords.ErrorResponse;
import com.githubcrawler.crawler.DTO.ResponseRecords.RepositoryInfo;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GithubConnector {

    private final RestTemplate restTemplate;
    private final String TOKEN;

    private final Gson gson = new Gson();

    @Autowired
    public GithubConnector(RestTemplate restTemplate, @Value("${github.token}") String TOKEN) {
        this.restTemplate = restTemplate;
        this.TOKEN = TOKEN;
    }

    public String getRepositories(String username) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(TOKEN);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = "https://api.github.com/users/" + username + "/repos";
            System.out.println(url);
                ResponseEntity<Repository[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Repository[].class);
                Repository[] repositories = response.getBody();
            assert repositories != null;
            List<RepositoryInfo> repositoryInfoList = Arrays.stream(repositories)
                    .filter(x -> !x.isFork())
                    .map(repo -> new RepositoryInfo(
                            repo.getOwner().getLogin(),
                            repo.getName(),
                            getBranchesWithCommits(username, repo.getName())
                    ))
                    .collect(Collectors.toList());

            return gson.toJson(repositoryInfoList);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return gson.toJson(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found"));
            }
            else if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED){
                return gson.toJson(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Token invalid"));
            }
            else {
                throw ex;
            }
        }
    }

    private List<BranchInfo> getBranchesWithCommits(String owner, String repoName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String branchesUrl = "https://api.github.com/repos/" + owner + "/" + repoName + "/branches";
        System.out.println(branchesUrl);
        ResponseEntity<Branch[]> response = restTemplate.exchange(branchesUrl, HttpMethod.GET, entity, Branch[].class);
        Branch[] branches = response.getBody();

        assert branches != null;
        return Arrays.stream(branches)
                .map(branch -> new BranchInfo(branch.getName(), getLastCommitSha(owner, repoName, branch.getName())))
                .collect(Collectors.toList());
    }

    private String getLastCommitSha(String owner, String repoName, String branchName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String commitsUrl = "https://api.github.com/repos/" + owner + "/" + repoName + "/commits?sha=" + branchName;
        System.out.println(commitsUrl);
        ResponseEntity<Commit[]> response = restTemplate.exchange(commitsUrl, HttpMethod.GET, entity, Commit[].class);
        Commit[] commits = response.getBody();

        if (commits != null && commits.length > 0) {
            return commits[0].getSha();
        } else {
            return "No commits";
        }
    }

}




