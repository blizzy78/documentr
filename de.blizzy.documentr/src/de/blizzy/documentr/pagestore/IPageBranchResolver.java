package de.blizzy.documentr.pagestore;

import java.io.IOException;

public interface IPageBranchResolver {
	String resolvePageBranch(String projectName, String branchName, String pagePath) throws IOException;
}
