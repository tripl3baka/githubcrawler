package com.githubcrawler.crawler.DTO.ResponseRecords;

import java.util.List;

public record RepositoryInfo(String owner, String name, List<BranchInfo> branches) {
}